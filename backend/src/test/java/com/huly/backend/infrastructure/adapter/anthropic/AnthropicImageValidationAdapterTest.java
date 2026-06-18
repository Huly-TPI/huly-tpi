package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.model.ImageValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnthropicImageValidationAdapterTest {

    private ChatModel chatModel;
    private AnthropicImageValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        adapter = new AnthropicImageValidationAdapter(chatModel);
    }

    private void givenModelReturns(String text) {
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage output = mock(AssistantMessage.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(text);
    }

    private byte[] someBytes() {
        return new byte[]{1, 2, 3};
    }

    @Test
    void validate_shouldReturnValidResult_whenClaudeApproves() {
        givenModelReturns("{\"valid\": true, \"reason\": \"La imagen muestra ejercicio físico\"}");

        ImageValidationResult result = adapter.validate(someBytes(), "image/jpeg", "Correr 5km", null);

        assertThat(result.valid()).isTrue();
        assertThat(result.reason()).isEqualTo("La imagen muestra ejercicio físico");
    }

    @Test
    void validate_shouldReturnInvalidResult_whenClaudeRejects() {
        givenModelReturns("{\"valid\": false, \"reason\": \"La imagen no está relacionada con el reto\"}");

        ImageValidationResult result = adapter.validate(someBytes(), "image/jpeg", "Correr 5km", null);

        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isEqualTo("La imagen no está relacionada con el reto");
    }

    @Test
    void validate_shouldParseJsonEmbeddedInSurroundingText() {
        givenModelReturns("Aquí está mi respuesta: {\"valid\": true, \"reason\": \"Correcto\"} — fin.");

        ImageValidationResult result = adapter.validate(someBytes(), "image/jpeg", "Reto", null);

        assertThat(result.valid()).isTrue();
        assertThat(result.reason()).isEqualTo("Correcto");
    }

    @Test
    void validate_shouldWorkWithDescription_whenDescriptionIsPresent() {
        givenModelReturns("{\"valid\": true, \"reason\": \"ok\"}");

        ImageValidationResult result = adapter.validate(
                someBytes(), "image/jpeg", "Correr 5km", "Completar una carrera de 5 kilómetros"
        );

        assertThat(result.valid()).isTrue();
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void validate_shouldWorkWithoutDescription_whenDescriptionIsBlank() {
        givenModelReturns("{\"valid\": false, \"reason\": \"Sin relación\"}");

        ImageValidationResult result = adapter.validate(someBytes(), "image/jpeg", "Reto", "");

        assertThat(result.valid()).isFalse();
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void validate_shouldWorkWithoutDescription_whenDescriptionIsNull() {
        givenModelReturns("{\"valid\": true, \"reason\": \"ok\"}");

        ImageValidationResult result = adapter.validate(someBytes(), "image/png", "Reto", null);

        assertThat(result.valid()).isTrue();
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void validate_shouldThrowImageValidationUnavailableException_whenChatModelThrows() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> adapter.validate(someBytes(), "image/jpeg", "Reto", null))
                .isInstanceOf(ImageValidationUnavailableException.class);
    }

    @Test
    void validate_shouldThrowImageValidationUnavailableException_whenResponseHasNoJson() {
        givenModelReturns("No puedo analizar esto");

        assertThatThrownBy(() -> adapter.validate(someBytes(), "image/jpeg", "Reto", null))
                .isInstanceOf(ImageValidationUnavailableException.class);
    }

    @Test
    void validate_shouldThrowImageValidationUnavailableException_whenResponseIsEmpty() {
        givenModelReturns("");

        assertThatThrownBy(() -> adapter.validate(someBytes(), "image/jpeg", "Reto", null))
                .isInstanceOf(ImageValidationUnavailableException.class);
    }

    @Test
    void validate_shouldThrowImageValidationUnavailableException_whenJsonIsMalformed() {
        givenModelReturns("{\"valid\": \"no es booleano\", \"reason\": }");

        assertThatThrownBy(() -> adapter.validate(someBytes(), "image/jpeg", "Reto", null))
                .isInstanceOf(ImageValidationUnavailableException.class);
    }

    @Test
    void validate_shouldThrowImageValidationUnavailableException_whenResponseResultIsNull() {
        ChatResponse chatResponse = mock(ChatResponse.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(null);

        assertThatThrownBy(() -> adapter.validate(someBytes(), "image/jpeg", "Reto", null))
                .isInstanceOf(ImageValidationUnavailableException.class);
    }
}
