package com.huly.backend.presentation.controller;


import com.huly.backend.domain.model.Example;
import com.huly.backend.domain.useCase.CreateUseCase;
import com.huly.backend.presentation.dto.ExampleRequest;
import com.huly.backend.presentation.dto.ExampleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Controller: recibe HTTP → llama al UseCase → devuelve respuesta.
 * No contiene lógica de negocio.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/examples")
public class ExampleController {

    private final CreateUseCase createUseCase;


    @PostMapping
    public ResponseEntity<ExampleResponse> create(@Valid @RequestBody ExampleRequest request) {
        Example created = createUseCase.execute(request.name(), request.description());
        return ResponseEntity.ok(toResponse(created));
    }


    private ExampleResponse toResponse(Example e) {
        return new ExampleResponse(
                e.getId(),
                e.getName(),
                e.getDescription()

        );
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head><title>Test</title></head>
                <body>
                    <h1>Server is running!</h1>
                    <p>Deploy exitoso en Render</p>
                    <p>Ponete a chambear crgio</p>
                </body>
                </html>
                """;
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }


}