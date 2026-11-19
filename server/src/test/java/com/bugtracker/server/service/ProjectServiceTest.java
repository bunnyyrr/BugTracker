package com.bugtracker.server.service;

import com.bugtracker.server.dto.ProjectResponse;
import com.bugtracker.server.dto.ProjectSummary;
import com.bugtracker.server.entity.Membership;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.User;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.ProjectRepository;
import com.bugtracker.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccessChecker accessChecker;

    @InjectMocks
    private ProjectService projectService;

    private User userWithId(long id) {
        User user = new User("kate", "kate@example.com", "h");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void createProjectMakesCreatorTeamLead() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.createProject(1L, "  Магазин ", "   ");

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        Project saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Магазин");
        assertThat(saved.getDescription()).isNull();
        assertThat(saved.getMembership(1L)).isPresent();
        assertThat(saved.getMembership(1L).get().getRole()).isEqualTo(Role.TEAM_LEAD);
        assertThat(response.myRole()).isEqualTo(Role.TEAM_LEAD);
    }

    @Test
    void createProjectRejectsEmptyName() {
        assertThatThrownBy(() -> projectService.createProject(1L, "   ", null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Название проекта обязательно")
                .extracting("field").isEqualTo("name");
        verify(projectRepository, never()).save(any());
    }

    @Test
    void getProjectsReturnsRoleAndBugCount() {
        Project project = new Project("Магазин", "Описание");
        ReflectionTestUtils.setField(project, "id", 10L);
        project.addMember(userWithId(1L), Role.DEVELOPER);
        when(projectRepository.findByMember(1L)).thenReturn(List.of(project));
        when(projectRepository.countActiveBugs(10L)).thenReturn(3L);

        List<ProjectSummary> result = projectService.getProjects(1L);

        assertThat(result).hasSize(1);
        ProjectSummary summary = result.get(0);
        assertThat(summary.id()).isEqualTo(10L);
        assertThat(summary.role()).isEqualTo(Role.DEVELOPER);
        assertThat(summary.openBugCount()).isEqualTo(3L);
    }

    @Test
    void getProjectReturnsRoleOfMember() {
        Project project = new Project("Магазин", null);
        ReflectionTestUtils.setField(project, "id", 10L);
        Membership membership = project.addMember(userWithId(1L), Role.TESTER);
        when(accessChecker.requireMember(10L, 1L)).thenReturn(membership);

        ProjectResponse response = projectService.getProject(1L, 10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Магазин");
        assertThat(response.myRole()).isEqualTo(Role.TESTER);
    }
}
