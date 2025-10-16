package com.event.eventscheduler.mapper;


import com.event.eventscheduler.dto.request.SingleEventRequest;
import com.event.eventscheduler.dto.response.EventResponse;
import com.event.eventscheduler.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final RecurrenceRuleMapper recurrenceRuleMapper;

    /**
     * Convert SingleEventRequest -> Entity
     * @param request
     * @return event
     */
    public Event toEntity(SingleEventRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setRecurrenceRule(null);

        return event;
    }

    public EventResponse toResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setRuleId(event.getRecurrenceRule().getId());

        return response;
    }

}
