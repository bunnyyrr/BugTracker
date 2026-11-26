package com.bugtracker.server.service.i;

import com.bugtracker.server.dto.BugQuery;
import com.bugtracker.server.dto.FileDownload;
import com.bugtracker.server.dto.FileUpload;
import com.bugtracker.server.entity.Bug;
import com.bugtracker.server.entity.Comment;
import com.bugtracker.server.entity.enums.Priority;

import java.util.List;

public interface IBugService {
    Bug createBug(Long actorId, Long projectId, String title, Priority priority, String description,
                  String stepsToReproduce, String expectedResult, String actualResult, List<FileUpload> files);

    Bug getBug(Long actorId, Long bugId);

    List<Bug> searchBugs(Long actorId, Long projectId, BugQuery query);

    Bug takeInWork(Long actorId, Long bugId);

    Bug abandonBug(Long actorId, Long bugId);

    void deleteBug(Long actorId, Long bugId);

    Bug submitForReview(Long actorId, Long bugId, List<String> urls);

    Bug closeBug(Long actorId, Long bugId, String commentText);

    Bug returnToWork(Long actorId, Long bugId, String commentText);

    Comment addComment(Long actorId, Long bugId, String text, List<FileUpload> files);

    FileDownload getAttachment(Long actorId, Long attachmentId);

    void releaseAssignedBugs(Long actorId, Long projectId, Long userId);
}
