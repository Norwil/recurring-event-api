package com.event.eventscheduler.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "recurrence_rule")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecurrenceRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dateOfWeek;

    private LocalDate repeatUntilDate;

    private LocalTime startTime;

    private LocalTime endTime;


}
