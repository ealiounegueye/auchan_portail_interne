package sn.auchan.portail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String url,
        @NotBlank String icon,
        String logoUrl,
        @NotNull Long categoryId,
        @NotBlank String ownerDepartment,
        String status,
        Boolean featured,
        Integer sortOrder,
        String longDescription,
        String modop,
        String technicalSheetContent,
        String userGuideContent,
        String documentationUrl,
        String documentationStoredFile,
        String documentationFileName,
        String documentationContentType,
        String technicalSheetUrl,
        String technicalSheetStoredFile,
        String technicalSheetFileName,
        String technicalSheetContentType,
        String userGuideUrl,
        String userGuideStoredFile,
        String userGuideFileName,
        String userGuideContentType,
        String supportUrl,
        String supportContact,
        String version,
        String audience
) {
}
