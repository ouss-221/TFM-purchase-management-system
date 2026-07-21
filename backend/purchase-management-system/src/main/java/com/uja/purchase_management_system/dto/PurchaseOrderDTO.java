package com.uja.purchase_management_system.dto;

import com.uja.purchase_management_system.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseOrderDTO {
    private Long id;
    private String orderNumber;
    private String fileNumber;
    private LocalDateTime requestDate;
    private OrderStatus status;
    private String requestedByUsername;

    @Size(max = 30, message = "Phone number must be at most 30 characters")
    private String requesterPhone;

    @NotNull(message = "Expenditure unit is required")
    private Long expenditureUnitId;
    private String expenditureUnitName;
    private String expenditureUnitCode;

    @Size(max = 60, message = "Budget line code must be at most 60 characters")
    private String budgetLineCode;

    private String period;

    @Size(max = 2000, message = "Notes must be at most 2000 characters")
    private String notes;

    @Size(max = 100)
    private String deliveryBuilding;
    @Size(max = 100)
    private String deliveryRoom;
    @Size(max = 30)
    private String deliveryPhone;
    @Size(max = 150)
    private String deliveryContactPerson;

    private BigDecimal totalAmount;

    @NotEmpty(message = "An order must have at least one line item")
    @Valid
    private List<OrderItemDTO> items;

    public PurchaseOrderDTO() {}

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
    public String getRequestedByUsername() { return requestedByUsername; }
    public void setRequestedByUsername(String requestedByUsername) { this.requestedByUsername = requestedByUsername; }
    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }
    public Long getExpenditureUnitId() { return expenditureUnitId; }
    public void setExpenditureUnitId(Long expenditureUnitId) { this.expenditureUnitId = expenditureUnitId; }
    public String getExpenditureUnitName() { return expenditureUnitName; }
    public void setExpenditureUnitName(String expenditureUnitName) { this.expenditureUnitName = expenditureUnitName; }
    public String getExpenditureUnitCode() { return expenditureUnitCode; }
    public void setExpenditureUnitCode(String expenditureUnitCode) { this.expenditureUnitCode = expenditureUnitCode; }
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
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}