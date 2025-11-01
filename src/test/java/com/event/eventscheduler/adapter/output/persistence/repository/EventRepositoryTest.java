package com.event.eventscheduler.adapter.output.persistence.repository;


import com.event.eventscheduler.AbstractIntegrationTest;
import com.event.eventscheduler.adapter.output.persistence.entity.EventEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


public class EventRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldSaveAndRetrieveEvent() {
        // Arrange
        EventEntity event = new EventEntity();
        event.setTitle("Testcontainers Demo");
        event.setStartDate(LocalDateTime.now());
        event.setEndDate(LocalDateTime.now().plusHours(1));

        // Act
        EventEntity savedEvent = eventRepository.save(event);

        EventEntity foundEvent = eventRepository.findById(savedEvent.getId()).orElse(null);

        // Assert
        assertThat(foundEvent).isNotNull();
        assertThat(foundEvent.getTitle()).isEqualTo("Testcontainers Demo");
    }

    @Test
    void shouldFindConflictingEvents() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 12, 0);

        EventEntity existingEvent = new EventEntity();
        existingEvent.setTitle("Existing Event");
        existingEvent.setStartDate(start);
        existingEvent.setEndDate(end);
        eventRepository.save(existingEvent);

        // Act
        var conflicts = eventRepository.findConflictingEvents(
                start.plusHours(1),
                end.plusHours(1)
        );

        // Assert
        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getTitle()).isEqualTo("Existing Event");
    }
}
