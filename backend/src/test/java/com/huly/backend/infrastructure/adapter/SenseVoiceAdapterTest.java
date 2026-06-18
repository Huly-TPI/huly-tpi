package com.huly.backend.infrastructure.adapter;

import com.huly.backend.domain.port.AudioTranscriptionPort;
import com.huly.backend.domain.port.AudioTranscriptionPort.AudioTranscriptionResult;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        adapter = new SenseVoiceAdapter();
        ReflectionTestUtils.setField(adapter, "senseVoiceUrl", BASE_URL);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(adapter, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void transcribe_shouldReturnFullResult_whenServiceRespondsSuccessfully() {
        String responseJson = """
                {
                  "transcripcion": "hola mundo",
                  "idioma_detectado": "es",
                  "emocion_dominante": "happy",
                  "vad": { "arousal": 0.6, "dominance": 0.5, "valence": 0.7 }
                }""";
        mockServer.expect(requestTo(BASE_URL + "/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AudioTranscriptionResult result = adapter.transcribe("audio".getBytes(), "recording.wav");

        assertThat(result.transcription()).isEqualTo("hola mundo");
        assertThat(result.language()).isEqualTo("es");
        assertThat(result.dominantEmotion()).isEqualTo("happy");
        assertThat(result.vad()).isNotNull();
        mockServer.verify();
    }

    @Test
    void transcribe_shouldParseVadValues_fromResponse() {
        String responseJson = """
                {
                  "transcripcion": "texto",
                  "idioma_detectado": "es",
                  "emocion_dominante": "sad",
                  "vad": { "arousal": 0.2, "dominance": 0.4, "valence": 0.3 }
                }""";
        mockServer.expect(requestTo(BASE_URL + "/analyze"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AudioTranscriptionResult result = adapter.transcribe("audio".getBytes(), "audio.wav");

        AudioTranscriptionPort.VadResult vad = result.vad();
        assertThat(vad.arousal()).isEqualTo(0.2);
        assertThat(vad.dominance()).isEqualTo(0.4);
        assertThat(vad.valence()).isEqualTo(0.3);
    }

    @Test
    void transcribe_shouldReturnNeutralDefaults_whenResponseBodyIsNull() {
        mockServer.expect(requestTo(BASE_URL + "/analyze"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        AudioTranscriptionResult result = adapter.transcribe("audio".getBytes(), "audio.webm");

        assertThat(result.transcription()).isEmpty();
        assertThat(result.dominantEmotion()).isEqualTo("neutral");
        assertThat(result.language()).isEqualTo("unknown");
        assertThat(result.vad()).isNull();
    }

    @Test
    void transcribe_shouldThrowRuntimeException_whenServiceUnavailable() {
        mockServer.expect(requestTo(BASE_URL + "/analyze"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.transcribe("audio".getBytes(), "audio.wav"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo transcribir el audio");
    }

    // --- resolveContentType (private method via reflection) ---

    @Test
    void resolveContentType_shouldReturnAudioWav_forWavFilename() throws Exception {
        Method m = SenseVoiceAdapter.class.getDeclaredMethod("resolveContentType", String.class);
        m.setAccessible(true);
        assertThat(m.invoke(adapter, "audio.wav")).isEqualTo("audio/wav");
    }

    @Test
    void resolveContentType_shouldReturnAudioMpeg_forMp3Filename() throws Exception {
        Method m = SenseVoiceAdapter.class.getDeclaredMethod("resolveContentType", String.class);
        m.setAccessible(true);
        assertThat(m.invoke(adapter, "audio.mp3")).isEqualTo("audio/mpeg");
    }

    @Test
    void resolveContentType_shouldReturnAudioOgg_forOggFilename() throws Exception {
        Method m = SenseVoiceAdapter.class.getDeclaredMethod("resolveContentType", String.class);
        m.setAccessible(true);
        assertThat(m.invoke(adapter, "audio.ogg")).isEqualTo("audio/ogg");
    }

    @Test
    void resolveContentType_shouldReturnAudioFlac_forFlacFilename() throws Exception {
        Method m = SenseVoiceAdapter.class.getDeclaredMethod("resolveContentType", String.class);
        m.setAccessible(true);
        assertThat(m.invoke(adapter, "audio.flac")).isEqualTo("audio/flac");
    }

    @Test
    void resolveContentType_shouldReturnVideoWebm_forNullFilename() throws Exception {
        Method m = SenseVoiceAdapter.class.getDeclaredMethod("resolveContentType", String.class);
        m.setAccessible(true);
        assertThat(m.invoke(adapter, (Object) null)).isEqualTo("video/webm");
    }

    @Test
    void resolveContentType_shouldReturnVideoWebm_forWebmFilename() throws Exception {
        Method m = SenseVoiceAdapter.class.getDeclaredMethod("resolveContentType", String.class);
        m.setAccessible(true);
        assertThat(m.invoke(adapter, "recording.webm")).isEqualTo("video/webm");
    }
}
