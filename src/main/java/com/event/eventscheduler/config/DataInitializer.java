package com.event.eventscheduler.config;

import com.event.eventscheduler.adapter.output.persistence.entity.EventEntity;
import com.event.eventscheduler.adapter.output.persistence.entity.RecurrenceRuleEntity;
import com.event.eventscheduler.adapter.output.persistence.repository.EventRepository;
import com.event.eventscheduler.adapter.output.persistence.repository.RecurrenceRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final EventRepository eventRepository;
    private final RecurrenceRuleRepository recurrenceRuleRepository;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Check if the database is already populated
            if (eventRepository.count() > 0) {
                return; // Skip initialization if data exists
            }

            // --- 1. Add Single EventEntity (Fixed Appointment) ---
            addSingleEvent("Project Kickoff",
                    LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                    LocalDateTime.now().plusDays(3).withHour(11).withMinute(30));

            // --- 2. Add Cyclic EventEntity (Weekly Standup) ---
            addCyclicEvent("Weekly Team Standup",
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(9, 30),
                    LocalDate.now().plusMonths(3)); // Repeats for next 3 months

            // --- 3. Add Cyclic EventEntity (Forever EventEntity for Conflict Testing) ---
            addCyclicEvent("Lunch Break Conflict",
                    DayOfWeek.FRIDAY,
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    null); // Repeat forever (capped by service logic)

            System.out.println("Database initialization complete. Total events generated: " + eventRepository.count());
        };
    }

    // Helper method to create and save a single event
    private void addSingleEvent(String title, LocalDateTime start, LocalDateTime end) {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setTitle(title);
        eventEntity.setStartDate(start);
        eventEntity.setEndDate(end);
        eventEntity.setRecurrenceRuleEntity(null);
        eventRepository.save(eventEntity);
    }

    // Helper method to create rule and generate initial events
    private void addCyclicEvent(String title, DayOfWeek day, LocalTime start, LocalTime end, LocalDate repeatUntil) {
        RecurrenceRuleEntity rule = new RecurrenceRuleEntity();
        rule.setDateOfWeek(day);
        rule.setStartTime(start);
        rule.setEndTime(end);
        rule.setRepeatUntilDate(repeatUntil);

        rule = recurrenceRuleRepository.save(rule);

        // Simulate the recurrence logic (similar to EventService.generateEventsFromRule)
        List<EventEntity> eventEntities = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate endExclusive = repeatUntil != null ? repeatUntil.plusDays(1) : today.plusMonths(3);

        int count = 0;
        final int MAX_INIT_EVENTS = 5; // Generate only a few initial eventEntities for quick setup

        for (LocalDate date = today; date.isBefore(endExclusive) && count < MAX_INIT_EVENTS; date = date.plusDays(1)) {
            if (date.getDayOfWeek() == day) {
                eventEntities.add(new EventEntity(null, title, LocalDateTime.of(date, start), LocalDateTime.of(date, end), rule));
                count++;
            }
        }

        eventRepository.saveAll(eventEntities);
    }
}