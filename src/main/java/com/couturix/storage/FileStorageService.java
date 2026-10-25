package com.couturix.storage;

import com.couturix.common.exception.ApiException;
import com.couturix.config.CouturixProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
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

    public List<StoredImage> list(String folder) {
        Path dir = root.resolve(folder).normalize();
        if (!dir.startsWith(root) || !Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> imageExtension(path.getFileName().toString()))
                    .sorted(Comparator.comparingLong(this::modified).reversed())
                    .map(path -> toStored(folder, path))
                    .toList();
        } catch (IOException ex) {
            return List.of();
        }
    }

    public record StoredImage(String url, String filename, String mime, long sizeBytes, Instant modifiedAt) {}

    private StoredImage toStored(String folder, Path path) {
        String filename = path.getFileName().toString();
        long size = 0;
        Instant modified = Instant.now();
        try {
            size = Files.size(path);
            modified = Files.getLastModifiedTime(path).toInstant();
        } catch (IOException ignored) {
            // keep defaults
        }
        return new StoredImage(publicPrefix + "/" + folder + "/" + filename, filename, mimeOf(filename), size, modified);
    }

    private long modified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            return 0L;
        }
    }

    private static boolean imageExtension(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".webp") || lower.endsWith(".svg");
    }

    private static String mimeOf(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "image/jpeg";
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
