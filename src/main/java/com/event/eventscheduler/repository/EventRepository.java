package com.event.eventscheduler.repository;

import com.event.eventscheduler.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartDateBetween(LocalDateTime startBoundary, LocalDateTime endBoundary);

    List<Event> findByEndDateAfterAndStartDateBefore(
            LocalDateTime newStart,
            LocalDateTime newEnd
    );

    @Query("SELECT e FROM Event e WHERE " +
            "(e.endDate > :newStart AND e.startDate < :newEnd) " +
            "AND e.id != :eventIdToExclude")
    List<Event> findConflictingEventsExcludingId(
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("eventIdToExclude") Long eventIdToExclude
    );
}
