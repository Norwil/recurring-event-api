package com.event.eventscheduler.domain.port.input.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateSingleEventCommand {
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
