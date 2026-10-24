package com.couturix.site;

import com.couturix.site.dto.PlatformOverviewDto;
import com.couturix.site.dto.SiteMenuDto;
import com.couturix.site.dto.SitePageDto;
import com.couturix.site.dto.SiteSettingsDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/site")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminSiteController {

    private final SiteCmsService cms;

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
}
