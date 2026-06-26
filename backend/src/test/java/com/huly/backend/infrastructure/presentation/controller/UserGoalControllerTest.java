package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huly.backend.domain.dto.userGoal.AcceptChallengeResponse;
import com.huly.backend.domain.dto.userGoal.AddUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.CompleteUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsRequest;
import com.huly.backend.domain.dto.userGoal.GetUserGoalsResponse;
import com.huly.backend.domain.dto.userGoal.UpdateUserGoalResponse;
import com.huly.backend.domain.dto.userGoal.UserGoalItem;
import com.huly.backend.domain.dto.userGoal.UserGoalPage;
import com.huly.backend.domain.dto.userPlant.UserPlantItem;
import com.huly.backend.domain.useCase.userGoal.*;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalRequest;
import com.huly.backend.infrastructure.presentation.dto.userGoal.UserGoalUpdateRequest;
import com.huly.backend.infrastructure.presentation.mapper.userGoal.UserGoalPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserGoalControllerTest {

    private static final Long USER_ID = 10L;

    @TempDir
    Path tempDir;

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
    private GetGoalImageUseCase getGoalImageUseCase;

    @BeforeEach
    void setUp() {
        addUserGoalUseCase = mock(AddUserGoalUseCase.class);
        getUserGoalsByUserUseCase = mock(GetUserGoalsByUserUseCase.class);
        deleteUserGoalUseCase = mock(DeleteUserGoalUseCase.class);
        updateUserGoalUseCase = mock(UpdateUserGoalUseCase.class);
        completeUserGoalUseCase = mock(CompleteUserGoalUseCase.class);
        acceptChallengeUseCase = mock(AcceptChallengeUseCase.class);
        getGoalImageUseCase = mock(GetGoalImageUseCase.class);

        UserGoalController controller = new UserGoalController(acceptChallengeUseCase,
                addUserGoalUseCase, getUserGoalsByUserUseCase,
                deleteUserGoalUseCase, updateUserGoalUseCase, completeUserGoalUseCase,
                getGoalImageUseCase, new UserGoalPresentationMapper());
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

    private UserGoalItem goalItem(Long id, String title, String status) {
        return new UserGoalItem(id, USER_ID, title, "D", status, Instant.now(), 1L, null, 10, 25);
    }

    private UserGoalPage page(List<UserGoalItem> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        return new UserGoalPage(content, pageNumber, pageSize, totalElements, totalPages, pageNumber == 0,
                pageNumber >= Math.max(totalPages - 1, 0));
    }

    @Test
    void add_shouldReturn201_whenRequestIsValid() throws Exception {
        when(addUserGoalUseCase.execute(argThat(r ->
                r != null && USER_ID.equals(r.userId()) && "Respirar".equals(r.title())
                        && "Desc".equals(r.description()) && Long.valueOf(2L).equals(r.activityId()))))
                .thenReturn(new AddUserGoalResponse(goalItem(1L, "Respirar", "PENDING")));

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
        when(addUserGoalUseCase.execute(argThat(r ->
                r != null && USER_ID.equals(r.userId()) && r.description() == null && r.activityId() == null)))
                .thenReturn(new AddUserGoalResponse(goalItem(1L, "Respirar", "PENDING")));

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
        GetUserGoalsResponse response = new GetUserGoalsResponse(
                page(List.of(goalItem(1L, "Completado", "COMPLETED")), 0, 5, 1),
                page(List.of(goalItem(2L, "Pendiente", "PENDING")), 0, 5, 1));
        when(getUserGoalsByUserUseCase.execute(any(GetUserGoalsRequest.class))).thenReturn(response);

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
        GetUserGoalsResponse response = new GetUserGoalsResponse(
                page(List.of(), 1, 3, 0),
                page(List.of(), 1, 3, 0));
        when(getUserGoalsByUserUseCase.execute(argThat(r -> r != null && r.page() == 1 && r.size() == 3)))
                .thenReturn(response);

        mockMvc.perform(get("/api/user-goals/me").param("page", "1").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.pageNumber").value(1))
                .andExpect(jsonPath("$.completados.pageSize").value(3));
    }

    @Test
    void listByUser_shouldReturn200WithEmptyPages_whenNoGoals() throws Exception {
        GetUserGoalsResponse response = new GetUserGoalsResponse(
                page(List.of(), 0, 5, 0),
                page(List.of(), 0, 5, 0));
        when(getUserGoalsByUserUseCase.execute(any(GetUserGoalsRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/user-goals/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completados.content").isEmpty())
                .andExpect(jsonPath("$.pendientes.content").isEmpty());
    }

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        when(updateUserGoalUseCase.execute(argThat(r ->
                r != null && Long.valueOf(1L).equals(r.id()) && "Nuevo".equals(r.title())
                        && "Desc".equals(r.description()) && Long.valueOf(2L).equals(r.activityId()))))
                .thenReturn(new UpdateUserGoalResponse(goalItem(1L, "Nuevo", "PENDING")));

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

        verify(deleteUserGoalUseCase).execute(argThat(r -> r != null && Long.valueOf(1L).equals(r.id())));
    }

    private CompleteUserGoalResponse completeResponse(Long goalId) {
        UserPlantItem plant = new UserPlantItem(1L, 1, 5, 1L, "GROWING", Instant.now(), null);
        return new CompleteUserGoalResponse(goalItem(goalId, "Meta", "COMPLETED"), false, null, plant);
    }

    @Test
    void complete_shouldReturn200WithCompletedStatus_whenGoalExists() throws Exception {
        when(completeUserGoalUseCase.execute(argThat(r -> r != null && Long.valueOf(1L).equals(r.id())), isNull()))
                .thenReturn(completeResponse(1L));

        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user-goals/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goal.id").value(1L))
                .andExpect(jsonPath("$.goal.status").value("COMPLETED"));
    }

    @Test
    void complete_shouldDelegateToUseCase_withCorrectId() throws Exception {
        when(completeUserGoalUseCase.execute(argThat(r -> r != null && Long.valueOf(42L).equals(r.id())), isNull()))
                .thenReturn(completeResponse(42L));

        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user-goals/42/complete"))
                .andExpect(status().isOk());

        verify(completeUserGoalUseCase).execute(argThat(r -> r != null && Long.valueOf(42L).equals(r.id())), isNull());
    }

    @Test
    void getImage_shouldReturn404_whenFileDoesNotExist() throws Exception {
        when(getGoalImageUseCase.execute(argThat(r -> r != null && "missing.jpg".equals(r.filename()))))
                .thenReturn(Path.of("nonexistent-xyz-dir", "missing.jpg"));

        mockMvc.perform(get("/api/user-goals/images/missing.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_shouldReturn200WithImageContent_whenFileExists() throws Exception, IOException {
        Path imageFile = tempDir.resolve("photo.jpg");
        Files.write(imageFile, new byte[]{1, 2, 3});
        when(getGoalImageUseCase.execute(argThat(r -> r != null && "photo.jpg".equals(r.filename()))))
                .thenReturn(imageFile);

        mockMvc.perform(get("/api/user-goals/images/photo.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void getImage_shouldReturn200WithPngContentType_whenFilenameIsPng() throws Exception, IOException {
        Path imageFile = tempDir.resolve("photo.png");
        Files.write(imageFile, new byte[]{1, 2, 3});
        when(getGoalImageUseCase.execute(argThat(r -> r != null && "photo.png".equals(r.filename()))))
                .thenReturn(imageFile);

        mockMvc.perform(get("/api/user-goals/images/photo.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void getImage_shouldReturn200WithGifContentType_whenFilenameIsGif() throws Exception, IOException {
        Path imageFile = tempDir.resolve("anim.gif");
        Files.write(imageFile, new byte[]{1, 2, 3});
        when(getGoalImageUseCase.execute(argThat(r -> r != null && "anim.gif".equals(r.filename()))))
                .thenReturn(imageFile);

        mockMvc.perform(get("/api/user-goals/images/anim.gif"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_GIF));
    }

    @Test
    void add_shouldReturn401_whenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/user-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserGoalRequest("Titulo", null, null))))
                .andExpect(status().isUnauthorized());
    }
}
