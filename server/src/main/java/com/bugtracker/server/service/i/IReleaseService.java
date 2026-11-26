package com.bugtracker.server.service.i;

import com.bugtracker.server.dto.PublishPreview;
import com.bugtracker.server.dto.ReleaseSummary;
import com.bugtracker.server.entity.Bug;
import com.bugtracker.server.entity.Release;

import java.time.Instant;
import java.util.List;

public interface IReleaseService {
    List<ReleaseSummary> getReleases(Long actorId, Long projectId);

    Release getRelease(Long actorId, Long releaseId);

    Release createRelease(Long actorId, Long projectId, String name, Instant plannedDate);

    Release changeDate(Long actorId, Long releaseId, Instant newDate);

    List<Bug> getAvailableBugs(Long actorId, Long releaseId);

    Release addBugs(Long actorId, Long releaseId, List<Long> bugIds);

    Release removeBugs(Long actorId, Long releaseId, List<Long> bugIds);

    PublishPreview getPublishPreview(Long actorId, Long releaseId);

    Release publish(Long actorId, Long releaseId);

    void deleteRelease(Long actorId, Long releaseId);
}
