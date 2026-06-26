package com.elderaid.platform.service;

import com.elderaid.platform.domain.booking.Booking;
import com.elderaid.platform.domain.booking.BookingStatus;
import com.elderaid.platform.domain.task.ApplicationStatus;
import com.elderaid.platform.domain.task.TaskApplication;
import com.elderaid.platform.domain.task.TaskRequest;
import com.elderaid.platform.domain.task.TaskStatus;
import com.elderaid.platform.exception.ForbiddenOperationException;
import com.elderaid.platform.repository.BookingRepository;
import com.elderaid.platform.repository.TaskApplicationRepository;
import com.elderaid.platform.web.dto.BookingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingService {

    private final TaskApplicationService taskApplicationService;
    private final TaskApplicationRepository taskApplicationRepository;
    private final BookingRepository bookingRepository;

    public BookingService(
            TaskApplicationService taskApplicationService,
            TaskApplicationRepository taskApplicationRepository,
            BookingRepository bookingRepository
    ) {
        this.taskApplicationService = taskApplicationService;
        this.taskApplicationRepository = taskApplicationRepository;
        this.bookingRepository = bookingRepository;
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
