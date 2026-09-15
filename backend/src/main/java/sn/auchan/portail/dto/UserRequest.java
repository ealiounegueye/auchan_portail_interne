package sn.auchan.portail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import sn.auchan.portail.domain.Role;

public record UserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String password,
        Role role,
        @NotBlank String department,
        Boolean active,
        Long managerId,
        Boolean restrictedAccess,
        List<Long> allowedApplicationIds,
        List<AppAccessGrant> applicationGrants
) {
}
