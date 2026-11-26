package com.bugtracker.server.repository;

import com.bugtracker.server.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReleaseRepository extends JpaRepository<Release, Long> {
    @Query("select r from Release r where r.project.id = :projectId")
    List<Release> findByProject(@Param("projectId") Long projectId);

    default Optional<Release> findNearestPlanned(Long projectId, Long excludeId) {
        return findPlannedByProject(projectId, excludeId).stream().findFirst();
    }

    @Query("""
            select r from Release r
            where r.project.id = :projectId
              and r.id <> :excludeId
              and r.status = com.bugtracker.server.entity.enums.ReleaseStatus.PLANNED
            order by r.plannedDate asc, r.id asc
            """)
    List<Release> findPlannedByProject(@Param("projectId") Long projectId,
                                       @Param("excludeId") Long excludeId);
}