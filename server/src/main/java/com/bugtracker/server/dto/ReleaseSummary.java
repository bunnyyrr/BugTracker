package com.bugtracker.server.dto;

import com.bugtracker.server.entity.Release;

public record ReleaseSummary(Release release, int bugCount, int closedBugCount) {
}
