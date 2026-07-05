package com.elderaid.platform.web.review;

import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.ReviewService;
import com.elderaid.platform.web.dto.CreateReviewRequest;
import com.elderaid.platform.web.dto.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Only clients and family members post tasks, so only they can review a
    // booking. The finer-grained check (that the caller is the actual poster
    // of this specific booking's task) lives in the service.
    @PostMapping("/api/bookings/{bookingId}/review")
    @PreAuthorize("hasAnyRole('CLIENT', 'FAMILY_MEMBER')")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal CurrentUser caller,
            @PathVariable UUID bookingId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(caller.id(), bookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
