package com.bugtracker.server.repository;

import com.bugtracker.server.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select distinct p from Project p join p.memberships m where m.user.id = :userId order by p.id")
    List<Project> findByMember(@Param("userId") Long userId);

    @Query(value = "select count(*) from bug where project_id = :projectId and status <> 'CLOSED'", nativeQuery = true)
    long countActiveBugs(@Param("projectId") Long projectId);
}
