package com.event.eventscheduler.adapter.input.rest.mapper;

import com.event.eventscheduler.adapter.input.rest.dto.request.RecurrenceRuleRequest;
import com.event.eventscheduler.adapter.input.rest.dto.response.RecurrenceRuleResponse;
import com.event.eventscheduler.domain.model.RecurrenceRule;
import com.event.eventscheduler.domain.port.input.command.RecurrenceRuleCommand;
import org.springframework.stereotype.Component;

@Component
public class RecurrenceRuleMapper {

    public RecurrenceRule toDomain(RecurrenceRuleRequest request) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setDayOfWeek(request.getDateOfWeek());
        rule.setRepeatUntilDate(request.getRepeatUntilDate());
        rule.setStartTime(request.getStartTime());
        rule.setEndTime(request.getEndTime());
        return rule;
    }

    public RecurrenceRuleResponse toResponse(RecurrenceRule rule) {
        RecurrenceRuleResponse response = new RecurrenceRuleResponse();
        response.setId(rule.getId());
        response.setDateOfWeek(rule.getDayOfWeek());
        response.setRepeatUntilDate(rule.getRepeatUntilDate());
        response.setStartTime(rule.getStartTime());
        response.setEndTime(rule.getEndTime());
        return response;
    }

    /**
     * Converts a RecurrenceRuleRequest (DTO) -> RecurrenceRuleCommand (Domain)
     */
    public RecurrenceRuleCommand toCommand(
            RecurrenceRuleRequest request) {

        if (request == null) return null;

        RecurrenceRuleCommand command = new RecurrenceRuleCommand();
        command.setDayOfWeek(request.getDateOfWeek());
        command.setRepeatUntilDate(request.getRepeatUntilDate());
        command.setStartTime(request.getStartTime());
        command.setEndTime(request.getEndTime());
        return command;
    }
}
