package com.event.eventscheduler.domain.port.input;

import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.port.input.command.CreateCyclicEventCommand;

import java.util.List;

public interface CreateCyclicEventUseCase {
    List<Event> createCyclicEvent(CreateCyclicEventCommand command);
}
