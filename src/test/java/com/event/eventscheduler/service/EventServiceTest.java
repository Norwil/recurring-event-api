package com.event.eventscheduler.service;

import com.event.eventscheduler.dto.request.CyclicEventRequest;
import com.event.eventscheduler.dto.request.RecurrenceRuleRequest;
import com.event.eventscheduler.dto.request.SingleEventRequest;
import com.event.eventscheduler.dto.response.EventResponse;
import com.event.eventscheduler.entity.Event;
import com.event.eventscheduler.entity.RecurrenceRule;
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

}
