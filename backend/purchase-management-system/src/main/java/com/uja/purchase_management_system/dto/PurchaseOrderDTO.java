package com.uja.purchase_management_system.dto;

import com.uja.purchase_management_system.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseOrderDTO {
    private Long id;
    private String orderNumber;
    private LocalDateTime requestDate;
    private OrderStatus status;
    private String requestedByUsername;
    private Long expenditureUnitId;
    private String expenditureUnitName;
    private String period;
    private String notes;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;

    public PurchaseOrderDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getRequestedByUsername() { return requestedByUsername; }
    public void setRequestedByUsername(String requestedByUsername) { this.requestedByUsername = requestedByUsername; }
    public Long getExpenditureUnitId() { return expenditureUnitId; }
    public void setExpenditureUnitId(Long expenditureUnitId) { this.expenditureUnitId = expenditureUnitId; }
    public String getExpenditureUnitName() { return expenditureUnitName; }
    public void setExpenditureUnitName(String expenditureUnitName) { this.expenditureUnitName = expenditureUnitName; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}