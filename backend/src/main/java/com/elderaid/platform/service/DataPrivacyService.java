package com.elderaid.platform.service;

import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.domain.user.UserStatus;
import com.elderaid.platform.domain.worker.WorkerProfile;
import com.elderaid.platform.exception.InvalidCredentialsException;
import com.elderaid.platform.repository.BookingRepository;
import com.elderaid.platform.repository.ConsentRecordRepository;
import com.elderaid.platform.repository.ElderlyProfileRepository;
import com.elderaid.platform.repository.FamilyLinkRepository;
import com.elderaid.platform.repository.RefreshTokenRepository;
import com.elderaid.platform.repository.TaskApplicationRepository;
import com.elderaid.platform.repository.TaskRequestRepository;
import com.elderaid.platform.repository.UserRepository;
import com.elderaid.platform.repository.VerificationDocumentRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.web.dto.UserDataExportResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * GDPR access (Article 15/20) and erasure (Article 17) for one's own
 * account. Erasure here means anonymization, not row deletion - bookings,
 * payments, and reviews involving other people stay intact for their sake
 * and for the records the platform may be legally required to keep; only
 * this account's own identifying fields get scrubbed.
 */
@Service
public class DataPrivacyService {

    private final UserRepository userRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final FamilyLinkRepository familyLinkRepository;
    private final ElderlyProfileRepository elderlyProfileRepository;
    private final TaskRequestRepository taskRequestRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final VerificationDocumentRepository verificationDocumentRepository;
    private final TaskApplicationRepository taskApplicationRepository;
    private final BookingRepository bookingRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public DataPrivacyService(
            UserRepository userRepository,
            ConsentRecordRepository consentRecordRepository,
            FamilyLinkRepository familyLinkRepository,
            ElderlyProfileRepository elderlyProfileRepository,
            TaskRequestRepository taskRequestRepository,
            WorkerProfileRepository workerProfileRepository,
            VerificationDocumentRepository verificationDocumentRepository,
            TaskApplicationRepository taskApplicationRepository,
            BookingRepository bookingRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.consentRecordRepository = consentRecordRepository;
        this.familyLinkRepository = familyLinkRepository;
        this.elderlyProfileRepository = elderlyProfileRepository;
        this.taskRequestRepository = taskRequestRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.verificationDocumentRepository = verificationDocumentRepository;
        this.taskApplicationRepository = taskApplicationRepository;
        this.bookingRepository = bookingRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserDataExportResponse exportMyData(UUID userId) {
        AppUser user = userRepository.findById(userId).orElseThrow();

        var account = new UserDataExportResponse.Account(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getLocale(), user.getCreatedAt()
        );

        List<UserDataExportResponse.Consent> consents = consentRecordRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(c -> new UserDataExportResponse.Consent(
                        c.getConsentType().name(), c.isGiven(), c.getPolicyVersion(), c.getCreatedAt()))
                .toList();

        List<UserDataExportResponse.ElderlyProfileSummary> elderlyProfiles = familyLinkRepository
                .findByFamilyUserId(userId).stream()
                .map(link -> new UserDataExportResponse.ElderlyProfileSummary(
                        link.getElderlyProfile().getId(),
                        link.getElderlyProfile().getFirstName(),
                        link.getElderlyProfile().getLastName(),
                        link.getRelationship(),
                        link.getPermissionLevel()))
                .toList();

        List<UserDataExportResponse.PostedTask> postedTasks = taskRequestRepository
                .findByPostedByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(task -> new UserDataExportResponse.PostedTask(
                        task.getId(), task.getCategory(), task.getStatus(), task.getPriceOffered(), task.getCreatedAt()))
                .toList();

        UserDataExportResponse.WorkerProfileSummary workerSummary = workerProfileRepository.findByUserId(userId)
                .map(this::toWorkerSummary)
                .orElse(null);

        return new UserDataExportResponse(account, consents, elderlyProfiles, postedTasks, workerSummary);
    }

    private UserDataExportResponse.WorkerProfileSummary toWorkerSummary(WorkerProfile workerProfile) {
        List<UserDataExportResponse.VerificationDocumentSummary> documents = verificationDocumentRepository
                .findByWorkerProfileIdOrderBySubmittedAtDesc(workerProfile.getId()).stream()
                .map(doc -> new UserDataExportResponse.VerificationDocumentSummary(
                        doc.getDocumentType(), doc.getStatus(), doc.getSubmittedAt()))
                .toList();

        List<UserDataExportResponse.ApplicationSummary> applications = taskApplicationRepository
                .findByWorkerProfileIdOrderByAppliedAtDesc(workerProfile.getId()).stream()
                .map(app -> new UserDataExportResponse.ApplicationSummary(
                        app.getTaskRequest().getId(), app.getStatus(), app.getAppliedAt()))
                .toList();

        List<UserDataExportResponse.BookingSummary> bookings = bookingRepository
                .findByWorkerProfileIdOrderByCreatedAtDesc(workerProfile.getId()).stream()
                .map(booking -> new UserDataExportResponse.BookingSummary(
                        booking.getId(), booking.getTaskRequest().getId(), booking.getStatus().name(),
                        booking.getCheckInTime(), booking.getCheckOutTime()))
                .toList();

        return new UserDataExportResponse.WorkerProfileSummary(
                workerProfile.getVerificationTier(),
                workerProfile.getAverageRating(),
                workerProfile.getCompletedTasksCount(),
                documents,
                applications,
                bookings
        );
    }

    @Transactional
    public void deleteMyAccount(UUID userId, String currentPassword) {
        AppUser user = userRepository.findById(userId).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        anonymizeUser(user);
        revokeAllRefreshTokens(userId);
        anonymizeSelfLinkedElderlyProfiles(userId);
    }

    private void anonymizeUser(AppUser user) {
        user.setEmail("deleted-" + UUID.randomUUID() + "@deleted.elderaid.invalid");
        user.setFirstName("Poistettu");
        user.setLastName("kayttaja");
        user.setPhone(null);
        // Not the same as a known password, and not recoverable - this
        // account can no longer authenticate at all, by design.
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    private void revokeAllRefreshTokens(UUID userId) {
        refreshTokenRepository.findByUserIdAndRevokedFalse(userId).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    /**
     * Only profiles linked to the deleted account's own login (the forSelf
     * case) get scrubbed - profiles this user merely managed on behalf of
     * someone else belong conceptually to that other person, not to this
     * account, and stay untouched.
     */
    private void anonymizeSelfLinkedElderlyProfiles(UUID userId) {
        elderlyProfileRepository.findByUserId(userId).forEach(profile -> {
            profile.setFirstName("Poistettu");
            profile.setLastName("kayttaja");
            profile.setAddressLine(null);
            profile.setCity(null);
            profile.setPostalCode(null);
            elderlyProfileRepository.save(profile);
        });
    }
}
