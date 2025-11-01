package com.event.eventscheduler.adapter.output.persistence.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Title is required.")
    private String title;

    @NotNull(message = "Please define the start date of the event")
    private LocalDateTime startDate;

    @NotNull(message = "Please define the end date of the event")
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = true)
    private RecurrenceRuleEntity recurrenceRuleEntity;
}
