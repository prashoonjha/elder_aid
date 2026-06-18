package com.elderaid.platform.domain.elderly;

import com.elderaid.platform.domain.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Links a family member's account to an elderly profile they manage,
 * with a relationship label and a permission level controlling whether
 * they can only view bookings or also create/pay for them.
 */
@Entity
@Table(name = "family_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_user_id", nullable = false)
    private AppUser familyUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elderly_profile_id", nullable = false)
    private ElderlyProfile elderlyProfile;

    @Column(nullable = false)
    private String relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level", nullable = false)
    @Builder.Default
    private PermissionLevel permissionLevel = PermissionLevel.FULL;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
