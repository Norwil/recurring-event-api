package com.event.eventscheduler.repository;

import com.event.eventscheduler.entity.RecurrenceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurrenceRuleRepository extends JpaRepository<RecurrenceRule, Long> {
}
