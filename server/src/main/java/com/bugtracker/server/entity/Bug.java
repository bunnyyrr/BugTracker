package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.BugStatus;
import com.bugtracker.server.entity.enums.HistoryEvent;
import com.bugtracker.server.entity.enums.LinkType;
import com.bugtracker.server.entity.enums.Priority;
import com.bugtracker.server.exception.ConflictException;
import com.bugtracker.server.exception.ValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "bug")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "steps_to_reproduce", nullable = false, columnDefinition = "text")
    private String stepsToReproduce;

    @Column(name = "expected_result", columnDefinition = "text")
    private String expectedResult;

    @Column(name = "actual_result", columnDefinition = "text")
    private String actualResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BugStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id")
    private Release release;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "bug", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<Comment> comments = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "bug", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Attachment> attachments = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "bug", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CommitLink> commitLinks = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "bug", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC, id ASC")
    private List<HistoryEntry> history = new ArrayList<>();

    public Bug(Project project, User author, String title, Priority priority, String description, String stepsToReproduce, String expectedResult, String actualResult) {
        this.project = project;
        this.author = author;
        this.title = title;
        this.priority = priority;
        this.description = description;
        this.stepsToReproduce = stepsToReproduce;
        this.expectedResult = expectedResult;
        this.actualResult = actualResult;
        this.status = BugStatus.NEW;
        this.assignee = null;
        this.createdAt = Instant.now();
        recordHistory(author, HistoryEvent.CREATED, null, null, null);
    }

    public Instant getDeadline() {
        return release == null ? null : release.getPlannedDate();
    }

    public boolean isClosed() {
        return status == BugStatus.CLOSED;
    }

    public void takeInWork(User actor) {
        if (status != BugStatus.NEW) {
            throw new ConflictException("Баг недоступен");
        }
        this.status = BugStatus.IN_PROGRESS;
        this.assignee = actor;
        recordHistory(actor, HistoryEvent.TAKEN_IN_WORK, null, null, null);
    }

    public void abandon(User actor) {
        if (status != BugStatus.IN_PROGRESS) {
            throw new ConflictException("Отказаться можно только от бага в работе");
        }
        this.status = BugStatus.NEW;
        this.assignee = null;
        recordHistory(actor, HistoryEvent.ABANDONED, null, null, null);
    }

    public void unassign(User actor) {
        this.status = BugStatus.NEW;
        this.assignee = null;
        recordHistory(actor, HistoryEvent.ASSIGNEE_RELEASED, null, null, null);
    }

    public void submitForReview(User actor, List<String> urls) {
        if (status != BugStatus.IN_PROGRESS) {
            throw new ConflictException("Передать на проверку можно только баг в работе");
        }
        if (urls != null) {
            for (String url : urls) {
                addCommitLink(url);
            }
        }
        if (commitLinks.isEmpty()) {
            throw new ValidationException("Добавьте хотя бы одну ссылку на коммит или merge request");
        }
        this.status = BugStatus.IN_REVIEW;
        recordHistory(actor, HistoryEvent.SUBMITTED_FOR_REVIEW, null, null, null);
    }

    public Comment returnToWork(User actor, String commentText) {
        if (commentText == null || commentText.isBlank()) {
            throw new ValidationException("Комментарий обязателен при возврате на доработку", "comment");
        }
        if (status != BugStatus.IN_REVIEW) {
            throw new ConflictException("Вернуть на доработку можно только баг на проверке");
        }
        this.status = BugStatus.IN_PROGRESS;
        Comment comment = addComment(actor, commentText);
        recordHistory(actor, HistoryEvent.RETURNED_TO_WORK, null, null, null);
        return comment;
    }

    public Comment close(User actor, String commentText) {
        if (status != BugStatus.IN_REVIEW) {
            throw new ConflictException("Закрыть можно только баг на проверке");
        }
        this.status = BugStatus.CLOSED;
        recordHistory(actor, HistoryEvent.CLOSED, null, null, null);
        if (commentText == null || commentText.isBlank()) {
            return null;
        }
        return addComment(actor, commentText);
    }

    public Comment addComment(User author, String text) {
        Comment comment = new Comment(this, author, text);
        comments.add(comment);
        return comment;
    }

    public void addAttachment(Attachment attachment) {
        attachment.bindTo(this);
        attachments.add(attachment);
    }

    void setRelease(Release release) {
        this.release = release;
    }

    private CommitLink addCommitLink(String url) {
        LinkType type = CommitLink.detectType(url);
        CommitLink link = new CommitLink(this, type, url);
        commitLinks.add(link);
        return link;
    }

    void recordHistory(User actor, HistoryEvent event, String field, String oldValue, String newValue) {
        history.add(new HistoryEntry(this, actor, event, field, oldValue, newValue));
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public List<Attachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public List<CommitLink> getCommitLinks() {
        return Collections.unmodifiableList(commitLinks);
    }

    public List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }
}