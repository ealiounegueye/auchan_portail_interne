package sn.auchan.portail.dto;

import sn.auchan.portail.domain.Department;

public record DepartmentResponse(Long id, String name, String code, String description, int sortOrder) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getSortOrder()
        );
    }
}
