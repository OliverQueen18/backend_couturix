package com.couturix.storage;

import com.couturix.common.exception.ApiException;
import com.couturix.config.CouturixProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private final Path root;
    private final String publicPrefix;

    public FileStorageService(CouturixProperties properties) throws IOException {
        this.root = Path.of(properties.storage().directory()).toAbsolutePath().normalize();
        this.publicPrefix = properties.storage().publicUrlPrefix();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("Fichier vide");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!ALLOWED.contains(contentType)) {
            throw ApiException.badRequest("Format d'image non supporté");
        }
        String ext = contentType.equals("image/png") ? ".png" : contentType.equals("image/webp") ? ".webp" : ".jpg";
        String filename = UUID.randomUUID() + ext;
        try {
            Path dir = root.resolve(folder);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return publicPrefix + "/" + folder + "/" + filename;
        } catch (IOException e) {
            throw ApiException.badRequest("Impossible d'enregistrer le fichier");
        }
    }

    public Resource load(String folder, String filename) {
        try {
            Path file = root.resolve(folder).resolve(filename).normalize();
            if (!file.startsWith(root) || !Files.exists(file)) {
                throw ApiException.notFound("Fichier introuvable");
            }
            return new UrlResource(file.toUri());
        } catch (IOException e) {
            throw ApiException.notFound("Fichier introuvable");
        }
    }
}
