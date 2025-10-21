package com.event.eventscheduler.service;

import com.event.eventscheduler.dto.request.CyclicEventRequest;
import com.event.eventscheduler.dto.request.EventUpdateRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RecurrenceRuleRepository recurrenceRuleRepository;
    private final RecurrenceRuleMapper recurrenceRuleMapper;

    @Transactional
    public EventResponse addSingleEvent(SingleEventRequest request) {
        checkForConflict(request.getStartDate(), request.getEndDate(), null);

        Event eventToAdd = eventMapper.toEntity(request);
        Event savedEvent = eventRepository.save(eventToAdd);

        return eventMapper.toResponse(savedEvent);
    }

    @Transactional
    public List<EventResponse> addCyclicEvent(CyclicEventRequest request) {
        RecurrenceRule rule = recurrenceRuleMapper.toEntity(request.getRecurrenceRule());
        rule = recurrenceRuleRepository.save(rule);

        List<Event> events = generateEventsFromRule(rule, request.getTitle());

        for (Event event : events) {
            checkForConflict(event.getStartDate(), event.getEndDate(), null);
        }

        List<Event> savedEvents = eventRepository.saveAll(events);

        return savedEvents.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    private List<Event> generateEventsFromRule(RecurrenceRule rule, String title) {
        List<Event> events = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate endExclusive = rule.getRepeatUntilDate() != null
                ? rule.getRepeatUntilDate().plusDays(1)
                : today.plusYears(1);

        int eventCount = 0;
        final int MAX_EVENTS = 1000;

        for (LocalDate date = today;
             date.isBefore(endExclusive) && eventCount < MAX_EVENTS;
             date = date.plusDays(1)) {

            if (date.getDayOfWeek() == rule.getDateOfWeek()) {
                if (rule.getRepeatUntilDate() != null && date.isAfter(rule.getRepeatUntilDate())) {
                    break;
                }

                LocalDateTime startDateTime = LocalDateTime.of(date, rule.getStartTime());
                LocalDateTime endDateTime = LocalDateTime.of(date, rule.getEndTime());

                Event event = new Event();
                event.setTitle(title);
                event.setStartDate(startDateTime);
                event.setEndDate(endDateTime);
                event.setRecurrenceRule(rule);

                events.add(event);
                eventCount++;
            }
        }

        return events;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Event> events = eventRepository.findByStartDateBetween(startOfDay, endOfDay);

        return events.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findAll() {
        List<Event> events = eventRepository.findAll();

        return events.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateSingleEvent(EventUpdateRequest request) {
        Event existingEvent = eventRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + request.getId()));

        if (existingEvent.getRecurrenceRule() != null) {
            throw new IllegalArgumentException("Cannot update a recurring event using the single event endpoint.");
        }

        checkForConflict(request.getStartDate(), request.getEndDate(), request.getId());

        // Apply Updates
        existingEvent.setTitle(request.getTitle());
        existingEvent.setStartDate(request.getStartDate());
        existingEvent.setEndDate(request.getEndDate());

        Event updatedEvent = eventRepository.save(existingEvent);

        return eventMapper.toResponse(updatedEvent);
    }

    @Transactional(readOnly = true)
    protected void checkForConflict(LocalDateTime newStart, LocalDateTime newEnd, Long eventIdToExclude) {
        List<Event> conflicts;

        if (eventIdToExclude != null) {
            conflicts = eventRepository.findConflictingEventsExcludingId(newStart, newEnd, eventIdToExclude);
        } else {
            // New event check
            conflicts = eventRepository.findByEndDateAfterAndStartDateBefore(newStart, newEnd);
        }

        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("Schedule conflict detected. The proposed event time overlaps with existing appointments.");
        }
    }
}
