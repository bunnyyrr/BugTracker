package com.bugtracker.server.repository;

import com.bugtracker.server.dto.BugQuery;
import com.bugtracker.server.entity.Bug;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BugRepository extends JpaRepository<Bug, Long> {

    @Query("select b from Bug b where b.id in :ids")
    List<Bug> findByIds(@Param("ids") List<Long> ids);

    @Query("""
            select b from Bug b
            where exists (select a.id from Attachment a where a.id = :attachmentId and a.bug = b)
               or exists (select a.id from Attachment a join a.comment c where a.id = :attachmentId and c.bug = b)
            """)
    Optional<Bug> findByAttachmentId(@Param("attachmentId") Long attachmentId);

    default List<Bug> search(Long projectId, Long userId, BugQuery query) {
        String text = query.text() == null ? "" : query.text().trim();
        Pageable page = PageRequest.of(query.page() - 1, query.pageSize());
        return switch (query.filter()) {
            case ACTUAL -> searchActual(projectId, text, page);
            case MINE -> searchMine(projectId, userId, text, page);
            case CLOSED -> searchClosed(projectId, text, page);
        };
    }

    @Query("""
            select b from Bug b
            where b.project.id = :projectId
              and b.status <> com.bugtracker.server.entity.enums.BugStatus.CLOSED
              and (lower(b.title) like lower(concat('%', :text, '%')) or cast(b.id as string) = :text)
            order by b.createdAt desc, b.id desc
            """)
    List<Bug> searchActual(@Param("projectId") Long projectId,
                           @Param("text") String text,
                           Pageable pageable);

    @Query("""
            select b from Bug b
            where b.project.id = :projectId
              and (b.author.id = :userId or b.assignee.id = :userId)
              and (lower(b.title) like lower(concat('%', :text, '%')) or cast(b.id as string) = :text)
            order by b.createdAt desc, b.id desc
            """)
    List<Bug> searchMine(@Param("projectId") Long projectId,
                         @Param("userId") Long userId,
                         @Param("text") String text,
                         Pageable pageable);

    @Query("""
            select b from Bug b
            where b.project.id = :projectId
              and b.status = com.bugtracker.server.entity.enums.BugStatus.CLOSED
              and (lower(b.title) like lower(concat('%', :text, '%')) or cast(b.id as string) = :text)
            order by b.createdAt desc, b.id desc
            """)
    List<Bug> searchClosed(@Param("projectId") Long projectId,
                           @Param("text") String text,
                           Pageable pageable);

    @Query("""
            select b from Bug b
            where b.project.id = :projectId
              and b.release is null
              and b.status <> com.bugtracker.server.entity.enums.BugStatus.CLOSED
            """)
    List<Bug> findWithoutRelease(@Param("projectId") Long projectId);

    @Query("""
            select b from Bug b
            where b.project.id = :projectId
              and b.assignee.id = :userId
              and b.status = com.bugtracker.server.entity.enums.BugStatus.IN_PROGRESS
            """)
    List<Bug> findInProgressByAssignee(@Param("projectId") Long projectId,
                                       @Param("userId") Long userId);

    @Query("""
            select count(b) from Bug b
            where b.project.id = :projectId
              and b.status <> com.bugtracker.server.entity.enums.BugStatus.CLOSED
            """)
    int countActive(@Param("projectId") Long projectId);
}