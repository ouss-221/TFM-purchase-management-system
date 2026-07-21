package com.uja.purchase_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber; // "Pedido"

    @Column(unique = true)
    private String fileNumber; // "Expediente"

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    private String requesterPhone;

    @ManyToOne
    @JoinColumn(name = "expenditure_unit_id", nullable = false)
    private ExpenditureUnit expenditureUnit;

    private String budgetLineCode; // "Aplicación presupuestaria"

    private String period;

    @Column(length = 2000)
    private String notes;

    private String deliveryBuilding;
    private String deliveryRoom;
    private String deliveryPhone;
    private String deliveryContactPerson;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public PurchaseOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getFileNumber() { return fileNumber; }
    public void setFileNumber(String fileNumber) { this.fileNumber = fileNumber; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }
    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }
    public ExpenditureUnit getExpenditureUnit() { return expenditureUnit; }
    public void setExpenditureUnit(ExpenditureUnit expenditureUnit) { this.expenditureUnit = expenditureUnit; }
    public String getBudgetLineCode() { return budgetLineCode; }
    public void setBudgetLineCode(String budgetLineCode) { this.budgetLineCode = budgetLineCode; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDeliveryBuilding() { return deliveryBuilding; }
    public void setDeliveryBuilding(String deliveryBuilding) { this.deliveryBuilding = deliveryBuilding; }
    public String getDeliveryRoom() { return deliveryRoom; }
    public void setDeliveryRoom(String deliveryRoom) { this.deliveryRoom = deliveryRoom; }
    public String getDeliveryPhone() { return deliveryPhone; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }
    public String getDeliveryContactPerson() { return deliveryContactPerson; }
    public void setDeliveryContactPerson(String deliveryContactPerson) { this.deliveryContactPerson = deliveryContactPerson; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}