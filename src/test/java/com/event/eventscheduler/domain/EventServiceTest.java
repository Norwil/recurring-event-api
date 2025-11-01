package com.event.eventscheduler.domain;

import com.event.eventscheduler.domain.exception.ResourceNotFoundException;
import com.event.eventscheduler.domain.exception.ScheduleConflictException;
import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.model.RecurrenceRule;
import com.event.eventscheduler.domain.port.input.command.CreateCyclicEventCommand;
import com.event.eventscheduler.domain.port.input.command.CreateSingleEventCommand;
import com.event.eventscheduler.domain.port.input.command.RecurrenceRuleCommand;
import com.event.eventscheduler.domain.port.input.command.UpdateEventCommand;
import com.event.eventscheduler.domain.port.output.EventRepositoryPort;
import com.event.eventscheduler.domain.port.output.RecurrenceRuleRepositoryPort;
import com.event.eventscheduler.domain.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    @Mock
    private RecurrenceRuleRepositoryPort recurrenceRuleRepositoryPort;

    // Our test data will be DOMAIN objects, not DTOs or Entities
    private CreateSingleEventCommand createCommand;
    private Event event;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);

        createCommand = new CreateSingleEventCommand("Test Title", start, end);

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Title");
        event.setStartDate(start);
        event.setEndDate(end);
    }

    @Test
    void createSingleEvent_ShouldSaveAndReturnEvent() {
        // Arrange
        // Mock the conflict check: "find no conflicts"
        when(eventRepositoryPort.findConflictingEvents(any(), any()))
                .thenReturn(Collections.emptyList());

        // Mock the save call: "return the saved event"
        when(eventRepositoryPort.save(any(Event.class)))
                .thenReturn(event);

        // Act
        // We call the USE CASE method, passing a COMMAND
        Event result = eventService.createSingleEvent(createCommand);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Title", result.getTitle());

        // Verify the service called the ports correctly
        verify(eventRepositoryPort, times(1)).findConflictingEvents(createCommand.getStartDate(), createCommand.getEndDate());
        verify(eventRepositoryPort, times(1)).save(any(Event.class));
    }

    @Test
    void createSingleEvent_ShouldThrowScheduleConflictException() {
        // Arrange
        // Mock the conflict check: "find ONE conflict
        when(eventRepositoryPort.findConflictingEvents(any(), any()))
                .thenReturn(List.of(new Event()));      // Just return a non-empty list

        // Act & Assert
        assertThrows(ScheduleConflictException.class, () -> {
            eventService.createSingleEvent(createCommand);
        });

        // Verify the service *never* tried to save
        verify(eventRepositoryPort, never()).save(any(Event.class));
    }

    @Test
    void updateEvent_ShouldUpdateEvent_WhenNoConflict() {
        // Arrange
        UpdateEventCommand updateCommand = new UpdateEventCommand(1L, "Updated Title", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        // Mock finding the event
        when(eventRepositoryPort.findById(1L)).thenReturn(Optional.of(event));

        // Mock the conflict check (no conflicts)
        when(eventRepositoryPort.findConflictingEventsExcludingId(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Mock the save
        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(i -> i.getArgument(0)); // Return the saved event

        // Act
        Event result = eventService.updateEvent(updateCommand);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());

        verify(eventRepositoryPort, times(1)).findById(1L);
        verify(eventRepositoryPort, times(1)).findConflictingEventsExcludingId(any(), any(), eq(1L));
        verify(eventRepositoryPort, times(1)).save(any(Event.class));
    }

    @Test
    void updateEvent_ShouldThrowNotFoundException_WhenEventDoesNotExist() {
        // Arrange
        UpdateEventCommand updateCommand = new UpdateEventCommand(99L, "Updated Title",LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        // Mock finding the event
        when(eventRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.updateEvent(updateCommand);
        });

        // Verify we never checked for conflicts or saved
        verify(eventRepositoryPort, never()).findConflictingEventsExcludingId(any(), any(), anyLong());
        verify(eventRepositoryPort, never()).save(any(Event.class));
    }

    @Test
    void createCyclicEvent_ShouldGenerateAndSaveMultipleEvents() {
        // Arrange
        RecurrenceRuleCommand ruleCommand = new RecurrenceRuleCommand(DayOfWeek.MONDAY, LocalDate.now().plusWeeks(1), LocalTime.NOON, LocalTime.NOON.plusHours(1));
        CreateCyclicEventCommand cyclicCommand = new CreateCyclicEventCommand("Cyclic Test", ruleCommand);

        RecurrenceRule savedRule = new RecurrenceRule(1L, ruleCommand.getDayOfWeek(), ruleCommand.getRepeatUntilDate(), ruleCommand.getStartTime(), ruleCommand.getEndTime());

        // Mock saving the rule
        when(recurrenceRuleRepositoryPort.save(any(RecurrenceRule.class))).thenReturn(savedRule);

        // Mock conflict check
        when(eventRepositoryPort.findConflictingEvents(any(), any())).thenReturn(Collections.emptyList());

        // Mock saving all events
        when(eventRepositoryPort.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        // Act
        List<Event> results = eventService.createCyclicEvent(cyclicCommand);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("Cyclic Test", results.get(0).getTitle());

        verify(recurrenceRuleRepositoryPort, times(1)).save(any(RecurrenceRule.class));
        verify(eventRepositoryPort, atLeastOnce()).findConflictingEvents(any(), any());
        verify(eventRepositoryPort, times(1)).saveAll(anyList());
    }

    @Test
    void getEventsForDate_ShouldCallPort() {
        // Arrange
        LocalDate targetDate = LocalDate.now();
        when(eventRepositoryPort.findByStartDateBetween(any(), any())).thenReturn(List.of(event));

        // Act
        List<Event> results = eventService.getEventsForDate(targetDate);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(eventRepositoryPort, times(1)).findByStartDateBetween(targetDate.atStartOfDay(), targetDate.atTime(LocalTime.MAX));
    }
}
