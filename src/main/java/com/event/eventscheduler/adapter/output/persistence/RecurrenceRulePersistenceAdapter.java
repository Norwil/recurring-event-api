package com.event.eventscheduler.adapter.output.persistence;

import com.event.eventscheduler.adapter.output.persistence.entity.RecurrenceRuleEntity;
import com.event.eventscheduler.adapter.output.persistence.mapper.RecurrenceRulePersistenceMapper;
import com.event.eventscheduler.adapter.output.persistence.repository.RecurrenceRuleRepository;
import com.event.eventscheduler.domain.model.RecurrenceRule;
import com.event.eventscheduler.domain.port.output.RecurrenceRuleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RecurrenceRulePersistenceAdapter implements RecurrenceRuleRepositoryPort {

    private final RecurrenceRuleRepository jpaRepository;
    private final RecurrenceRulePersistenceMapper mapper;

    @Override
    public RecurrenceRule save(RecurrenceRule recurrenceRule) {
        RecurrenceRuleEntity entity = mapper.toEntity(recurrenceRule);

        RecurrenceRuleEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RecurrenceRule> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}
