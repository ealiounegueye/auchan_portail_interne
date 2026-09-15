package sn.auchan.portail.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sn.auchan.portail.dto.UploadedDocument;

@Service
public class DocumentFileStorageService {

    private static final Set<String> EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "odt", "ods", "odp", "txt", "html", "htm",
            "png", "jpg", "jpeg", "webp", "gif"
    );
    private static final Map<String, String> CONTENT_TYPES = Map.ofEntries(
            Map.entry("pdf", "application/pdf"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("ppt", "application/vnd.ms-powerpoint"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            Map.entry("odt", "application/vnd.oasis.opendocument.text"),
            Map.entry("ods", "application/vnd.oasis.opendocument.spreadsheet"),
            Map.entry("odp", "application/vnd.oasis.opendocument.presentation"),
            Map.entry("txt", "text/plain"),
            Map.entry("html", "text/html"),
            Map.entry("htm", "text/html"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("webp", "image/webp"),
            Map.entry("gif", "image/gif")
    );

    private final Path directory;

    public DocumentFileStorageService(@Value("${app.documents.dir}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public UploadedDocument save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choisissez un fichier à importer");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formats acceptés : PDF, Word, Excel, PowerPoint, OpenDocument, HTML, TXT ou image"
            );
        }
        try {
            Files.createDirectories(directory);
            String storedFile = UUID.randomUUID() + "." + extension;
            Path target = directory.resolve(storedFile).normalize();
            if (!target.startsWith(directory)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
            }
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String originalName = originalName(file.getOriginalFilename(), extension);
            String contentType = contentType(storedFile, file.getContentType());
            return new UploadedDocument(storedFile, originalName, contentType);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Enregistrement du fichier impossible");
        }
    }

    public Resource load(String storedFile) {
        Path file = resolve(storedFile);
        if (!Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
        }
        return new FileSystemResource(file);
    }

    public String contentType(String storedFile, String fallback) {
        String extension = extensionOf(storedFile);
        String mapped = CONTENT_TYPES.get(extension);
        if (mapped != null) {
            return mapped;
        }
        if (fallback != null && !fallback.isBlank() && !"application/octet-stream".equalsIgnoreCase(fallback)) {
            return fallback;
        }
        return "application/octet-stream";
    }

    public void delete(String storedFile) {
        if (storedFile == null || storedFile.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(storedFile));
        } catch (Exception ignored) {
            // best effort: a missing file must not block the admin save
        }
    }

    public void replace(String previous, String next) {
        if (previous == null || previous.isBlank() || previous.equals(next)) {
            return;
        }
        delete(previous);
    }

    private Path resolve(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        }
        Path file = directory.resolve(filename).normalize();
        if (!file.startsWith(directory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        }
        return file;
    }

    private String originalName(String originalFilename, String extension) {
        String name = originalFilename == null ? "" : Path.of(originalFilename).getFileName().toString().trim();
        if (name.isBlank()) {
            return "document." + extension;
        }
        return name.length() > 180 ? name.substring(0, 180) : name;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
