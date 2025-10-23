package com.event.eventscheduler.controller;

import com.event.eventscheduler.dto.request.SingleEventRequest;
import com.event.eventscheduler.dto.request.EventUpdateRequest;
import com.event.eventscheduler.dto.response.EventResponse;
import com.event.eventscheduler.exception.GlobalExceptionHandler;
import com.event.eventscheduler.exception.ResourceNotFoundException;
import com.event.eventscheduler.exception.ScheduleConflictException;
import com.event.eventscheduler.service.EventService;
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

    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Test Data Setup ---
    private SingleEventRequest singleRequest;
    private EventResponse eventResponse;
    private LocalDateTime testStart;
    private LocalDateTime testEnd;

    @BeforeEach
    void setUp() {
        testStart = LocalDateTime.of(2025, 11, 10, 10, 0);
        testEnd = testStart.plusHours(1);

        singleRequest = new SingleEventRequest("Team Sync", testStart, testEnd);
        eventResponse = new EventResponse(5L, "Team Sync", testStart, testEnd, null);
    }

    // -------------------------------------------------------------------------------------------------
    // 1. POST /api/events/single - Add Single Event
    // -------------------------------------------------------------------------------------------------

    @Test
    void addSingleEvent_ShouldReturn_201Created_OnSuccess() throws Exception {
        // Arrange
        when(eventService.addSingleEvent(any(SingleEventRequest.class))).thenReturn(eventResponse);

        // Act & Assert
        mockMvc.perform(post("/api/events/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(singleRequest)))
                .andExpect(status().isCreated()) // Expect HTTP 201
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("Team Sync"));

        verify(eventService, times(1)).addSingleEvent(any(SingleEventRequest.class));
    }

    @Test
    void addSingleEvent_ShouldReturn_409Conflict_OnOverlap() throws Exception {
        // Arrange
        when(eventService.addSingleEvent(any(SingleEventRequest.class))).thenThrow(new ScheduleConflictException("Overlap"));

        // Act & Assert
        mockMvc.perform(post("/api/events/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(singleRequest)))
                .andExpect(status().isConflict())
                .andExpect(status().is(409));

        verify(eventService, times(1)).addSingleEvent(any(SingleEventRequest.class));
    }

    // -------------------------------------------------------------------------------------------------
    // 2. PUT /api/events/{id} - Update Single Event
    // -------------------------------------------------------------------------------------------------

    @Test
    void updateSingleEvent_ShouldReturn_200Ok_OnSuccess() throws Exception {
        // Arrange
        Long eventId = 5L;
        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setId(eventId);
        updateRequest.setTitle("Updated Project Review");
        updateRequest.setStartDate(testStart.plusDays(1));
        updateRequest.setEndDate(testEnd.plusDays(1));

        EventResponse updatedResponse = new EventResponse(eventId, "Updated Project Review", updateRequest.getStartDate(), updateRequest.getEndDate(), null);

        when(eventService.updateSingleEvent(any(EventUpdateRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk()) // Expect HTTP 200
                .andExpect(jsonPath("$.title").value("Updated Project Review"));

        verify(eventService, times(1)).updateSingleEvent(any(EventUpdateRequest.class));
    }

    @Test
    void updateSingleEvent_ShouldReturn_404NotFound_WhenIdMissing() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        EventUpdateRequest updateRequest = new EventUpdateRequest();
        updateRequest.setId(nonExistentId);
        updateRequest.setTitle("Test");
        updateRequest.setStartDate(testStart);
        updateRequest.setEndDate(testEnd);

        when(eventService.updateSingleEvent(any(EventUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Event not found."));

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound()) // Expect HTTP 404
                .andExpect(status().is(404));

        verify(eventService, times(1)).updateSingleEvent(any(EventUpdateRequest.class));
    }

    @Test
    void updateSingleEvent_ShouldReturn_400BadRequest_OnIdMismatch() throws Exception {
        // Arrange
        Long pathId = 5L;
        Long bodyId = 10L;
        EventUpdateRequest updateRequest = new EventUpdateRequest(bodyId, "Title", testStart, testEnd);

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", pathId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path ID must match request body ID."));

        verify(eventService, never()).updateSingleEvent(any());
    }

    // -------------------------------------------------------------------------------------------------
    // 3. GET /api/events?date=... - Query by Date
    // -------------------------------------------------------------------------------------------------

    @Test
    void getEventsForDate_ShouldReturn_200Ok_AndExpectedList() throws Exception {
        // Arrange
        LocalDate queryDate = LocalDate.of(20025, 11, 10);
        List<EventResponse> mockList = Collections.singletonList(eventResponse);

        when(eventService.getEventsForDate(queryDate)).thenReturn(mockList);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                    .param("date", queryDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Team Sync"));

        verify(eventService, times(1)).getEventsForDate(queryDate);
    }

    // -------------------------------------------------------------------------------------------------
    // 4. GET /api/events/all - Find All
    // -------------------------------------------------------------------------------------------------

    @Test
    void findAll_ShouldReturn_200Ok() throws Exception {
        // Arrange
        when(eventService.findAll()).thenReturn(Collections.singletonList(eventResponse));

        // Act & Assert
        mockMvc.perform(get("/api/events/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // -------------------------------------------------------------------------------------------------
    // 5. POST /api/events/cyclic - Cyclic Events (Success Only Example)
    // -------------------------------------------------------------------------------------------------

    @Test
    void addCyclicEvent_ShouldReturn_201Created() throws Exception {
        // Arrange: Skip generating the complex DTO/Rule, and focus only on the JSON structure
        String cyclicJson = """
                {
                    "title": "Weekly Standup",
                    "startTime": "09:00:00",
                    "endTime": "09:30:00",
                    "recurrenceRule": {
                        "dateOfWeek": "MONDAY",
                        "repeatUntilDate": "2026-01-01",
                        "startTime": "09:00:00",
                        "endTime": "09:30:00"
                    }
                }
            """;

        // Mock the service
        when(eventService.addCyclicEvent(any())).thenReturn(Collections.singletonList(eventResponse));

        // Act & Assert
        mockMvc.perform(post("/api/events/cyclic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cyclicJson))
                .andExpect(status().isCreated())    // Expect HTTP 201
                .andExpect(jsonPath("$[0].title").value("Team Sync"));
    }
}


