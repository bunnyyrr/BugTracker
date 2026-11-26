package com.bugtracker.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id")
    private Bug bug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    public Attachment(String fileName, String contentType, long sizeBytes, String storageKey) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
    }

    void bindTo(Bug bug) {
        ensureUnbound();
        this.bug = bug;
    }

    void bindTo(Comment comment) {
        ensureUnbound();
        this.comment = comment;
    }

    private void ensureUnbound() {
        if (bug != null || comment != null) {
            throw new IllegalStateException("Вложение уже принадлежит багу или комментарию");
        }
    }
}