package com.elderaid.platform.service;

import com.elderaid.platform.domain.elderly.ElderlyProfile;
import com.elderaid.platform.domain.elderly.FamilyLink;
import com.elderaid.platform.domain.elderly.PermissionLevel;
import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.exception.ForbiddenOperationException;
import com.elderaid.platform.exception.InvalidRequestException;
import com.elderaid.platform.exception.ResourceNotFoundException;
import com.elderaid.platform.repository.ElderlyProfileRepository;
import com.elderaid.platform.repository.FamilyLinkRepository;
import com.elderaid.platform.repository.UserRepository;
import com.elderaid.platform.web.dto.CreateElderlyProfileRequest;
import com.elderaid.platform.web.dto.ElderlyProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ElderlyProfileService {

    private final ElderlyProfileRepository elderlyProfileRepository;
    private final FamilyLinkRepository familyLinkRepository;
    private final UserRepository userRepository;

    public ElderlyProfileService(
            ElderlyProfileRepository elderlyProfileRepository,
            FamilyLinkRepository familyLinkRepository,
            UserRepository userRepository
    ) {
        this.elderlyProfileRepository = elderlyProfileRepository;
        this.familyLinkRepository = familyLinkRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ElderlyProfileResponse createProfile(UUID callerId, CreateElderlyProfileRequest request) {
        AppUser caller = userRepository.getReferenceById(callerId);
        boolean forSelf = Boolean.TRUE.equals(request.forSelf());

        if (!forSelf && (request.relationship() == null || request.relationship().isBlank())) {
            throw new InvalidRequestException("relationship is required when not creating a profile for yourself");
        }

        ElderlyProfile.ElderlyProfileBuilder profileBuilder = ElderlyProfile.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .addressLine(request.addressLine())
                .city(request.city())
                .postalCode(request.postalCode())
                .preferredLanguage(request.preferredLanguage() != null ? request.preferredLanguage() : "fi");

        // An elderly person managing their own profile gets it linked
        // straight to their login - this is what lets them log in later and
        // see their own profile directly rather than only through a family
        // member's account.
        if (forSelf) {
            profileBuilder.user(caller);
        }

        ElderlyProfile profile = elderlyProfileRepository.save(profileBuilder.build());

        // Whoever creates the profile becomes its first family link, with
        // full permissions - they can invite siblings etc. with reduced
        // permissions later through a separate endpoint.
        FamilyLink link = FamilyLink.builder()
                .familyUser(caller)
                .elderlyProfile(profile)
                .relationship(forSelf ? "self" : request.relationship())
                .permissionLevel(PermissionLevel.FULL)
                .build();
        familyLinkRepository.save(link);

        return toResponse(profile, link);
    }

    @Transactional(readOnly = true)
    public List<ElderlyProfileResponse> listMine(UUID callerId) {
        return familyLinkRepository.findByFamilyUserId(callerId).stream()
                .map(link -> toResponse(link.getElderlyProfile(), link))
                .toList();
    }

    /**
     * Used by the task feature to check whether the caller is allowed to
     * post a task for this elderly profile. Throws rather than returning a
     * boolean, since every caller of this needs to react the same way - reject
     * the request - and there's no legitimate case where the caller would
     * want to keep going after a permission failure here.
     */
    @Transactional(readOnly = true)
    public FamilyLink requireBookingPermission(UUID callerId, UUID elderlyProfileId) {
        FamilyLink link = familyLinkRepository.findByFamilyUserIdAndElderlyProfileId(callerId, elderlyProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("No link to this elderly profile"));

        if (link.getPermissionLevel() == PermissionLevel.VIEW_ONLY) {
            throw new ForbiddenOperationException("This permission level cannot create bookings");
        }
        return link;
    }

    private ElderlyProfileResponse toResponse(ElderlyProfile profile, FamilyLink link) {
        return new ElderlyProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getDateOfBirth(),
                profile.getAddressLine(),
                profile.getCity(),
                profile.getPostalCode(),
                profile.getPreferredLanguage(),
                link.getRelationship(),
                link.getPermissionLevel()
        );
    }
}
