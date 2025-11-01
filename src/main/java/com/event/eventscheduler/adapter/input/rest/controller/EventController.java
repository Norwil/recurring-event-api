package com.event.eventscheduler.adapter.input.rest.controller;

// DTOs (Web)
import com.event.eventscheduler.adapter.input.rest.dto.request.CyclicEventRequest;
import com.event.eventscheduler.adapter.input.rest.dto.request.EventUpdateRequest;
import com.event.eventscheduler.adapter.input.rest.dto.request.SingleEventRequest;
import com.event.eventscheduler.adapter.input.rest.dto.response.EventResponse;

// Mappers (Web Adapter)
import com.event.eventscheduler.adapter.input.rest.mapper.EventMapper;

// Commands (Domain)
import com.event.eventscheduler.domain.port.input.command.CreateCyclicEventCommand;
import com.event.eventscheduler.domain.port.input.command.CreateSingleEventCommand;
import com.event.eventscheduler.domain.port.input.command.UpdateEventCommand;

// Use Cases (Domain Ports)
import com.event.eventscheduler.domain.port.input.CreateCyclicEventUseCase;
import com.event.eventscheduler.domain.port.input.CreateEventUseCase;
import com.event.eventscheduler.domain.port.input.GetEventsUseCase;
import com.event.eventscheduler.domain.port.input.UpdateEventUseCase;

// Domain Model
import com.event.eventscheduler.domain.model.Event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final CreateCyclicEventUseCase createCyclicEventUseCase;
    private final GetEventsUseCase getEventsUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final EventMapper eventMapper;

    @PostMapping("/single")
    public ResponseEntity<EventResponse> addSingleEvent(@Valid @RequestBody SingleEventRequest request) {
        // Map DTO -> Command
        CreateSingleEventCommand command = eventMapper.toSingleCommand(request);

        // Call the Use Case
        Event domainEvent = createEventUseCase.createSingleEvent(command);

        // Map Domain -> DTO
        EventResponse response = eventMapper.toResponse(domainEvent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/cyclic")
    public ResponseEntity<List<EventResponse>> addCyclicEvent(@Valid @RequestBody CyclicEventRequest request) {
        // Map DTO -> Command
        CreateCyclicEventCommand command = eventMapper.toCyclicCommand(request);

        // Call the Use Case
        List<Event> domainEvents = createCyclicEventUseCase.createCyclicEvent(command);

        // Map List<Domain> -> List<DTO>
        List<EventResponse> responses = domainEvents.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());

        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEventsForDate(
            @RequestParam("date") LocalDate date) {
        // Call the Use Case
        List<Event> domainEvents = getEventsUseCase.getEventsForDate(date);

        // Map List<Domain> -> List<DTO>
        List<EventResponse> responses = domainEvents.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponse>> findAll() {
        // Call the Use Case
        List<Event> domainEvents = getEventsUseCase.findAll();

        // Map List<Domain> -> List<DTO>
        List<EventResponse> responses = domainEvents.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateSingleEvent(
            @PathVariable Long id,
           @Valid @RequestBody EventUpdateRequest request) {

        if (!id.equals(request.getId())) {
            throw new IllegalArgumentException("Path ID must match request body ID.");
        }

        // Map DTO -> Command
        UpdateEventCommand command = eventMapper.toUpdateCommand(request);

        // Call the Use Case
        Event domainEvent = updateEventUseCase.updateEvent(command);

        // Map Domain -> DTO
        EventResponse response = eventMapper.toResponse(domainEvent);
        return ResponseEntity.ok(response);
    }

}
