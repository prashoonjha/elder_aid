package com.elderaid.platform.repository;

import com.elderaid.platform.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookingId(UUID bookingId);

    Optional<Review> findByBookingId(UUID bookingId);

    // Recalculated straight from the reviews table so the stored average on
    // WorkerProfile is always a true recomputation, never an incrementally
    // nudged value that could drift out of sync.
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.ratedUser.id = :ratedUserId")
    BigDecimal averageRatingForUser(@Param("ratedUserId") UUID ratedUserId);

    long countByRatedUserId(UUID ratedUserId);
}
