package com.event.eventscheduler.repository;

import com.event.eventscheduler.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartDateBetween(LocalDateTime startBoundary, LocalDateTime endBoundary);
}
