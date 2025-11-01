package com.event.eventscheduler.adapter.output.persistence.repository;

import com.event.eventscheduler.adapter.output.persistence.entity.RecurrenceRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurrenceRuleRepository extends JpaRepository<RecurrenceRuleEntity, Long> {
    // We don't need any custom methods for this one
}