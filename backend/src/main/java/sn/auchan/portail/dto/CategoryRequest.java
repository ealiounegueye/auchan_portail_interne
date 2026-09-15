package sn.auchan.portail.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        String slug,
        @NotBlank String icon,
        @NotBlank String color,
        Integer sortOrder
) {
}
