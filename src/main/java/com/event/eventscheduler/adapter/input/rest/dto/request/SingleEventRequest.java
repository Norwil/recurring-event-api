package com.event.eventscheduler.adapter.input.rest.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SingleEventRequest {

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Please define the start date of the event")
    private LocalDateTime startDate;

    @NotNull(message = "Please define the end date of the event")
    private LocalDateTime endDate;
}
