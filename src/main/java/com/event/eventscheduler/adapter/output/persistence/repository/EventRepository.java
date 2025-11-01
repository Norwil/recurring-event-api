package com.event.eventscheduler.adapter.output.persistence.repository;

import com.event.eventscheduler.adapter.output.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    /**
     * This is a custom query that Spring Data automatically creates
     * just from the method name.
     */
    List<EventEntity> findByStartDateBetween(LocalDateTime startBoundary, LocalDateTime endBoundary);

    /**
     * This is a complex query, so we provide the JPQL (JPA Query Language)
     * ourselves using @Query.
     */
    @Query("SELECT e FROM EventEntity e WHERE e.endDate > :newStart AND e.startDate < :newEnd")
    List<EventEntity> findConflictingEvents(
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );

    /**
     * This is the other complex query for checking conflicts during an update.
     */
    @Query("SELECT e FROM EventEntity e WHERE " +
            "(e.endDate > :newStart AND e.startDate < :newEnd) " +
            "AND e.id != :eventIdToExclude")
    List<EventEntity> findConflictingEventsExcludingId(
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("eventIdToExclude") Long eventIdToExclude
    );
}