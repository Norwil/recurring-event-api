package com.event.eventscheduler.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CyclicEventRequest {

    @NotNull(message = "Title is required.")
    private String title;

    @NotNull(message = "Please define the start date of the event")
    private LocalTime startTime;

    @NotNull(message = "Please define the end date of the event")
    private LocalTime endTime;

    private RecurrenceRuleRequest recurrenceRule;

}
