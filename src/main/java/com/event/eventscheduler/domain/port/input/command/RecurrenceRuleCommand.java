package com.event.eventscheduler.domain.port.input.command;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class RecurrenceRuleCommand {

    private DayOfWeek dayOfWeek;
    private LocalDate repeatUntilDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
