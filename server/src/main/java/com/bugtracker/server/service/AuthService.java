package com.bugtracker.server.service;

import com.bugtracker.server.entity.User;
import com.bugtracker.server.exception.UnauthorizedException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.PasswordHasher;
import com.bugtracker.server.service.i.TokenProvider;
import com.bugtracker.server.service.i.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    public User register(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Введите имя пользователя", "username");
        }
        if (email == null || email.isBlank()) {
            throw new ValidationException("Введите email", "email");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Введите пароль", "password");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ValidationException("Имя пользователя уже занято", "username");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email уже используется", "email");
        }
        String hash = passwordHasher.hash(password);
        return userRepository.save(new User(username, email, hash));
    }

    @Override
    @Transactional(readOnly = true)
    public String login(String login, String password) {
        User user = userRepository.findByLogin(login).orElse(null);
        if (user == null || user.isDeleted()) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }
        if (!passwordHasher.verify(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }
        return tokenProvider.issue(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticate(String token) {
        Long userId = tokenProvider.validate(token).orElseThrow(() -> new UnauthorizedException("Требуется вход"));
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("Требуется вход"));
        if (user.isDeleted()) {
            throw new UnauthorizedException("Требуется вход");
        }
        return user;
    }
}
