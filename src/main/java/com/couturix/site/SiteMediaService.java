package com.couturix.site;

import com.couturix.site.dto.SiteMediaDto;
import com.couturix.storage.FileStorageService;
import com.couturix.storage.FileStorageService.StoredImage;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SiteMediaService {

    private static final List<String> FOLDERS = List.of("site", "slides");

    private final SiteMediaRepository repository;
    private final SiteSlideRepository slideRepository;
    private final FileStorageService files;

    @Transactional
    public List<SiteMediaDto> list() {
        sync();
        return repository.findAllByOrderByCreatedAtDescIdDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public SiteMediaDto upload(MultipartFile file) {
        String original = originalName(file.getOriginalFilename());
        String url = files.store(file, "site");
        SiteMedia row = new SiteMedia();
        row.setOriginalName(original);
        row.setUrl(url);
        row.setMime(file.getContentType());
        row.setSizeBytes(file.getSize());
        return toDto(repository.save(row));
    }

    private void sync() {
        for (String folder : FOLDERS) {
            for (StoredImage image : files.list(folder)) {
                remember(image.filename(), image.url(), image.mime(), image.sizeBytes(), image.modifiedAt());
            }
        }
        for (SiteSlide slide : slideRepository.findAll()) {
            String url = slide.getImageUrl();
            if (url == null || url.isBlank()) {
                continue;
            }
            remember(nameFromUrl(url), url, mimeFromUrl(url), 0, Instant.now());
        }
    }

    private void remember(String name, String url, String mime, long size, Instant createdAt) {
        if (url == null || url.isBlank() || repository.existsByUrl(url)) {
            return;
        }
        SiteMedia row = new SiteMedia();
        row.setOriginalName(trimName(name));
        row.setUrl(url);
        row.setMime(mime);
        row.setSizeBytes(Math.max(0, size));
        if (createdAt != null) {
            row.setCreatedAt(createdAt);
            row.setUpdatedAt(createdAt);
        }
        repository.save(row);
    }

    private SiteMediaDto toDto(SiteMedia row) {
        return new SiteMediaDto(
                row.getId(),
                row.getOriginalName(),
                row.getUrl(),
                row.getMime(),
                row.getSizeBytes(),
                row.getCreatedAt());
    }

    private static String originalName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "image";
        }
        String name = raw.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        return trimName(slash >= 0 ? name.substring(slash + 1) : name);
    }

    private static String nameFromUrl(String url) {
        int slash = url.lastIndexOf('/');
        return trimName(slash >= 0 ? url.substring(slash + 1) : url);
    }

    private static String mimeFromUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
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

    private static String trimName(String name) {
        String value = name == null ? "image" : name.trim();
        if (value.isBlank()) {
            return "image";
        }
        return value.length() > 180 ? value.substring(0, 180) : value;
    }
}
