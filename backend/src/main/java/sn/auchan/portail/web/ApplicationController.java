package sn.auchan.portail.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.auchan.portail.dto.ApplicationResponse;
import sn.auchan.portail.dto.GeneratedDocument;
import sn.auchan.portail.dto.StoredDocumentFile;
import sn.auchan.portail.service.ApplicationService;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            Authentication authentication
    ) {
        return applicationService.search(q, categoryId, authentication.getName());
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id, Authentication authentication) {
        return applicationService.get(id, authentication.getName());
    }

    @PostMapping("/{id}/favorite")
    public void toggleFavorite(@PathVariable Long id, Authentication authentication) {
        applicationService.toggleFavorite(id, authentication.getName());
    }

    @GetMapping("/{id}/documents/{kind}")
    public ResponseEntity<byte[]> document(
            @PathVariable Long id,
            @PathVariable String kind,
            @RequestParam(defaultValue = "html") String format,
            Authentication authentication
    ) {
        GeneratedDocument document = applicationService.exportDocument(id, kind, format, authentication.getName());
        boolean inline = "html".equalsIgnoreCase(format);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.contentType()));
        headers.setContentDisposition(
                ContentDisposition.builder(inline ? "inline" : "attachment")
                        .filename(document.filename())
                        .build()
        );
        return ResponseEntity.ok().headers(headers).body(document.content());
    }

    @GetMapping("/{id}/documents/{kind}/file")
    public ResponseEntity<Resource> uploadedFile(
            @PathVariable Long id,
            @PathVariable String kind,
            Authentication authentication
    ) {
        StoredDocumentFile file = applicationService.downloadUploadedFile(id, kind, authentication.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.contentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(file.originalName(), StandardCharsets.UTF_8)
                        .build()
        );
        return ResponseEntity.ok().headers(headers).body(file.resource());
    }
}
