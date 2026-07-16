package com.uja.purchase_management_system.dto;

import java.math.BigDecimal;

public class StatisticEntryDTO {
    private String label;
    private BigDecimal totalAmount;
    private long orderCount;

    public StatisticEntryDTO() {}

    public StatisticEntryDTO(String label, BigDecimal totalAmount, long orderCount) {
        this.label = label;
        this.totalAmount = totalAmount;
        this.orderCount = orderCount;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
}