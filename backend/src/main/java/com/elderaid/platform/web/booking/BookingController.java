package com.elderaid.platform.web.booking;

import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.BookingService;
import com.elderaid.platform.web.dto.BookingResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PatchMapping("/api/bookings/{bookingId}/check-in")
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse checkIn(@AuthenticationPrincipal CurrentUser caller, @PathVariable UUID bookingId) {
        return bookingService.checkIn(caller.id(), bookingId);
    }

    @PatchMapping("/api/bookings/{bookingId}/check-out")
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse checkOut(@AuthenticationPrincipal CurrentUser caller, @PathVariable UUID bookingId) {
        return bookingService.checkOut(caller.id(), bookingId);
    }

    @GetMapping("/api/bookings/mine")
    @PreAuthorize("hasRole('WORKER')")
    public List<BookingResponse> mine(@AuthenticationPrincipal CurrentUser caller) {
        return bookingService.listMine(caller.id());
    }

    // No role restriction here - either the assigned worker or the family
    // member who posted the task may look this up; the service enforces
    // that the caller is actually one of those two people.
    @GetMapping("/api/tasks/{taskId}/booking")
    public BookingResponse getByTask(@AuthenticationPrincipal CurrentUser caller, @PathVariable UUID taskId) {
        return bookingService.getByTaskId(caller.id(), taskId);
    }
}
