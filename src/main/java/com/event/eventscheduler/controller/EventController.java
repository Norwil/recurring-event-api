package com.event.eventscheduler.controller;

import com.event.eventscheduler.dto.request.CyclicEventRequest;
import com.event.eventscheduler.dto.request.EventUpdateRequest;
import com.event.eventscheduler.dto.request.SingleEventRequest;
import com.event.eventscheduler.dto.response.EventResponse;
import com.event.eventscheduler.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/single")
    public ResponseEntity<EventResponse> addSingleEvent(@RequestBody SingleEventRequest request) {
        EventResponse response = eventService.addSingleEvent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/cyclic")
    public ResponseEntity<List<EventResponse>> addCyclicEvent(@RequestBody CyclicEventRequest request) {
        List<EventResponse> responses = eventService.addCyclicEvent(request);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEventsForDate(
            @RequestParam("date") LocalDate date) {

        List<EventResponse> responses = eventService.getEventsForDate(date);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponse>> findAll() {
        List<EventResponse> response = eventService.findAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateSingleEvent(
            @PathVariable Long id,
            @RequestBody EventUpdateRequest request) {
        if (!id.equals(request.getId())) {
            throw new IllegalArgumentException("Path ID must match request body ID.");
        }

        EventResponse response = eventService.updateSingleEvent(request);
        return ResponseEntity.ok(response);
    }

}
