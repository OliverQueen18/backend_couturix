package com.couturix.site;

import com.couturix.site.dto.SitePageDto;
import com.couturix.site.dto.SiteSnapshotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/site")
public class PublicSiteController {

    private final SiteCmsService cms;

    @GetMapping
    public SiteSnapshotDto snapshot() {
        return cms.publicSnapshot();
    }

    @GetMapping("/pages/{slug}")
    public SitePageDto page(@PathVariable String slug) {
        return cms.publicPage(slug);
    }
}
