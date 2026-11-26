package com.bugtracker.server.dto;

import com.bugtracker.server.entity.Bug;
import com.bugtracker.server.entity.Release;

import java.util.List;

public record PublishPreview(List<Bug> unclosedBugs, Release nextRelease) {
}
