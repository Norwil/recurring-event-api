package com.event.eventscheduler.adapter.input.rest.mapper;


import com.event.eventscheduler.adapter.input.rest.dto.request.CyclicEventRequest;
import com.event.eventscheduler.adapter.input.rest.dto.request.EventUpdateRequest;
import com.event.eventscheduler.adapter.input.rest.dto.request.SingleEventRequest;
import com.event.eventscheduler.adapter.input.rest.dto.response.EventResponse;
import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.port.input.command.CreateCyclicEventCommand;
import com.event.eventscheduler.domain.port.input.command.CreateSingleEventCommand;
import com.event.eventscheduler.domain.port.input.command.UpdateEventCommand;
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
    public Event toDomain(SingleEventRequest request) {
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

        Long ruleId = (event.getRecurrenceRule() != null)
                ? event.getRecurrenceRule().getId()
                : null;

        response.setRuleId(ruleId);

        return response;
    }

    /**
     * Converts a SingleEventRequest (DTO) -> CreateSingleEventCommand (Domain)
     */
    public CreateSingleEventCommand toSingleCommand(
            SingleEventRequest request) {
        CreateSingleEventCommand command = new CreateSingleEventCommand();

        command.setTitle(request.getTitle());
        command.setStartDate(request.getStartDate());
        command.setEndDate(request.getEndDate());
        return command;
    }

    /**
     * Converts an EventUpdateRequest (DTO) -> UpdateEventCommand (Domain)
     */
    public UpdateEventCommand toUpdateCommand(
            EventUpdateRequest request) {
        UpdateEventCommand command = new UpdateEventCommand();
        command.setId(request.getId());
        command.setTitle(request.getTitle());
        command.setStartDate(request.getStartDate());
        command.setEndDate(request.getEndDate());
        return command;
    }

    /**
     * Converts a CyclicEventRequest (DTO) -> CreateCyclicEventCommand (Domain)
     */
    public CreateCyclicEventCommand toCyclicCommand(
            CyclicEventRequest request) {
        CreateCyclicEventCommand command = new CreateCyclicEventCommand();
        command.setTitle(request.getTitle());
        command.setRecurrenceRule(
                recurrenceRuleMapper.toCommand(request.getRecurrenceRule())
        );
        return command;
    }

}
