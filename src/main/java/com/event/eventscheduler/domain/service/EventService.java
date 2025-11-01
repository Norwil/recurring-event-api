package com.event.eventscheduler.domain.service;



import com.event.eventscheduler.domain.exception.ResourceNotFoundException;
import com.event.eventscheduler.domain.exception.ScheduleConflictException;
import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.model.RecurrenceRule;
import com.event.eventscheduler.domain.port.input.CreateCyclicEventUseCase;
import com.event.eventscheduler.domain.port.input.CreateEventUseCase;
import com.event.eventscheduler.domain.port.input.GetEventsUseCase;
import com.event.eventscheduler.domain.port.input.UpdateEventUseCase;
import com.event.eventscheduler.domain.port.input.command.CreateCyclicEventCommand;
import com.event.eventscheduler.domain.port.input.command.CreateSingleEventCommand;
import com.event.eventscheduler.domain.port.input.command.UpdateEventCommand;
import com.event.eventscheduler.domain.port.output.EventRepositoryPort;
import com.event.eventscheduler.domain.port.output.RecurrenceRuleRepositoryPort;
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
public class EventService implements CreateEventUseCase, UpdateEventUseCase, CreateCyclicEventUseCase, GetEventsUseCase {

    private final EventRepositoryPort eventRepositoryPort;
    private final RecurrenceRuleRepositoryPort recurrenceRuleRepositoryPort;

    @Override
    @Transactional
    public Event createSingleEvent(CreateSingleEventCommand command) {
        // Check for conflicts using the PORT
        checkForConflict(command.getStartDate(), command.getEndDate(), null);

        // Map Command (domain) -> Model (domain)
        Event event = new Event();
        event.setTitle(command.getTitle());
        event.setStartDate(command.getStartDate());
        event.setEndDate(command.getEndDate());
        event.setRecurrenceRule(null);  // It's a single event

        return eventRepositoryPort.save(event);
    }

    @Override
    @Transactional
    public List<Event> createCyclicEvent(CreateCyclicEventCommand command) {
        // 1. Map Command (domain) -> Model (domain)
        RecurrenceRule rule = new RecurrenceRule();
        rule.setDayOfWeek(command.getRecurrenceRule().getDayOfWeek());
        rule.setRepeatUntilDate(command.getRecurrenceRule().getRepeatUntilDate());
        rule.setStartTime(command.getRecurrenceRule().getStartTime());
        rule.setEndTime(command.getRecurrenceRule().getEndTime());

        // 2. Save the rule using the PORT
        RecurrenceRule savedRule = recurrenceRuleRepositoryPort.save(rule);

        // 3. Generate DOMAIN MODELS (not entities)
        List<Event> events = generateEventsFromRule(savedRule, command.getTitle());

        // 4. Check for conflicts
        for (Event event : events) {
            checkForConflict(event.getStartDate(), event.getEndDate(), null);
        }

        // 5. Save all using the PORT
        return eventRepositoryPort.saveAll(events);
    }

    @Override
    @Transactional
    public Event updateEvent(UpdateEventCommand command) {
        // Find the existing DOMAIN MODEL using the PORT
        Event existingEvent = eventRepositoryPort.findById(command.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + command.getId()));

        if (existingEvent.getRecurrenceRule() != null) {
            throw new IllegalArgumentException("Cannot update a recurring event using this method.");
        }

        checkForConflict(command.getStartDate(), command.getEndDate(), command.getId());

        existingEvent.setTitle(command.getTitle());
        existingEvent.setStartDate(command.getStartDate());
        existingEvent.setEndDate(command.getEndDate());

        return eventRepositoryPort.save(existingEvent);
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

            if (date.getDayOfWeek() == rule.getDayOfWeek()) {
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
    public List<Event> getEventsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return eventRepositoryPort.findByStartDateBetween(startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepositoryPort.findAll();
    }



    @Transactional(readOnly = true)
    protected void checkForConflict(LocalDateTime newStart, LocalDateTime newEnd, Long eventIdToExclude) {
        List<Event> conflicts;

        if (eventIdToExclude != null) {
            conflicts = eventRepositoryPort.findConflictingEventsExcludingId(newStart, newEnd, eventIdToExclude);
        } else {
            // New event check
            conflicts = eventRepositoryPort.findConflictingEvents(newStart, newEnd);
        }

        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("Schedule conflict detected.");
        }
    }
}
