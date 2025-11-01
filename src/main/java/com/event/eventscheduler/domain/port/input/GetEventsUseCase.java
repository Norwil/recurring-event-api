package com.event.eventscheduler.domain.port.input;

import com.event.eventscheduler.domain.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface GetEventsUseCase {
    List<Event> getEventsForDate(LocalDate date);
    List<Event> findAll();

}
