package com.bugtracker.server.service;

import com.bugtracker.server.entity.User;
import com.bugtracker.server.exception.UnauthorizedException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.PasswordHasher;
import com.bugtracker.server.service.i.TokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public User register(String username, String email, String password) {
        String name = trim(username);
        String mail = trim(email);
        if (name.isEmpty()) {
            throw new ValidationException("Не все поля заполнены", "username");
        }
        if (mail.isEmpty()) {
            throw new ValidationException("Не все поля заполнены", "email");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Не все поля заполнены", "password");
        }
        if (userRepository.findByUsernameIgnoreCase(name).isPresent()) {
            throw new ValidationException("Имя пользователя уже занято", "username");
        }
        if (userRepository.findByEmailIgnoreCase(mail).isPresent()) {
            throw new ValidationException("Email уже используется", "email");
        }
        return userRepository.save(new User(name, mail, passwordHasher.hash(password)));
    }

    public String login(String login, String password) {
        String identifier = trim(login);
        User user = identifier.isEmpty() ? null : userRepository.findByLogin(identifier).orElse(null);
        if (user == null || user.isDeleted() || password == null || !passwordHasher.verify(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }
        return tokenProvider.issue(user.getId());
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public User authenticate(String token) {
        Long userId = tokenProvider.validate(token)
                .orElseThrow(() -> new UnauthorizedException("Токен неверный или просрочен"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));
        if (user.isDeleted()) {
            throw new UnauthorizedException("Профиль удалён");
        }
        return user;
    }
}