package sn.auchan.portail.dto;

import sn.auchan.portail.domain.Category;

public record CategoryResponse(Long id, String name, String slug, String icon, String color, int sortOrder) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getIcon(),
                category.getColor(),
                category.getSortOrder()
        );
    }
}
