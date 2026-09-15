package sn.auchan.portail.dto;

import java.util.List;

public record AccessRequest(
        Boolean restrictedAccess,
        List<Long> allowedApplicationIds,
        List<AppAccessGrant> applicationGrants
) {
}
