package com.elderaid.platform.repository;

import com.elderaid.platform.domain.booking.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByTaskRequestId(UUID taskRequestId);
}
