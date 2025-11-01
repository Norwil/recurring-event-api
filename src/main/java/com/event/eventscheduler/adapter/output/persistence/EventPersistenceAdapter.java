package com.event.eventscheduler.adapter.output.persistence;

import com.event.eventscheduler.adapter.output.persistence.entity.EventEntity;
import com.event.eventscheduler.adapter.output.persistence.mapper.EventPersistenceMapper;
import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.port.output.EventRepositoryPort;
import com.event.eventscheduler.adapter.output.persistence.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EventPersistenceAdapter implements EventRepositoryPort {

    private final EventRepository jpaRepository;
    private final EventPersistenceMapper mapper;

    @Autowired

    public EventPersistenceAdapter(EventRepository jpaRepository, EventPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Event save(Event event) {
        EventEntity eventEntity = mapper.toEntity(event);
        EventEntity savedEntity = jpaRepository.save(eventEntity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Event> saveAll(List<Event> events) {
        // Convert List<Domain> to List<Entity>
        List<EventEntity> entitiesToSave = events.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());

        List<EventEntity> savedEntities = jpaRepository.saveAll(entitiesToSave);

        return savedEntities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByStartDateBetween(LocalDateTime startBoundary, LocalDateTime endBoundary) {
        List<EventEntity> entities = jpaRepository.findByStartDateBetween(startBoundary, endBoundary);

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findConflictingEvents(LocalDateTime newStart, LocalDateTime newEnd) {
        List<EventEntity> entities = jpaRepository.findConflictingEvents(newStart, newEnd);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findConflictingEventsExcludingId(LocalDateTime newStart, LocalDateTime newEnd, Long eventIdToExclude) {
        List<EventEntity> entities = jpaRepository.findConflictingEventsExcludingId(newStart, newEnd, eventIdToExclude);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findAll() {
        List<EventEntity> entities = jpaRepository.findAll();
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
