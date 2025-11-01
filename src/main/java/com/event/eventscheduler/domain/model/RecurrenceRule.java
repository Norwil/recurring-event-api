package com.event.eventscheduler.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenceRule {

    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalDate repeatUntilDate;
    private LocalTime startTime;
    private LocalTime endTime;

}
