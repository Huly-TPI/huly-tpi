package com.huly.backend.infrastructure.adapter;

import com.huly.backend.domain.port.AudioTranscriptionPort;
import com.huly.backend.domain.port.AudioTranscriptionPort.AudioTranscriptionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class SenseVoiceAdapterTest {

    private static final String BASE_URL = "http://localhost:8001";

    private SenseVoiceAdapter adapter;
    private MockRestServiceServer mockServer;
    private MockRestServiceServer healthMockServer;

    @BeforeEach
    void setUp() {
        adapter = new SenseVoiceAdapter();
        ReflectionTestUtils.setField(adapter, "senseVoiceUrl", BASE_URL);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(adapter, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestTemplate healthRestTemplate = (RestTemplate) ReflectionTestUtils.getField(adapter, "healthRestTemplate");
        healthMockServer = MockRestServiceServer.createServer(healthRestTemplate);
    }

    @Test
    @DisplayName("Devuelve el resultado completo cuando el servicio responde correctamente")
    void transcribeShouldReturnFullResultWhenServiceRespondsSuccessfully() {
        // --- arrange ---
        givenTranscriptionAndAnalysisSucceed();
        // --- act ---
        AudioTranscriptionResult result = transcribe();
        // --- assert ---
        thenResultIsFullyPopulated(result);
    }

    @Test
    @DisplayName("Parsea los valores VAD de la respuesta")
    void transcribeShouldParseVadValuesFromResponse() {
        // --- arrange ---
        givenAnalysisReturnsSadVad();
        // --- act ---
        AudioTranscriptionResult result = transcribe();
        // --- assert ---
        thenVadIsParsed(result);
    }

    @Test
    @DisplayName("Devuelve valores neutrales por defecto cuando el cuerpo de la respuesta es nulo")
    void transcribeShouldReturnNeutralDefaultsWhenResponseBodyIsNull() {
        // --- arrange ---
        givenServiceReturnsNullBodies();
        // --- act ---
        AudioTranscriptionResult result = transcribe();
        // --- assert ---
        thenResultHasNeutralDefaults(result);
    }

    @Test
    @DisplayName("Lanza excepción cuando el servicio no está disponible")
    void transcribeShouldThrowWhenServiceUnavailable() {
        // --- arrange ---
        givenTranscriptionServiceUnavailable();
        // --- assert ---
        thenTranscribeThrowsServiceUnavailable();
    }

    @Test
    @DisplayName("Ping devuelve true cuando /health responde correctamente")
    void pingShouldReturnTrueWhenHealthRespondsSuccessfully() {
        // --- arrange ---
        givenHealthRespondsSuccessfully();
        // --- act ---
        boolean result = ping();
        // --- assert ---
        thenPingSucceeded(result);
    }

    @Test
    @DisplayName("Ping devuelve false cuando /health responde con error de servidor")
    void pingShouldReturnFalseWhenHealthReturnsServerError() {
        // --- arrange ---
        givenHealthReturnsServerError();
        // --- act ---
        boolean result = ping();
        // --- assert ---
        thenPingFailed(result);
    }

    @Test
    @DisplayName("resolveContentType devuelve audio/wav para archivos .wav")
    void resolveContentTypeShouldReturnAudioWavForWavFilename() throws Exception {
        // --- act ---
        String contentType = resolveContentType("audio.wav");
        // --- assert ---
        thenContentTypeIs(contentType, "audio/wav");
    }

    @Test
    @DisplayName("resolveContentType devuelve audio/mpeg para archivos .mp3")
    void resolveContentTypeShouldReturnAudioMpegForMp3Filename() throws Exception {
        // --- act ---
        String contentType = resolveContentType("audio.mp3");
        // --- assert ---
        thenContentTypeIs(contentType, "audio/mpeg");
    }

    @Test
    @DisplayName("resolveContentType devuelve audio/ogg para archivos .ogg")
    void resolveContentTypeShouldReturnAudioOggForOggFilename() throws Exception {
        // --- act ---
        String contentType = resolveContentType("audio.ogg");
        // --- assert ---
        thenContentTypeIs(contentType, "audio/ogg");
    }

    @Test
    @DisplayName("resolveContentType devuelve audio/flac para archivos .flac")
    void resolveContentTypeShouldReturnAudioFlacForFlacFilename() throws Exception {
        // --- act ---
        String contentType = resolveContentType("audio.flac");
        // --- assert ---
        thenContentTypeIs(contentType, "audio/flac");
    }

    @Test
    @DisplayName("resolveContentType devuelve video/webm cuando el nombre es nulo")
    void resolveContentTypeShouldReturnVideoWebmForNullFilename() throws Exception {
        // --- act ---
        String contentType = resolveContentType(null);
        // --- assert ---
        thenContentTypeIs(contentType, "video/webm");
    }

    @Test
    @DisplayName("resolveContentType devuelve video/webm para archivos .webm")
    void resolveContentTypeShouldReturnVideoWebmForWebmFilename() throws Exception {
        // --- act ---
        String contentType = resolveContentType("recording.webm");
        // --- assert ---
        thenContentTypeIs(contentType, "video/webm");
    }

    // --- arrange ---

    private void givenTranscriptionAndAnalysisSucceed() {
        mockServer.expect(requestTo(BASE_URL + "/transcribe"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "transcripcion": "hola mundo",
                          "idioma_detectado": "es"
                        }""", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(BASE_URL + "/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "emocion_dominante": "happy",
                          "vad": { "arousal": 0.6, "dominance": 0.5, "valence": 0.7 }
                        }""", MediaType.APPLICATION_JSON));
    }

    private void givenAnalysisReturnsSadVad() {
        mockServer.expect(requestTo(BASE_URL + "/transcribe"))
                .andRespond(withSuccess("""
                        {
                          "transcripcion": "texto",
                          "idioma_detectado": "es"
                        }""", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(BASE_URL + "/analyze"))
                .andRespond(withSuccess("""
                        {
                          "emocion_dominante": "sad",
                          "vad": { "arousal": 0.2, "dominance": 0.4, "valence": 0.3 }
                        }""", MediaType.APPLICATION_JSON));
    }

    private void givenServiceReturnsNullBodies() {
        mockServer.expect(requestTo(BASE_URL + "/transcribe"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(BASE_URL + "/analyze"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
    }

    private void givenTranscriptionServiceUnavailable() {
        mockServer.expect(requestTo(BASE_URL + "/transcribe"))
                .andRespond(withServerError());
    }

    private void givenHealthRespondsSuccessfully() {
        healthMockServer.expect(requestTo(BASE_URL + "/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));
    }

    private void givenHealthReturnsServerError() {
        healthMockServer.expect(requestTo(BASE_URL + "/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());
    }

    // --- act ---

    private AudioTranscriptionResult transcribe() {
        return adapter.transcribe("audio".getBytes(), "recording.wav");
    }

    private boolean ping() {
        return adapter.ping();
    }

    private String resolveContentType(String filename) throws Exception {
        Method method = SenseVoiceAdapter.class.getDeclaredMethod("resolveContentType", String.class);
        method.setAccessible(true);
        return (String) method.invoke(adapter, filename);
    }

    // --- assert ---

    private void thenResultIsFullyPopulated(AudioTranscriptionResult result) {
        assertThat(result.transcription()).isEqualTo("hola mundo");
        assertThat(result.language()).isEqualTo("es");
        assertThat(result.dominantEmotion()).isEqualTo("happy");
        assertThat(result.vad()).isNotNull();
        mockServer.verify();
    }

    private void thenVadIsParsed(AudioTranscriptionResult result) {
        AudioTranscriptionPort.VadResult vad = result.vad();
        assertThat(vad.arousal()).isEqualTo(0.2);
        assertThat(vad.dominance()).isEqualTo(0.4);
        assertThat(vad.valence()).isEqualTo(0.3);
    }

    private void thenResultHasNeutralDefaults(AudioTranscriptionResult result) {
        assertThat(result.transcription()).isEmpty();
        assertThat(result.dominantEmotion()).isEqualTo("neutral");
        assertThat(result.language()).isEqualTo("unknown");
        assertThat(result.vad()).isNull();
    }

    private void thenTranscribeThrowsServiceUnavailable() {
        assertThatThrownBy(this::transcribe)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo transcribir el audio");
    }

    private void thenPingSucceeded(boolean result) {
        assertThat(result).isTrue();
        healthMockServer.verify();
    }

    private void thenPingFailed(boolean result) {
        assertThat(result).isFalse();
    }

    private void thenContentTypeIs(String contentType, String expected) {
        assertThat(contentType).isEqualTo(expected);
    }
}
