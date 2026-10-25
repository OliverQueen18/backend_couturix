package com.couturix.site;

import com.couturix.site.dto.PlatformOverviewDto;
import com.couturix.site.dto.SiteMenuDto;
import com.couturix.site.dto.SitePageDto;
import com.couturix.site.dto.SiteSettingsDto;
import com.couturix.site.dto.SiteMediaDto;
import com.couturix.site.dto.SiteSlideDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/site")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminSiteController {

    private final SiteCmsService cms;
    private final SiteMediaService media;

    @GetMapping("/overview")
    public PlatformOverviewDto overview() {
        return cms.overview();
    }

    @GetMapping("/settings")
    public SiteSettingsDto settings() {
        return cms.settingsDto();
    }

    @PutMapping("/settings")
    public SiteSettingsDto saveSettings(@RequestBody SiteSettingsDto body) {
        return cms.saveSettings(body);
    }

    @GetMapping("/menus")
    public List<SiteMenuDto> menus() {
        return cms.menus();
    }

    @PostMapping("/menus")
    public SiteMenuDto createMenu(@Valid @RequestBody SiteMenuDto body) {
        return cms.saveMenu(null, body);
    }

    @PutMapping("/menus/{id}")
    public SiteMenuDto updateMenu(@PathVariable Long id, @Valid @RequestBody SiteMenuDto body) {
        return cms.saveMenu(id, body);
    }

    @DeleteMapping("/menus/{id}")
    public void deleteMenu(@PathVariable Long id) {
        cms.deleteMenu(id);
    }

    @GetMapping("/pages")
    public List<SitePageDto> pages() {
        return cms.pages();
    }

    @PostMapping("/pages")
    public SitePageDto createPage(@RequestBody SitePageDto body) {
        return cms.savePage(null, body);
    }

    @PutMapping("/pages/{id}")
    public SitePageDto updatePage(@PathVariable Long id, @RequestBody SitePageDto body) {
        return cms.savePage(id, body);
    }

    @DeleteMapping("/pages/{id}")
    public void deletePage(@PathVariable Long id) {
        cms.deletePage(id);
    }

    @GetMapping("/slides")
    public List<SiteSlideDto> slides() {
        return cms.slides();
    }

    @PostMapping("/slides")
    public SiteSlideDto createSlide(@RequestBody SiteSlideDto body) {
        return cms.saveSlide(null, body);
    }

    @PutMapping("/slides/{id}")
    public SiteSlideDto updateSlide(@PathVariable Long id, @RequestBody SiteSlideDto body) {
        return cms.saveSlide(id, body);
    }

    @DeleteMapping("/slides/{id}")
    public void deleteSlide(@PathVariable Long id) {
        cms.deleteSlide(id);
    }

    @PostMapping(value = "/slides/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadSlideImage(@RequestParam("file") MultipartFile file) {
        return Map.of("url", cms.uploadSlideImage(file));
    }

    @GetMapping("/media")
    public List<SiteMediaDto> media() {
        return media.list();
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SiteMediaDto uploadMedia(@RequestParam("file") MultipartFile file) {
        return media.upload(file);
    }
}
