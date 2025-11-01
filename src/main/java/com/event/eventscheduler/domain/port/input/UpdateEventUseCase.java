package com.event.eventscheduler.domain.port.input;

import com.event.eventscheduler.domain.model.Event;
import com.event.eventscheduler.domain.port.input.command.UpdateEventCommand;

public interface UpdateEventUseCase {

    Event updateEvent(UpdateEventCommand command);
}
