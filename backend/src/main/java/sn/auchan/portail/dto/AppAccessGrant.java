package sn.auchan.portail.dto;

import sn.auchan.portail.domain.AppAccess;

public record AppAccessGrant(
        Long applicationId,
        Boolean canViewDocumentation,
        Boolean canViewTechnicalSheet,
        Boolean canViewUserGuide
) {
    public static AppAccessGrant from(AppAccess access) {
        return new AppAccessGrant(
                access.getApplication().getId(),
                access.isCanViewDocumentation(),
                access.isCanViewTechnicalSheet(),
                access.isCanViewUserGuide()
        );
    }

    public boolean documentation() {
        return canViewDocumentation == null || canViewDocumentation;
    }

    public boolean technicalSheet() {
        return Boolean.TRUE.equals(canViewTechnicalSheet);
    }

    public boolean userGuide() {
        return canViewUserGuide == null || canViewUserGuide;
    }
}
