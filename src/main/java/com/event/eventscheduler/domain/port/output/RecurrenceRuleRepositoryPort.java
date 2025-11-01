package com.event.eventscheduler.domain.port.output;

import com.event.eventscheduler.domain.model.RecurrenceRule;

import java.util.Optional;

public interface RecurrenceRuleRepositoryPort {

    RecurrenceRule save(RecurrenceRule recurrenceRule);

    Optional<RecurrenceRule> findById(Long id);

}
