package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.LinkType;
import com.bugtracker.server.exception.ValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;

@Entity
@Table(name = "commit_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommitLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LinkType type;

    @Column(name = "url", nullable = false, columnDefinition = "text")
    private String url;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bug_id", nullable = false)
    private Bug bug;

    CommitLink(Bug bug, LinkType type, String url) {
        this.bug = bug;
        this.type = type;
        this.url = url;
    }

    public static LinkType detectType(String url) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new ValidationException("Адрес ссылки не распознан");
        }
        String path;
        try {
            path = URI.create(url).getPath();
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Адрес ссылки не распознан");
        }
        if (path == null) {
            throw new ValidationException("Адрес ссылки не распознан");
        }
        if (path.contains("/merge_requests/") || path.contains("/pull/")) {
            return LinkType.MERGE_REQUEST;
        }
        if (path.contains("/commit/") || path.contains("/commits/")) {
            return LinkType.COMMIT;
        }
        throw new ValidationException("Адрес ссылки не распознан");
    }
}