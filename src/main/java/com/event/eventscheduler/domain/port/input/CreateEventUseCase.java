package com.event.eventscheduler.domain.port.input;

import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.port.input.command.CreateSingleEventCommand;

public interface CreateEventUseCase {

    Event createSingleEvent(CreateSingleEventCommand command);
}
