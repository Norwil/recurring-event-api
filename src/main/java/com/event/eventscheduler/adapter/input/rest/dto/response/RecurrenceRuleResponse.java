package com.event.eventscheduler.adapter.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecurrenceRuleResponse {

    private Long id;
    private DayOfWeek dateOfWeek;
    private LocalDate repeatUntilDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
