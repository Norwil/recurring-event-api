package com.event.eventscheduler.adapter.controller;

import com.event.eventscheduler.adapter.input.rest.controller.EventController;
import com.event.eventscheduler.adapter.input.rest.dto.request.EventUpdateRequest;
import com.event.eventscheduler.adapter.input.rest.dto.request.SingleEventRequest;
import com.event.eventscheduler.adapter.input.rest.dto.response.EventResponse;
import com.event.eventscheduler.adapter.input.rest.mapper.EventMapper;
import com.event.eventscheduler.adapter.input.rest.exception.GlobalExceptionHandler;
import com.event.eventscheduler.domain.exception.ResourceNotFoundException;
import com.event.eventscheduler.domain.exception.ScheduleConflictException;
import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.port.input.CreateCyclicEventUseCase;
import com.event.eventscheduler.domain.port.input.CreateEventUseCase;
import com.event.eventscheduler.domain.port.input.GetEventsUseCase;
import com.event.eventscheduler.domain.port.input.UpdateEventUseCase;
import com.event.eventscheduler.domain.port.input.command.CreateSingleEventCommand;
import com.event.eventscheduler.domain.port.input.command.UpdateEventCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({EventController.class, GlobalExceptionHandler.class})
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CreateEventUseCase createEventUseCase;
    @MockitoBean private CreateCyclicEventUseCase createCyclicEventUseCase;
    @MockitoBean private GetEventsUseCase getEventUseCase;
    @MockitoBean private UpdateEventUseCase updateEventUseCase;
    @MockitoBean private EventMapper eventMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // Test Data
    private SingleEventRequest singleRequest;
    private CreateSingleEventCommand createCommand;
    private Event domainEvent;
    private EventResponse eventResponse;
    private LocalDateTime testStart;
    private LocalDateTime testEnd;

    @BeforeEach
    void setUp() {
        testStart = LocalDateTime.of(2025, 11, 10, 10, 0);
        testEnd = testStart.plusHours(1);

        // DTO Request
        singleRequest = new SingleEventRequest("Team Sync", testStart, testEnd);
        // Command
        createCommand = new CreateSingleEventCommand("Team Sync", testStart, testEnd);
        // Domain Model
        domainEvent = new Event();
        domainEvent.setId(5L);
        domainEvent.setTitle("Team Sync");
        domainEvent.setStartDate(testStart);
        domainEvent.setEndDate(testEnd);
        // DTO Response
        eventResponse = new EventResponse(5L, "Team Sync", testStart, testEnd, null);
    }

    @Test
    void addSingleEvent_ShouldReturn_201Created_OnSuccess() throws Exception {
        // Arrange
        // Mock Mapper (DTO -> Command)
        when(eventMapper.toSingleCommand(any(SingleEventRequest.class))).thenReturn(createCommand);
        // Mock Use Case (Command -> Domain Model
        when(createEventUseCase.createSingleEvent(any(CreateSingleEventCommand.class))).thenReturn(domainEvent);
        // Mock Mapper (Domain Model -> DTO)
        when(eventMapper.toResponse(any(Event.class))).thenReturn(eventResponse);

        // Act & Assert
        mockMvc.perform(post("/api/events/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(singleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("Team Sync"));

        // Verify the new flow
        verify(eventMapper, times(1)).toSingleCommand(any(SingleEventRequest.class));
        verify(createEventUseCase, times(1)).createSingleEvent(any(CreateSingleEventCommand.class));
        verify(eventMapper, times(1)).toResponse(any(Event.class));
    }

    @Test
    void addSingleEvent_ShouldReturn_409Conflict_OnOverlap() throws Exception {
        // Arrange
        // Mock Mapper (DTO -> Command)
        when(eventMapper.toSingleCommand(any(SingleEventRequest.class))).thenReturn(createCommand);
        // Mock use Case (throws the exception)
        when(createEventUseCase.createSingleEvent(any(CreateSingleEventCommand.class)))
                .thenThrow(new ScheduleConflictException("Overlap"));

        // Act & Assert
        mockMvc.perform(post("/api/events/single")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(singleRequest)))
                .andExpect(status().isConflict())
                .andExpect(status().is(409));

        // Verify
        verify(createEventUseCase, times(1)).createSingleEvent(any(CreateSingleEventCommand.class));
        verify(eventMapper, never()).toResponse(any(Event.class));
    }

    @Test
    void updateSingleEvent_ShouldReturn_200Ok_OnSuccess() throws Exception {
        // Arrange
        Long eventId = 5L;
        EventUpdateRequest updateRequest = new EventUpdateRequest(eventId, "Updated Title", testStart, testEnd);
        UpdateEventCommand updateCommand = new UpdateEventCommand(eventId, "Updated Title", testStart, testEnd);
        Event updatedDomainEvent = new Event(eventId, "Updated Title", testStart, testEnd, null);
        EventResponse updatedResponse = new EventResponse(eventId, "Updated Title", testStart, testEnd, null);

        // Mock Mapper (DTO -> Command)
        when(eventMapper.toUpdateCommand(any(EventUpdateRequest.class))).thenReturn(updateCommand);
        when(updateEventUseCase.updateEvent(any(UpdateEventCommand.class))).thenReturn(updatedDomainEvent);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(updateEventUseCase, times(1)).updateEvent(any(UpdateEventCommand.class));
    }

    @Test
    void updateSingleEvent_ShouldReturn_404NotFound_WhenIdMissing() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        EventUpdateRequest updateRequest = new EventUpdateRequest(nonExistentId, "Title", testStart, testEnd);
        UpdateEventCommand updateCommand = new UpdateEventCommand(nonExistentId, "Title", testStart, testEnd);

        // Mock Mapper (DTO -> Command)
        when(eventMapper.toUpdateCommand(any(EventUpdateRequest.class))).thenReturn(updateCommand);
        // Mock use Case (Throws the exception)
        when(updateEventUseCase.updateEvent(any(UpdateEventCommand.class)))
                .thenThrow(new ResourceNotFoundException("Event not found."));

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(status().is(404));
    }

    @Test
    void getEventsForDate_ShouldReturn_200Ok_AndExpectedList() throws Exception {
        // Arrange
        LocalDate queryDate = LocalDate.of(2025, 11, 10);
        List<Event> domainList = Collections.singletonList(domainEvent);

        // MOck Use Case
        when(getEventUseCase.getEventsForDate(queryDate)).thenReturn(domainList);
        // Mock Mapper
        when(eventMapper.toResponse(any(Event.class))).thenReturn(eventResponse);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("date", queryDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Team Sync"));

        verify(getEventUseCase, times(1)).getEventsForDate(queryDate);
        verify(eventMapper, times(1)).toResponse(any(Event.class));
    }

}


