package com.event.eventscheduler.service;

import com.event.eventscheduler.dto.request.CyclicEventRequest;
import com.event.eventscheduler.dto.request.EventUpdateRequest;
import com.event.eventscheduler.dto.request.RecurrenceRuleRequest;
import com.event.eventscheduler.dto.request.SingleEventRequest;
import com.event.eventscheduler.dto.response.EventResponse;
import com.event.eventscheduler.entity.Event;
import com.event.eventscheduler.entity.RecurrenceRule;
import com.event.eventscheduler.exception.ResourceNotFoundException;
import com.event.eventscheduler.exception.ScheduleConflictException;
import com.event.eventscheduler.mapper.EventMapper;
import com.event.eventscheduler.mapper.RecurrenceRuleMapper;
import com.event.eventscheduler.repository.EventRepository;
import com.event.eventscheduler.repository.RecurrenceRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private RecurrenceRuleMapper recurrenceRuleMapper;

    @Mock
    private RecurrenceRuleRepository recurrenceRuleRepository;

    private SingleEventRequest singleRequest;
    private Event eventEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);

        singleRequest = new SingleEventRequest("Test Title", start, end);
        eventEntity = new Event(1L, "Test Title", start, end, null);
    }

    @Test
    void addSingleEvent_ShouldSaveAndReturnMappedEvent() {
        // Arrange
        when(eventMapper.toEntity(singleRequest)).thenReturn(eventEntity);
        when(eventRepository.save(any(Event.class))).thenReturn(eventEntity);

        when(eventMapper.toResponse(eventEntity)).thenReturn(
                new EventResponse(
                        1L, "Test Title", eventEntity.getStartDate(), eventEntity.getEndDate(), null
                )
        );

        // Act
        eventService.addSingleEvent(singleRequest);

        // Assert
        verify(eventRepository, times(1)).save(eventEntity);

        verify(eventMapper, times(1)).toEntity(singleRequest);
        verify(eventMapper, times(1)).toResponse(eventEntity);
    }

    @Test
    void addCyclicEvent_ShouldGenerateAndSaveMultipleEvents() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate repeatUntil = today.plusWeeks(3);

        RecurrenceRuleRequest ruleRequest = new RecurrenceRuleRequest(
                today.getDayOfWeek(), repeatUntil, today.atTime(10, 0).toLocalTime(), today.atTime(11, 0).toLocalTime()
        );
        CyclicEventRequest cyclicRequest = new CyclicEventRequest(
                "Cyclic Test",
                ruleRequest.getStartTime(),
                ruleRequest.getEndTime(),
                ruleRequest
        );

        RecurrenceRule ruleEntity = new RecurrenceRule(1L, today.getDayOfWeek(), repeatUntil, today.atTime(10, 0).toLocalTime(), today.atTime(11, 0).toLocalTime());

        List<Event> generatedEvents = List.of(
                new Event(2L, "Cyclic Test", today.atTime(10, 0), today.atTime(11, 0), ruleEntity),
                new Event(3L, "Cyclic Test", today.plusWeeks(1).atTime(10, 0), today.plusWeeks(1).atTime(11, 0), ruleEntity)
        );

        // Arrange Mocks
        when(recurrenceRuleMapper.toEntity(ruleRequest)).thenReturn(ruleEntity);
        when(recurrenceRuleRepository.save(ruleEntity)).thenReturn(ruleEntity);
        when(eventRepository.saveAll(anyList())).thenReturn(generatedEvents);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(
                new EventResponse()
        );

        // Act
        eventService.addCyclicEvent(cyclicRequest);

        // Assert
        verify(recurrenceRuleRepository, times(1)).save(ruleEntity);
        verify(eventRepository, times(1)).saveAll(anyList());

        verify(eventMapper, atLeastOnce()).toResponse(any(Event.class));


    }

    @Test
    void getEventsForDate_ShouldCallRepositoryWithCorrectBoundaries() {
        // Arrange
        LocalDate targetDate = LocalDate.of(2026, 1, 1);
        LocalDateTime startOfDay = targetDate.atStartOfDay();

        List<Event> foundEvents = Collections.singletonList(eventEntity);

        when(eventRepository.findByStartDateBetween(eq(startOfDay), any(LocalDateTime.class)))
                .thenReturn(foundEvents);
        when(eventMapper.toResponse(any(Event.class)))
                .thenReturn(new EventResponse());

        // Act
        eventService.getEventsForDate(targetDate);

        // Assert
        verify(eventRepository, times(1)).findByStartDateBetween(eq(startOfDay), any(LocalDateTime.class));

        verify(eventMapper, times(1)).toResponse(any(Event.class));
    }

    @Test
    void addSingleEvent_ShouldThrow_ScheduleConflictException() {
        // Arrange
        List<Event> conflictingEvents = Collections.singletonList(
                new Event(10L, "Conflicting Meeting", LocalDateTime.now(), LocalDateTime.now().plusHours(1), null)
        );

        when(eventRepository.findByEndDateAfterAndStartDateBefore(
                singleRequest.getStartDate(),
                singleRequest.getEndDate()))
                .thenReturn(conflictingEvents);

        // Act & Assert
        assertThrows(ScheduleConflictException.class, () -> {
            eventService.addSingleEvent(singleRequest);
        });

        verify(eventRepository, never()).save(any(Event.class));

        verify(eventMapper, never()).toResponse(any(Event.class));
    }

    @Test
    void addCyclicEvent_ShouldThrow_ScheduleConflictException() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate repeatUntil = today.plusWeeks(3);

        RecurrenceRuleRequest ruleRequest = new RecurrenceRuleRequest(
                today.getDayOfWeek(), repeatUntil, today.atTime(10, 0).toLocalTime(), today.atTime(11, 0).toLocalTime()
        );
        CyclicEventRequest cyclicRequest = new CyclicEventRequest(
                "Cyclic Test", ruleRequest.getStartTime(), ruleRequest.getEndTime(), ruleRequest
        );

        RecurrenceRule ruleEntity = new RecurrenceRule(1L, today.getDayOfWeek(), repeatUntil, ruleRequest.getStartTime(), ruleRequest.getEndTime());

        List<Event> conflictingEvents = Collections.singletonList(
                new Event(10L, "Conflict", LocalDateTime.now(), LocalDateTime.now().plusHours(1), null)
        );

        // Mock
        when(recurrenceRuleMapper.toEntity(ruleRequest)).thenReturn(ruleEntity);
        when(recurrenceRuleRepository.save(ruleEntity)).thenReturn(ruleEntity);

        when(eventRepository.findByEndDateAfterAndStartDateBefore(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(conflictingEvents);

        // Act & Assert
        assertThrows(ScheduleConflictException.class, () -> {
            eventService.addCyclicEvent(cyclicRequest);
        });

        verify(eventRepository, never()).saveAll(anyList());
        verify(recurrenceRuleRepository, times(1)).save(ruleEntity);
        verify(eventRepository, atLeastOnce()).findByEndDateAfterAndStartDateBefore(any(LocalDateTime.class), any(LocalDateTime.class));

    }

    @Test
    void updateSingleEvent_ShouldUpdateEvent_WhenNoConflict() {
        // Arrange
        Long eventId = 1L;

        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setId(eventId);
        updateRequest.setTitle("Updated Title");
        updateRequest.setStartDate(LocalDateTime.now().plusDays(1));
        updateRequest.setEndDate(LocalDateTime.now().plusDays(1).plusHours(2));

        eventEntity.setId(eventId);

        Event updatedEntity = new Event(
                eventId,
                updateRequest.getTitle(),
                updateRequest.getStartDate(),
                updateRequest.getEndDate(),
                null
        );

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        when(eventRepository.findConflictingEventsExcludingId(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(eventId)))
                .thenReturn(Collections.emptyList());

        when(eventRepository.save(any(Event.class))).thenReturn(updatedEntity);

        when(eventMapper.toResponse(any(Event.class))).thenReturn(
                new EventResponse(
                        eventId, "Updated Title", updateRequest.getStartDate(), updateRequest.getEndDate(), null
                )
        );

        // Act
        eventService.updateSingleEvent(updateRequest);

        // Assert
        verify(eventRepository, times(1)).findConflictingEventsExcludingId(
                updateRequest.getStartDate(), updateRequest.getEndDate(), eventId
        );

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateSingleEvent_ShouldThrowConflictException_WhenOverlapExists() {
        // Arrange
        Long eventId = 1L;

        LocalDateTime testStart = LocalDateTime.now().plusHours(1);
        LocalDateTime testEnd = testStart.plusHours(2);

        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setId(eventId);
        updateRequest.setTitle("Updated Title");
        updateRequest.setStartDate(testStart);
        updateRequest.setEndDate(testEnd);

        eventEntity.setId(eventId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        List<Event> conflicts = Collections.singletonList(
                new Event(5L, "Existing Conflict", testStart.minusMinutes(30), testEnd.plusMinutes(30), null)
        );

        when(eventRepository.findConflictingEventsExcludingId(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(eventId)))
                .thenReturn(conflicts);

        // Act & Assert
        assertThrows(ScheduleConflictException.class, () -> {
            eventService.updateSingleEvent(updateRequest);
        });

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateSingleEvent_ShouldThrowNotFoundException_WhenEventDoesNotExist() {
        // Arrange
        Long nonExistentId = 99L;

        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setId(nonExistentId);

        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.updateSingleEvent(updateRequest);
        });

        verify(eventRepository, never()).findConflictingEventsExcludingId(any(), any(), any());
        verify(eventRepository, never()).save(any());
    }

}
