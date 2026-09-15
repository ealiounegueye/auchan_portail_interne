package sn.auchan.portail.dto;

import org.springframework.core.io.Resource;

public record StoredDocumentFile(Resource resource, String originalName, String contentType) {
}
