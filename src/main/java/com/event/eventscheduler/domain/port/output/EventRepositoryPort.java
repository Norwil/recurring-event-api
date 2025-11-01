package com.event.eventscheduler.domain.port.output;

import com.event.eventscheduler.domain.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepositoryPort {

    Event save(Event event);

    Optional<Event> findById(Long id);

    List<Event> saveAll(List<Event> events);

    List<Event> findByStartDateBetween(LocalDateTime startBoundary, LocalDateTime endBoundary);

    List<Event> findConflictingEvents(LocalDateTime newStart, LocalDateTime newEnd);

    List<Event> findConflictingEventsExcludingId(LocalDateTime newStart, LocalDateTime newEnd, Long eventIdToExclude);

    List<Event> findAll();
}