package com.uja.purchase_management_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "expenditure_units")
public class ExpenditureUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "responsible_user_id")
    private User responsible;

    public ExpenditureUnit() {}

    public ExpenditureUnit(String name, String code, Department department, User responsible) {
        this.name = name;
        this.code = code;
        this.department = department;
        this.responsible = responsible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public User getResponsible() { return responsible; }
    public void setResponsible(User responsible) { this.responsible = responsible; }
}