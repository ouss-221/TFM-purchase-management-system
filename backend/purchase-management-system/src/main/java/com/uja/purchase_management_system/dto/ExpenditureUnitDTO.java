package com.uja.purchase_management_system.dto;

import jakarta.validation.constraints.NotBlank;

public class ExpenditureUnitDTO {
    private Long id;
    @NotBlank private String name;
    @NotBlank private String code;
    private Long departmentId;
    private String departmentName;
    private Long responsibleUserId;
    private String responsibleUsername;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getResponsibleUserId() { return responsibleUserId; }
    public void setResponsibleUserId(Long responsibleUserId) { this.responsibleUserId = responsibleUserId; }
    public String getResponsibleUsername() { return responsibleUsername; }
    public void setResponsibleUsername(String responsibleUsername) { this.responsibleUsername = responsibleUsername; }
}