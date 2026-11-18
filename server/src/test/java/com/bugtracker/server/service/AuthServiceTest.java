package com.bugtracker.server.service;

import com.bugtracker.server.entity.User;
import com.bugtracker.server.exception.UnauthorizedException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.PasswordHasher;
import com.bugtracker.server.service.i.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSavesUserWithHashedPassword() {
        when(userRepository.findByUsernameIgnoreCase("kate")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("kate@example.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = authService.register(" kate ", "kate@example.com", "secret");

        assertThat(saved.getUsername()).isEqualTo("kate");
        assertThat(saved.getEmail()).isEqualTo("kate@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void registerRejectsTakenUsername() {
        when(userRepository.findByUsernameIgnoreCase("kate"))
                .thenReturn(Optional.of(new User("kate", "old@example.com", "h")));

        assertThatThrownBy(() -> authService.register("kate", "kate@example.com", "secret"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Имя пользователя уже занято")
                .extracting("field").isEqualTo("username");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsTakenEmail() {
        when(userRepository.findByUsernameIgnoreCase("kate")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("kate@example.com"))
                .thenReturn(Optional.of(new User("other", "kate@example.com", "h")));

        assertThatThrownBy(() -> authService.register("kate", "kate@example.com", "secret"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Email уже используется")
                .extracting("field").isEqualTo("email");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsEmptyField() {
        assertThatThrownBy(() -> authService.register("  ", "kate@example.com", "secret"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Не все поля заполнены")
                .extracting("field").isEqualTo("username");
    }

    @Test
    void loginReturnsToken() {
        User user = new User("kate", "kate@example.com", "hashed");
        when(userRepository.findByLogin("kate")).thenReturn(Optional.of(user));
        when(passwordHasher.verify("secret", "hashed")).thenReturn(true);
        when(tokenProvider.issue(any())).thenReturn("token");

        assertThat(authService.login("kate", "secret")).isEqualTo("token");
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = new User("kate", "kate@example.com", "hashed");
        when(userRepository.findByLogin("kate")).thenReturn(Optional.of(user));
        when(passwordHasher.verify("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("kate", "wrong"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nobody", "secret"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    void authenticateReturnsUserForValidToken() {
        User user = new User("kate", "kate@example.com", "h");
        when(tokenProvider.validate("token")).thenReturn(Optional.of(5L));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        assertThat(authService.authenticate("token")).isSameAs(user);
    }

    @Test
    void authenticateRejectsInvalidToken() {
        when(tokenProvider.validate("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("bad"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void authenticateRejectsUnknownUser() {
        when(tokenProvider.validate("token")).thenReturn(Optional.of(9L));
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void authenticateRejectsDeletedUser() {
        User user = new User("kate", "kate@example.com", "h");
        ReflectionTestUtils.setField(user, "isDeleted", true);
        when(tokenProvider.validate("token")).thenReturn(Optional.of(5L));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.authenticate("token"))
                .isInstanceOf(UnauthorizedException.class);
    }
}