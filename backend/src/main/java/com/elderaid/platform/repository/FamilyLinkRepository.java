package com.elderaid.platform.repository;

import com.elderaid.platform.domain.elderly.FamilyLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FamilyLinkRepository extends JpaRepository<FamilyLink, UUID> {

    List<FamilyLink> findByFamilyUserId(UUID familyUserId);

    Optional<FamilyLink> findByFamilyUserIdAndElderlyProfileId(UUID familyUserId, UUID elderlyProfileId);
}
