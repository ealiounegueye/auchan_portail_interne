package sn.auchan.portail.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sn.auchan.portail.dto.UploadedDocument;
import sn.auchan.portail.service.DocumentFileStorageService;
import sn.auchan.portail.service.LogoStorageService;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final LogoStorageService logos;
    private final DocumentFileStorageService documents;

    public AdminUploadController(LogoStorageService logos, DocumentFileStorageService documents) {
        this.logos = logos;
        this.documents = documents;
    }

    @PostMapping("/logos")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> uploadLogo(@RequestParam("file") MultipartFile file) {
        return Map.of("url", logos.save(file));
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadedDocument uploadDocument(@RequestParam("file") MultipartFile file) {
        return documents.save(file);
    }
}
