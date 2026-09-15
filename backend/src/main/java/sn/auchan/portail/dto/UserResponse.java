package sn.auchan.portail.dto;

import java.util.List;
import sn.auchan.portail.domain.Role;
import sn.auchan.portail.domain.UserAccount;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        String department,
        boolean active,
        Long managerId,
        String managerName,
        boolean restrictedAccess,
        List<Long> allowedApplicationIds,
        List<AppAccessGrant> applicationGrants
) {
    public static UserResponse from(UserAccount user) {
        return from(user, List.of(), List.of());
    }

    public static UserResponse from(UserAccount user, List<Long> allowedApplicationIds) {
        return from(user, allowedApplicationIds, List.of());
    }

    public static UserResponse from(
            UserAccount user,
            List<Long> allowedApplicationIds,
            List<AppAccessGrant> applicationGrants
    ) {
        UserAccount manager = user.getManager();
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment(),
                user.isActive(),
                manager == null ? null : manager.getId(),
                manager == null ? null : manager.getFirstName() + " " + manager.getLastName(),
                user.isRestrictedAccess(),
                allowedApplicationIds == null ? List.of() : allowedApplicationIds,
                applicationGrants == null ? List.of() : applicationGrants
        );
    }
}
