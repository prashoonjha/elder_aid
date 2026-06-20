package com.elderaid.platform.repository;

import com.elderaid.platform.domain.elderly.ElderlyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ElderlyProfileRepository extends JpaRepository<ElderlyProfile, UUID> {
}
