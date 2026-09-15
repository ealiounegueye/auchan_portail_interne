package sn.auchan.portail.web;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.auchan.portail.service.LogoStorageService;

@RestController
@RequestMapping("/api/files/logos")
public class LogoFileController {

    private final LogoStorageService logos;

    public LogoFileController(LogoStorageService logos) {
        this.logos = logos;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> get(@PathVariable String filename) {
        Resource resource = logos.load(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logos.contentType(filename)))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(resource);
    }
}
