package sn.auchan.portail.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank String name,
        String code,
        String description,
        Integer sortOrder
) {
}
