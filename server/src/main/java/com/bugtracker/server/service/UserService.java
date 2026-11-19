package com.bugtracker.server.service;

import com.bugtracker.server.entity.User;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
