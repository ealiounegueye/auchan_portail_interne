package sn.auchan.portail.dto;

import sn.auchan.portail.domain.BusinessApp;

public record ApplicationResponse(
        Long id,
        String name,
        String description,
        String url,
        String icon,
        String logoUrl,
        CategoryResponse category,
        String ownerDepartment,
        String status,
        boolean featured,
        int sortOrder,
        boolean favorite,
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
        String audience,
        boolean canViewDocumentation,
        boolean canViewTechnicalSheet,
        boolean canViewUserGuide
) {
    public static ApplicationResponse from(BusinessApp app, boolean favorite) {
        return from(app, favorite, true, true, true);
    }

    public static ApplicationResponse from(
            BusinessApp app,
            boolean favorite,
            boolean canViewDocumentation,
            boolean canViewTechnicalSheet,
            boolean canViewUserGuide
    ) {
        return new ApplicationResponse(
                app.getId(),
                app.getName(),
                app.getDescription(),
                app.getUrl(),
                app.getIcon(),
                app.getLogoUrl(),
                CategoryResponse.from(app.getCategory()),
                app.getOwnerDepartment(),
                app.getStatus(),
                app.isFeatured(),
                app.getSortOrder(),
                favorite,
                app.getLongDescription(),
                canViewDocumentation ? app.getModop() : null,
                canViewTechnicalSheet ? app.getTechnicalSheetContent() : null,
                canViewUserGuide ? app.getUserGuideContent() : null,
                canViewDocumentation ? app.getDocumentationUrl() : null,
                canViewDocumentation ? app.getDocumentationStoredFile() : null,
                canViewDocumentation ? app.getDocumentationFileName() : null,
                canViewDocumentation ? app.getDocumentationContentType() : null,
                canViewTechnicalSheet ? app.getTechnicalSheetUrl() : null,
                canViewTechnicalSheet ? app.getTechnicalSheetStoredFile() : null,
                canViewTechnicalSheet ? app.getTechnicalSheetFileName() : null,
                canViewTechnicalSheet ? app.getTechnicalSheetContentType() : null,
                canViewUserGuide ? app.getUserGuideUrl() : null,
                canViewUserGuide ? app.getUserGuideStoredFile() : null,
                canViewUserGuide ? app.getUserGuideFileName() : null,
                canViewUserGuide ? app.getUserGuideContentType() : null,
                app.getSupportUrl(),
                app.getSupportContact(),
                app.getVersion(),
                app.getAudience(),
                canViewDocumentation,
                canViewTechnicalSheet,
                canViewUserGuide
        );
    }
}
