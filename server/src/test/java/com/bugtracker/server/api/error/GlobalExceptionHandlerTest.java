package com.bugtracker.server.api.error;

import com.bugtracker.server.exception.ConflictException;
import com.bugtracker.server.exception.ForbiddenException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.exception.UnauthorizedException;
import com.bugtracker.server.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void validationReturns400WithField() throws Exception {
        mvc.perform(get("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("Имя пользователя уже занято"))
                .andExpect(jsonPath("$.field").value("username"));
    }

    @Test
    void unauthorizedReturns401() throws Exception {
        mvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.field").doesNotExist());
    }

    @Test
    void forbiddenReturns403() throws Exception {
        mvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void notFoundReturns404() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void conflictReturns409() throws Exception {
        mvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Закрытый баг удалить нельзя"));
    }

    @Test
    void unexpectedReturns500WithoutDetails() throws Exception {
        mvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL"))
                .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/validation")
        void validation() {
            throw new ValidationException("Имя пользователя уже занято", "username");
        }

        @GetMapping("/test/unauthorized")
        void unauthorized() {
            throw new UnauthorizedException("Неверный логин или пароль");
        }

        @GetMapping("/test/forbidden")
        void forbidden() {
            throw new ForbiddenException("Недостаточно прав");
        }

        @GetMapping("/test/not-found")
        void notFound() {
            throw new NotFoundException("Не найдено");
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new ConflictException("Закрытый баг удалить нельзя");
        }

        @GetMapping("/test/boom")
        void boom() {
            throw new IllegalStateException("секретные детали");
        }
    }
}