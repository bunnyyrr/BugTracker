package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.HistoryEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "history_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bug_id", nullable = false)
    private Bug bug;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    private HistoryEvent event;

    @Column(name = "field")
    private String field;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    HistoryEntry(Bug bug, User changedBy, HistoryEvent event, String field, String oldValue, String newValue) {
        this.bug = bug;
        this.changedBy = changedBy;
        this.event = event;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = Instant.now();
    }
}