package com.event.eventscheduler.adapter.output.persistence.mapper;

import com.event.eventscheduler.adapter.output.persistence.entity.EventEntity;
import com.event.eventscheduler.domain.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventPersistenceMapper {

    @Autowired
    private RecurrenceRulePersistenceMapper recurrenceRuleMapper;


    /**
     * Converts a Domain object -> a Persistence Entity object
     * @param event
     * @return entity
     */
    public EventEntity toEntity(Event event) {
        if (event == null) return null;

        EventEntity entity = new EventEntity();
        entity.setId(event.getId());
        entity.setTitle(event.getTitle());
        entity.setStartDate(event.getStartDate());
        entity.setEndDate(event.getEndDate());

        entity.setRecurrenceRuleEntity(
                recurrenceRuleMapper.toEntity(event.getRecurrenceRule())
        );

        return entity;
    }


    /**
     * Converts a Persistance Entity object -> a Domain object
     * @param entity
     * @return
     */
    public Event toDomain(EventEntity entity) {
        if (entity == null) return null;

        Event domain = new Event();
        domain.setId(entity.getId());
        domain.setTitle(entity.getTitle());
        domain.setStartDate(entity.getStartDate());
        domain.setEndDate(entity.getEndDate());

        domain.setRecurrenceRule(
                recurrenceRuleMapper.toDomain(entity.getRecurrenceRuleEntity())
        );

        return domain;
    }


}
