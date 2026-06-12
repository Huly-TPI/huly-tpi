package com.huly.backend.domain.service.userGoal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.uploads.goals-dir:uploads/goals}")
    private String uploadsDir;

    public String save(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Path dir = Paths.get(uploadsDir);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen del reto", e);
        }
        return "/api/user-goals/images/" + filename;
    }

    public Path resolve(String filename) {
        return Paths.get(uploadsDir).resolve(filename);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
