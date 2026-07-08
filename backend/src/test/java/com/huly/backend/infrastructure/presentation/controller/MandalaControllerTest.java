package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.mandala.ClearMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaProgressResponse;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusRequest;
import com.huly.backend.domain.dto.mandala.GetMandalaSessionStatusResponse;
import com.huly.backend.domain.dto.mandala.ListAvailableMandalasRequest;
import com.huly.backend.domain.dto.mandala.ListAvailableMandalasResponse;
import com.huly.backend.domain.dto.mandala.MandalaItem;
import com.huly.backend.domain.dto.mandala.SaveMandalaProgressRequest;
import com.huly.backend.domain.useCase.mandala.ClearMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaSessionStatusUseCase;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
import com.huly.backend.domain.useCase.mandala.SaveMandalaProgressUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.mandala.MandalaPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MandalaControllerTest {

    private static final Long USER_ID = 7L;
    private static final String MANDALA_ID = "mandala-01";
    private static final byte[] PAINT_BLOB = "paint".getBytes();

    private MockMvc mockMvc;
    private ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private SaveMandalaProgressUseCase saveMandalaProgressUseCase;
    private GetMandalaProgressUseCase getMandalaProgressUseCase;
    private GetMandalaSessionStatusUseCase getMandalaSessionStatusUseCase;
    private ClearMandalaProgressUseCase clearMandalaProgressUseCase;

    @BeforeEach
    void setUp() {
        listAvailableMandalasUseCase = mock(ListAvailableMandalasUseCase.class);
        saveMandalaProgressUseCase = mock(SaveMandalaProgressUseCase.class);
        getMandalaProgressUseCase = mock(GetMandalaProgressUseCase.class);
        getMandalaSessionStatusUseCase = mock(GetMandalaSessionStatusUseCase.class);
        clearMandalaProgressUseCase = mock(ClearMandalaProgressUseCase.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new MandalaController(listAvailableMandalasUseCase, saveMandalaProgressUseCase,
                        getMandalaProgressUseCase, getMandalaSessionStatusUseCase, clearMandalaProgressUseCase,
                        new MandalaPresentationMapper()))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── GET /api/mandalas ────────────────────────────────────────────────────

    @Test
    @DisplayName("Devuelve 200 con la página de mandalas disponibles")
    void getAvailableMandalasReturnsPageSuccessfully() throws Exception {
        // --- arrange ---
        givenAvailableMandalas(availablePage());
        // --- act ---
        ResultActions result = performGetAvailableMandalas();
        // --- assert ---
        thenOkWithMandalaPage(result);
    }

    @Test
    @DisplayName("Devuelve 401 al listar mandalas cuando no está autenticado")
    void getAvailableMandalasWithoutAuthenticationReturnsUnauthorized() throws Exception {
        // --- arrange ---
        givenNoAuthentication();
        // --- act ---
        ResultActions result = performGetAvailableMandalas();
        // --- assert ---
        thenUnauthorized(result);
    }

    // ── PUT /api/mandalas/{id}/progress ──────────────────────────────────────

    @Test
    @DisplayName("Delega el cuerpo binario al caso de uso al guardar el progreso")
    void saveProgressDelegatesBinaryBodyToUseCase() throws Exception {
        // --- act ---
        ResultActions result = performSaveProgress();
        // --- assert ---
        thenOk(result);
        thenSaveProgressDelegated();
    }

    // ── GET /api/mandalas/{id}/progress ──────────────────────────────────────

    @Test
    @DisplayName("Devuelve el cuerpo binario almacenado cuando hay progreso")
    void getProgressReturnsStoredBinaryBody() throws Exception {
        // --- arrange ---
        givenStoredProgress();
        // --- act ---
        ResultActions result = performGetProgress();
        // --- assert ---
        thenOkWithBinaryBody(result);
    }

    @Test
    @DisplayName("Devuelve 404 cuando no hay progreso almacenado")
    void getProgressWithoutStoredProgressReturnsNotFound() throws Exception {
        // --- arrange ---
        givenNoStoredProgress();
        // --- act ---
        ResultActions result = performGetProgress();
        // --- assert ---
        thenNotFound(result);
    }

    // ── DELETE /api/mandalas/{id}/progress ───────────────────────────────────

    @Test
    @DisplayName("Delega en el caso de uso al limpiar el progreso")
    void clearProgressDelegatesToUseCase() throws Exception {
        // --- act ---
        ResultActions result = performClearProgress();
        // --- assert ---
        thenNoContent(result);
        thenClearProgressDelegated();
    }

    // ── GET /api/mandalas/{id}/session-status ────────────────────────────────

    @Test
    @DisplayName("Devuelve el estado de la sesión correctamente")
    void getSessionStatusReturnsStatusSuccessfully() throws Exception {
        // --- arrange ---
        givenSessionStatus(true);
        // --- act ---
        ResultActions result = performGetSessionStatus();
        // --- assert ---
        thenOkWithSessionStatus(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenAvailableMandalas(ListAvailableMandalasResponse response) {
        when(listAvailableMandalasUseCase.execute(new ListAvailableMandalasRequest(USER_ID, 0, 6)))
                .thenReturn(response);
    }

    private void givenStoredProgress() {
        when(getMandalaProgressUseCase.execute(new GetMandalaProgressRequest(USER_ID, MANDALA_ID)))
                .thenReturn(new GetMandalaProgressResponse(Optional.of(PAINT_BLOB)));
    }

    private void givenNoStoredProgress() {
        when(getMandalaProgressUseCase.execute(new GetMandalaProgressRequest(USER_ID, MANDALA_ID)))
                .thenReturn(new GetMandalaProgressResponse(Optional.empty()));
    }

    private void givenSessionStatus(boolean registered) {
        when(getMandalaSessionStatusUseCase.execute(new GetMandalaSessionStatusRequest(USER_ID, MANDALA_ID)))
                .thenReturn(new GetMandalaSessionStatusResponse(registered));
    }

    private ListAvailableMandalasResponse availablePage() {
        return new ListAvailableMandalasResponse(
                List.of(new MandalaItem(MANDALA_ID, "Mandala 01", "Una mandala", "asset-01", 1, "DEFAULT", "FREE", false)),
                0, 6, 1, 1, true, true);
    }

    // --- act ---
    private ResultActions performGetAvailableMandalas() throws Exception {
        return mockMvc.perform(get("/api/mandalas"));
    }

    private ResultActions performSaveProgress() throws Exception {
        return mockMvc.perform(put("/api/mandalas/" + MANDALA_ID + "/progress")
                .contentType("application/octet-stream")
                .content(PAINT_BLOB));
    }

    private ResultActions performGetProgress() throws Exception {
        return mockMvc.perform(get("/api/mandalas/" + MANDALA_ID + "/progress"));
    }

    private ResultActions performClearProgress() throws Exception {
        return mockMvc.perform(delete("/api/mandalas/" + MANDALA_ID + "/progress"));
    }

    private ResultActions performGetSessionStatus() throws Exception {
        return mockMvc.perform(get("/api/mandalas/" + MANDALA_ID + "/session-status"));
    }

    // --- assert ---
    private void thenOkWithMandalaPage(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(MANDALA_ID))
                .andExpect(jsonPath("$.content[0].title").value("Mandala 01"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(6))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenSaveProgressDelegated() {
        ArgumentCaptor<SaveMandalaProgressRequest> captor = ArgumentCaptor.forClass(SaveMandalaProgressRequest.class);
        verify(saveMandalaProgressUseCase).execute(captor.capture());
        SaveMandalaProgressRequest request = captor.getValue();
        assertThat(request.userId()).isEqualTo(USER_ID);
        assertThat(request.mandalaId()).isEqualTo(MANDALA_ID);
        assertThat(Arrays.equals(request.paintBlob(), PAINT_BLOB)).isTrue();
    }

    private void thenOkWithBinaryBody(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().bytes(PAINT_BLOB));
    }

    private void thenNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound());
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenClearProgressDelegated() {
        verify(clearMandalaProgressUseCase).execute(new ClearMandalaProgressRequest(USER_ID, MANDALA_ID));
    }

    private void thenOkWithSessionStatus(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().json("{\"sessionRegistered\":true}"));
    }
}
