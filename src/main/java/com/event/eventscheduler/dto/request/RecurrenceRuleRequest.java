package com.event.eventscheduler.dto.request;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class RecurrenceRuleRequest {

    @Enumerated(EnumType.STRING)
    private DayOfWeek dateOfWeek;

    private LocalDate repeatUntilDate;

    private LocalTime startTime;

    private LocalTime endTime;
}
