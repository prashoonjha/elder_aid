package com.elderaid.platform.repository;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.task.TaskRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRequestRepository extends JpaRepository<TaskRequest, UUID> {

    List<TaskRequest> findByPostedByUserIdOrderByCreatedAtDesc(UUID postedByUserId);

    // category is optional - passing null skips that filter rather than
    // matching nothing. Only ever shows OPEN tasks scheduled in the future,
    // since there's no point surfacing something a worker can't actually take.
    @Query("""
            SELECT t FROM TaskRequest t
            WHERE t.status = 'OPEN'
            AND t.scheduledStart >= :from
            AND (:category IS NULL OR t.category = :category)
            ORDER BY t.scheduledStart ASC
            """)
    Page<TaskRequest> browseOpen(
            @Param("category") TaskCategory category,
            @Param("from") OffsetDateTime from,
            Pageable pageable
    );
}
