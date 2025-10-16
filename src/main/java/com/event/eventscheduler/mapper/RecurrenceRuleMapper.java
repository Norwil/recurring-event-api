package com.event.eventscheduler.mapper;

import com.event.eventscheduler.dto.request.RecurrenceRuleRequest;
import com.event.eventscheduler.dto.response.RecurrenceRuleResponse;
import com.event.eventscheduler.entity.RecurrenceRule;
import org.springframework.stereotype.Component;

@Component
public class RecurrenceRuleMapper {

    public RecurrenceRule toEntity(RecurrenceRuleRequest request) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setDateOfWeek(request.getDateOfWeek());
        rule.setRepeatUntilDate(request.getRepeatUntilDate());
        rule.setStartTime(request.getStartTime());
        rule.setEndTime(request.getEndTime());
        return rule;
    }

    public RecurrenceRuleResponse toResponse(RecurrenceRule rule) {
        RecurrenceRuleResponse response = new RecurrenceRuleResponse();
        response.setDateOfWeek(rule.getDateOfWeek());
        response.setRepeatUntilDate(rule.getRepeatUntilDate());
        response.setStartTime(rule.getStartTime());
        response.setEndTime(rule.getEndTime());
        return response;
    }
}
