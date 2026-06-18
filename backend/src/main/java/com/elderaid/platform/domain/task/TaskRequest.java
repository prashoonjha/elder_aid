package com.elderaid.platform.domain.task;

import com.elderaid.platform.domain.elderly.ElderlyProfile;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A help request posted for an elderly profile. Keep description generic -
 * avoid capturing health/mobility details here (GDPR special category data);
 * model accessibility needs as neutral task attributes instead.
 */
@Entity
@Table(name = "task_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elderly_profile_id", nullable = false)
    private ElderlyProfile elderlyProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    private AppUser postedByUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "scheduled_start", nullable = false)
    private OffsetDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private OffsetDateTime scheduledEnd;

    @Column(name = "price_offered", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceOffered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
