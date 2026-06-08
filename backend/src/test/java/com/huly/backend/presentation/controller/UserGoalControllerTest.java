package com.huly.backend.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huly.backend.domain.model.UserGoal;
import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.useCase.userGoal.*;
import com.huly.backend.infrastructure.presentation.controller.UserGoalController;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserGoalControllerTest {

    private static final Long USER_ID = 10L;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private AddUserGoalUseCase addUserGoalUseCase;
    private GetUserGoalsByUserUseCase getUserGoalsByUserUseCase;
    private DeleteUserGoalUseCase deleteUserGoalUseCase;
    private UpdateUserGoalUseCase updateUserGoalUseCase;
    private CompleteUserGoalUseCase completeUserGoalUseCase;
    private AcceptChallengeUseCase acceptChallengeUseCase;

    @BeforeEach
    void setUp() {
        addUserGoalUseCase = mock(AddUserGoalUseCase.class);
        getUserGoalsByUserUseCase = mock(GetUserGoalsByUserUseCase.class);
        deleteUserGoalUseCase = mock(DeleteUserGoalUseCase.class);
        updateUserGoalUseCase = mock(UpdateUserGoalUseCase.class);
        completeUserGoalUseCase = mock(CompleteUserGoalUseCase.class);
        acceptChallengeUseCase = mock(AcceptChallengeUseCase.class);

        UserGoalController controller = new UserGoalController(acceptChallengeUseCase,
                addUserGoalUseCase, getUserGoalsByUserUseCase,
                deleteUserGoalUseCase, updateUserGoalUseCase, completeUserGoalUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserGoal goal(Long id, String title, GoalStatus status) {
        return UserGoal.builder()
                .id(id).userId(USER_ID).title(title).description("D")
                .status(status).activityId(1L).createdAt(Instant.now()).build();
    }

    @Test
    void add_shouldReturn201_whenRequestIsValid() throws Exception {
        when(addUserGoalUseCase.execute(eq(USER_ID), eq("Respirar"), eq("Desc"), eq(2L)))
                .thenReturn(goal(1L, "Respirar", GoalStatus.PENDING));

        mockMvc.perform(post("/api/user-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserGoalRequest("Respirar", "Desc", 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Respirar"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void add_shouldReturn201_withNullActivityId() throws Exception {
        when(addUserGoalUseCase.execute(eq(USER_ID), eq("Respirar"), isNull(), isNull()))
                .thenReturn(goal(1L, "Respirar", GoalStatus.PENDING));

        mockMvc.perform(post("/api/user-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserGoalRequest("Respirar", null, null))))
                .andExpect(status().isCreated());
    }

    @Test
    void add_shouldReturn400_whenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/user-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserGoalRequest("", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listByUser_shouldReturn200WithPaginatedCompletadosAndPendientes() throws Exception {
        Page<UserGoal> completadosPage = new PageImpl<>(
                List.of(goal(1L, "Completado", GoalStatus.COMPLETED)),
                PageRequest.of(0, 5), 1);
        Page<UserGoal> pendientesPage = new PageImpl<>(
                List.of(goal(2L, "Pendiente", GoalStatus.PENDING)),
                PageRequest.of(0, 5), 1);

        when(getUserGoalsByUserUseCase.executeCompleted(eq(USER_ID), any(Pageable.class))).thenReturn(completadosPage);
        when(getUserGoalsByUserUseCase.executePending(eq(USER_ID), any(Pageable.class))).thenReturn(pendientesPage);

        mockMvc.perform(get("/api/user-goals/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.content[0].title").value("Completado"))
                .andExpect(jsonPath("$.completados.totalElements").value(1))
                .andExpect(jsonPath("$.completados.pageSize").value(5))
                .andExpect(jsonPath("$.pendientes.content[0].title").value("Pendiente"))
                .andExpect(jsonPath("$.pendientes.totalElements").value(1));
    }

    @Test
    void listByUser_shouldUseSizeAndPageParams() throws Exception {
        when(getUserGoalsByUserUseCase.executeCompleted(eq(USER_ID), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(1, 3)));
        when(getUserGoalsByUserUseCase.executePending(eq(USER_ID), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(1, 3)));

        mockMvc.perform(get("/api/user-goals/me").param("page", "1").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.pageNumber").value(1))
                .andExpect(jsonPath("$.completados.pageSize").value(3));
    }

    @Test
    void listByUser_shouldReturn200WithEmptyPages_whenNoGoals() throws Exception {
        when(getUserGoalsByUserUseCase.executeCompleted(eq(USER_ID), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(getUserGoalsByUserUseCase.executePending(eq(USER_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/user-goals/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.content").isEmpty())
                .andExpect(jsonPath("$.pendientes.content").isEmpty());
    }

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        when(updateUserGoalUseCase.execute(eq(1L), eq("Nuevo"), eq("Desc"), eq(2L)))
                .thenReturn(goal(1L, "Nuevo", GoalStatus.PENDING));

        mockMvc.perform(put("/api/user-goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserGoalUpdateRequest("Nuevo", "Desc", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Nuevo"));
    }

    @Test
    void update_shouldReturn400_whenTitleIsBlank() throws Exception {
        mockMvc.perform(put("/api/user-goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserGoalUpdateRequest("", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204_whenGoalExists() throws Exception {
        mockMvc.perform(delete("/api/user-goals/1"))
                .andExpect(status().isNoContent());

        verify(deleteUserGoalUseCase).execute(1L);
    }

    @Test
    void complete_shouldReturn200WithCompletedStatus_whenGoalExists() throws Exception {
        when(completeUserGoalUseCase.execute(1L))
                .thenReturn(goal(1L, "Meta", GoalStatus.COMPLETED));

        mockMvc.perform(patch("/api/user-goals/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void complete_shouldDelegateToUseCase_withCorrectId() throws Exception {
        when(completeUserGoalUseCase.execute(42L))
                .thenReturn(goal(42L, "Meta", GoalStatus.COMPLETED));

        mockMvc.perform(patch("/api/user-goals/42/complete"))
                .andExpect(status().isOk());

        verify(completeUserGoalUseCase).execute(42L);
    }
}
