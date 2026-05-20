package com.huly.backend.presentation.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.Example;
import com.huly.backend.domain.useCase.CreateUseCase;
import com.huly.backend.presentation.dto.ExampleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ExampleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CreateUseCase createUseCase;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateUseCase.class);
        ExampleController controller = new ExampleController(createUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_shouldReturn200WithExampleResponse_whenRequestIsValid() throws Exception {
        Example saved = Example.builder().id(1L).name("Test").description("Desc").build();
        when(createUseCase.execute("Test", "Desc")).thenReturn(saved);

        ExampleRequest request = new ExampleRequest("Test", "Desc");

        mockMvc.perform(post("/api/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.description").value("Desc"));
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        ExampleRequest request = new ExampleRequest("", "Desc");

        mockMvc.perform(post("/api/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenNameExceedsMaxLength() throws Exception {
        String longName = "a".repeat(101);
        ExampleRequest request = new ExampleRequest(longName, "Desc");

        mockMvc.perform(post("/api/examples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn200WithHtmlContentType() throws Exception {
        mockMvc.perform(get("/api/examples/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}