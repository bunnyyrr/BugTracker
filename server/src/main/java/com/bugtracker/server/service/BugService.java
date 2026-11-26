package com.bugtracker.server.service;

import com.bugtracker.server.dto.BugQuery;
import com.bugtracker.server.dto.FileDownload;
import com.bugtracker.server.dto.FileUpload;
import com.bugtracker.server.entity.*;
import com.bugtracker.server.entity.enums.Priority;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.ConflictException;
import com.bugtracker.server.exception.ForbiddenException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.BugRepository;
import com.bugtracker.server.repository.ProjectRepository;
import com.bugtracker.server.repository.UserRepository;

import com.bugtracker.server.storage.FileStorage;

import com.bugtracker.server.service.i.IBugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BugService implements IBugService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/gif", "image/webp", "text/plain", "application/pdf");

    private final BugRepository bugRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AccessChecker accessChecker;
    private final FileStorage fileStorage;

    @Override
    @Transactional
    public Bug createBug(Long actorId, Long projectId, String title, Priority priority, String description, String stepsToReproduce, String expectedResult, String actualResult, List<FileUpload> files)
    {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TESTER, Role.TEAM_LEAD));
        User author = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (title == null || title.isBlank()) {
            throw new ValidationException("Введите название", "title");
        }
        if (priority == null) {
            throw new ValidationException("Выберите приоритет", "priority");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("Введите описание", "description");
        }
        if (stepsToReproduce == null || stepsToReproduce.isBlank()) {
            throw new ValidationException("Введите шаги воспроизведения", "stepsToReproduce");
        }
        List<FileUpload> uploads = files == null ? List.of() : files;
        for (FileUpload file : uploads) {
            if (!ALLOWED_CONTENT_TYPES.contains(file.contentType())) {
                throw new ValidationException("Недопустимый тип файла: " + file.fileName(), "files");
            }
        }
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        Bug bug = new Bug(project, author, title, priority, description, stepsToReproduce, expectedResult, actualResult);
        for (FileUpload file : uploads) {
            String storageKey = fileStorage.save(file);
            bug.addAttachment(new Attachment(file.fileName(), file.contentType(), file.getSize(), storageKey));
        }
        return bugRepository.save(bug);
    }

    @Override
    @Transactional(readOnly = true)
    public Bug getBug(Long actorId, Long bugId) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        accessChecker.requireMember(bug.getProject().getId(), actorId);
        return bug;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bug> searchBugs(Long actorId, Long projectId, BugQuery query) {
        accessChecker.requireMember(projectId, actorId);
        return bugRepository.search(projectId, actorId, query);
    }

    @Override
    @Transactional
    public Bug takeInWork(Long actorId, Long bugId) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        accessChecker.requireRole(bug.getProject().getId(), actorId, List.of(Role.DEVELOPER, Role.TEAM_LEAD));
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        bug.takeInWork(actor);
        return bugRepository.save(bug);
    }

    @Override
    @Transactional
    public Bug abandonBug(Long actorId, Long bugId) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        User assignee = bug.getAssignee();
        if (assignee == null || !assignee.getId().equals(actorId)) {
            throw new ForbiddenException("Отказаться от бага может только ответственный");
        }
        bug.abandon(assignee);
        return bugRepository.save(bug);
    }

    @Override
    @Transactional
    public void deleteBug(Long actorId, Long bugId) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        accessChecker.requireAuthorOrLead(bug, actorId);
        if (bug.isClosed()) {
            throw new ConflictException("Закрытый баг нельзя удалить");
        }
        for (Attachment attachment : bug.getAttachments()) {
            fileStorage.delete(attachment.getStorageKey());
        }
        for (Comment comment : bug.getComments()) {
            for (Attachment attachment : comment.getAttachments()) {
                fileStorage.delete(attachment.getStorageKey());
            }
        }
        bugRepository.delete(bug);
    }

    @Override
    @Transactional
    public Bug submitForReview(Long actorId, Long bugId, List<String> urls) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        User assignee = bug.getAssignee();
        if (assignee == null || !assignee.getId().equals(actorId)) {
            throw new ForbiddenException("Передать на проверку может только ответственный");
        }
        bug.submitForReview(assignee, urls);
        return bugRepository.save(bug);
    }

    @Override
    @Transactional
    public Bug closeBug(Long actorId, Long bugId, String commentText) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        accessChecker.requireReviewer(bug, actorId);
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        bug.close(actor, commentText);
        return bugRepository.save(bug);
    }

    @Override
    @Transactional
    public Bug returnToWork(Long actorId, Long bugId, String commentText) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        accessChecker.requireReviewer(bug, actorId);
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        bug.returnToWork(actor, commentText);
        return bugRepository.save(bug);
    }

    @Override
    @Transactional
    public Comment addComment(Long actorId, Long bugId, String text, List<FileUpload> files) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new NotFoundException("Баг не найден"));
        accessChecker.requireMember(bug.getProject().getId(), actorId);
        List<FileUpload> uploads = files == null ? List.of() : files;
        if ((text == null || text.isBlank()) && uploads.isEmpty()) {
            throw new ValidationException("Введите текст комментария или приложите файл", "text");
        }
        for (FileUpload file : uploads) {
            if (!ALLOWED_CONTENT_TYPES.contains(file.contentType())) {
                throw new ValidationException("Недопустимый тип файла: " + file.fileName(), "files");
            }
        }
        User author = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Comment comment = bug.addComment(author, text == null ? "" : text);
        for (FileUpload file : uploads) {
            String storageKey = fileStorage.save(file);
            comment.addAttachment(new Attachment(file.fileName(), file.contentType(), file.getSize(), storageKey));
        }
        bugRepository.saveAndFlush(bug);
        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownload getAttachment(Long actorId, Long attachmentId) {
        Bug bug = bugRepository.findByAttachmentId(attachmentId).orElseThrow(() -> new NotFoundException("Вложение не найдено"));
        accessChecker.requireMember(bug.getProject().getId(), actorId);
        Attachment attachment = Stream.concat(
                        bug.getAttachments().stream(),
                        bug.getComments().stream().flatMap(comment -> comment.getAttachments().stream()))
                .filter(found -> found.getId().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Вложение не найдено"));
        byte[] content = fileStorage.open(attachment.getStorageKey());
        return new FileDownload(attachment.getFileName(), attachment.getContentType(), content);
    }

    @Override
    @Transactional
    public void releaseAssignedBugs(Long actorId, Long projectId, Long userId) {
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        for (Bug bug : bugRepository.findInProgressByAssignee(projectId, userId)) {
            bug.unassign(actor);
            bugRepository.save(bug);
        }
    }
}
