package com.elderaid.platform.service;

import com.elderaid.platform.domain.booking.Booking;
import com.elderaid.platform.domain.booking.BookingStatus;
import com.elderaid.platform.domain.task.ApplicationStatus;
import com.elderaid.platform.domain.task.TaskApplication;
import com.elderaid.platform.domain.task.TaskRequest;
import com.elderaid.platform.domain.task.TaskStatus;
import com.elderaid.platform.exception.ForbiddenOperationException;
import com.elderaid.platform.exception.ResourceNotFoundException;
import com.elderaid.platform.repository.BookingRepository;
import com.elderaid.platform.repository.TaskApplicationRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.web.dto.BookingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final TaskApplicationService taskApplicationService;
    private final TaskApplicationRepository taskApplicationRepository;
    private final BookingRepository bookingRepository;
    private final WorkerProfileRepository workerProfileRepository;

    public BookingService(
            TaskApplicationService taskApplicationService,
            TaskApplicationRepository taskApplicationRepository,
            BookingRepository bookingRepository,
            WorkerProfileRepository workerProfileRepository
    ) {
        this.taskApplicationService = taskApplicationService;
        this.taskApplicationRepository = taskApplicationRepository;
        this.bookingRepository = bookingRepository;
        this.workerProfileRepository = workerProfileRepository;
    }

    @Transactional
    public BookingResponse acceptApplication(UUID callerId, UUID taskId, UUID applicationId) {
        TaskApplication application = taskApplicationService.findOwnedPendingApplication(callerId, taskId, applicationId);
        TaskRequest task = application.getTaskRequest();

        if (task.getStatus() != TaskStatus.OPEN) {
            // Shouldn't normally happen - a task only leaves OPEN once an
            // application is accepted, and accepting is the one thing that
            // can land here - but worth guarding against double-accepts
            // racing each other rather than trusting the PENDING check alone.
            throw new ForbiddenOperationException("This task is no longer open");
        }

        application.setStatus(ApplicationStatus.ACCEPTED);
        taskApplicationRepository.save(application);

        task.setStatus(TaskStatus.MATCHED);

        Booking booking = Booking.builder()
                .taskRequest(task)
                .workerProfile(application.getWorkerProfile())
                .status(BookingStatus.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);

        // Every other worker who applied is now out of the running - they
        // get rejected automatically rather than left PENDING forever with
        // no resolution.
        taskApplicationRepository.findByTaskRequestId(task.getId()).stream()
                .filter(other -> other.getStatus() == ApplicationStatus.PENDING)
                .filter(other -> !other.getId().equals(application.getId()))
                .forEach(other -> {
                    other.setStatus(ApplicationStatus.REJECTED);
                    taskApplicationRepository.save(other);
                });

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse checkIn(UUID callerId, UUID bookingId) {
        Booking booking = findOwnedByAssignedWorker(callerId, bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ForbiddenOperationException("Booking must be confirmed before checking in");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckInTime(OffsetDateTime.now());
        booking = bookingRepository.save(booking);

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse checkOut(UUID callerId, UUID bookingId) {
        Booking booking = findOwnedByAssignedWorker(callerId, bookingId);

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new ForbiddenOperationException("Booking must be checked in before checking out");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCheckOutTime(OffsetDateTime.now());
        bookingRepository.save(booking);

        // The task itself is now done, not just the booking record - this
        // is what lets a completed task disappear from "open"/"matched"
        // views and eventually feed into reviews/payouts.
        booking.getTaskRequest().setStatus(TaskStatus.COMPLETED);

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMine(UUID callerId) {
        var workerProfile = workerProfileRepository.findByUserId(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("No worker profile for this account"));

        return bookingRepository.findByWorkerProfileIdOrderByCreatedAtDesc(workerProfile.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Used by both sides of a booking - the assigned worker and the family
     * member who posted the task - to check on its status, e.g. whether
     * check-in has happened yet.
     */
    @Transactional(readOnly = true)
    public BookingResponse getByTaskId(UUID callerId, UUID taskId) {
        Booking booking = bookingRepository.findByTaskRequestId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("No booking exists for this task yet"));

        boolean isAssignedWorker = booking.getWorkerProfile().getUser().getId().equals(callerId);
        boolean isTaskPoster = booking.getTaskRequest().getPostedByUser().getId().equals(callerId);

        if (!isAssignedWorker && !isTaskPoster) {
            throw new ForbiddenOperationException("You are not part of this booking");
        }

        return toResponse(booking);
    }

    private Booking findOwnedByAssignedWorker(UUID callerId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getWorkerProfile().getUser().getId().equals(callerId)) {
            throw new ForbiddenOperationException("You are not the worker assigned to this booking");
        }
        return booking;
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getTaskRequest().getId(),
                booking.getWorkerProfile().getId(),
                booking.getStatus(),
                booking.getCheckInTime(),
                booking.getCheckOutTime(),
                booking.getCreatedAt()
        );
    }
}
