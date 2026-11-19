package com.bugtracker.server.service;

import com.bugtracker.server.entity.Membership;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.User;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.ForbiddenException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessCheckerTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AccessChecker accessChecker;

    private Project projectWithMember(long projectId, long userId, Role role) {
        User user = new User("kate", "kate@example.com", "h");
        ReflectionTestUtils.setField(user, "id", userId);
        Project project = new Project("Магазин", null);
        ReflectionTestUtils.setField(project, "id", projectId);
        project.addMember(user, role);
        return project;
    }

    @Test
    void returnsMembershipForMember() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(projectWithMember(10L, 1L, Role.DEVELOPER)));

        Membership membership = accessChecker.requireMember(10L, 1L);

        assertThat(membership.getRole()).isEqualTo(Role.DEVELOPER);
    }

    @Test
    void rejectsNonMember() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(projectWithMember(10L, 1L, Role.DEVELOPER)));

        assertThatThrownBy(() -> accessChecker.requireMember(10L, 2L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectsUnknownProject() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessChecker.requireMember(99L, 1L))
                .isInstanceOf(NotFoundException.class);
    }
}
