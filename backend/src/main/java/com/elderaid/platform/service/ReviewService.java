package com.elderaid.platform.service;

import com.elderaid.platform.domain.booking.Booking;
import com.elderaid.platform.domain.booking.BookingStatus;
import com.elderaid.platform.domain.review.Review;
import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.domain.worker.WorkerProfile;
import com.elderaid.platform.exception.ForbiddenOperationException;
import com.elderaid.platform.exception.ResourceNotFoundException;
import com.elderaid.platform.repository.BookingRepository;
import com.elderaid.platform.repository.ReviewRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.web.dto.CreateReviewRequest;
import com.elderaid.platform.web.dto.ReviewResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ReviewService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final WorkerProfileRepository workerProfileRepository;

    public ReviewService(
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository,
            WorkerProfileRepository workerProfileRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.workerProfileRepository = workerProfileRepository;
    }

    @Transactional
    public ReviewResponse createReview(UUID callerId, UUID bookingId, CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        AppUser poster = booking.getTaskRequest().getPostedByUser();
        AppUser worker = booking.getWorkerProfile().getUser();

        // Only the person who posted the task can review its booking.
        if (!poster.getId().equals(callerId)) {
            throw new ForbiddenOperationException("REVIEW_NOT_POSTER",
                    "Only the person who posted the task can review this booking");
        }

        // Reviewing only makes sense once the work is actually done.
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ForbiddenOperationException("BOOKING_NOT_COMPLETED",
                    "You can only review a completed booking");
        }

        // A booking gets exactly one review - no editing/re-rating for now.
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ForbiddenOperationException("ALREADY_REVIEWED",
                    "This booking has already been reviewed");
        }

        Review review = Review.builder()
                .booking(booking)
                .raterUser(poster)
                .ratedUser(worker)
                .rating(request.rating())
                .comment(request.comment())
                .build();
        review = reviewRepository.save(review);

        recalculateWorkerRating(booking.getWorkerProfile(), worker.getId());

        return toResponse(review);
    }

    /**
     * Recomputes the worker's average and review count directly from the
     * reviews table rather than nudging the previous stored value. The
     * reviews table is the source of truth; the columns on WorkerProfile are
     * a denormalized cache that can always be rebuilt from it.
     */
    private void recalculateWorkerRating(WorkerProfile workerProfile, UUID workerUserId) {
        BigDecimal average = reviewRepository.averageRatingForUser(workerUserId);
        long count = reviewRepository.countByRatedUserId(workerUserId);

        workerProfile.setAverageRating(
                average != null ? average.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        workerProfile.setReviewCount((int) count);
        workerProfileRepository.save(workerProfile);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getBooking().getId(),
                review.getRatedUser().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
