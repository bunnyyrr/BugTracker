package com.bugtracker.server.service;

import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.User;
import com.bugtracker.server.exception.ConflictException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.ProjectRepository;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.PasswordHasher;
import com.bugtracker.server.service.i.IBugService;
import com.bugtracker.server.service.i.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordHasher passwordHasher;
    private final IBugService bugService;

    @Override
    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String username, String email, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (username == null || username.isBlank()) {
            throw new ValidationException("Введите имя пользователя", "username");
        }
        if (email == null || email.isBlank()) {
            throw new ValidationException("Введите email", "email");
        }
        userRepository.findByUsername(username)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new ValidationException("Имя пользователя уже занято", "username");
                });
        userRepository.findByEmail(email)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new ValidationException("Email уже используется", "email");
                });
        user.changeUsername(username);
        user.changeEmail(email);
        if (newPassword != null && !newPassword.isBlank()) {
            user.changePassword(passwordHasher.hash(newPassword));
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteProfile(Long userId) {
        List<Project> projects = projectRepository.findByMember(userId);
        for (Project project : projects) {
            if (project.isOnlyTeamLead(userId)) {
                throw new ConflictException("Вы единственный тимлид в проекте");
            }
        }
        for (Project project : projects) {
            bugService.releaseAssignedBugs(userId, project.getId(), userId);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        user.anonymize();
        userRepository.save(user);
    }
}
