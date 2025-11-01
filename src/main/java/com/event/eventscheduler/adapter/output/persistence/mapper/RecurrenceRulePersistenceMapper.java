package com.event.eventscheduler.adapter.output.persistence.mapper;


import com.event.eventscheduler.adapter.output.persistence.entity.RecurrenceRuleEntity;
import com.event.eventscheduler.domain.model.RecurrenceRule;
import org.springframework.stereotype.Component;

@Component
public class RecurrenceRulePersistenceMapper {

    /**
     * Converts a Domain object -> a Persistence Entity object
     */
    public RecurrenceRuleEntity toEntity(RecurrenceRule domain) {
        if (domain == null) return null;

        RecurrenceRuleEntity entity = new RecurrenceRuleEntity();
        entity.setId(domain.getId());
        entity.setDateOfWeek(domain.getDayOfWeek());
        entity.setRepeatUntilDate(domain.getRepeatUntilDate());
        entity.setStartTime(domain.getStartTime());
        entity.setEndTime(domain.getEndTime());
        return entity;
    }

    /**
     * Convers a Persistence Entity object -> a Domain object
     */
    public RecurrenceRule toDomain(RecurrenceRuleEntity entity) {
        if (entity == null) return null;

        RecurrenceRule domain = new RecurrenceRule();
        domain.setId(entity.getId());
        domain.setDayOfWeek(entity.getDateOfWeek());
        domain.setRepeatUntilDate(entity.getRepeatUntilDate());
        domain.setStartTime(entity.getStartTime());
        domain.setEndTime(entity.getEndTime());
        return domain;
    }

}
