package com.event.eventscheduler.domain.port.input.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CreateCyclicEventCommand {
    private String title;
    private RecurrenceRuleCommand recurrenceRule;
}
