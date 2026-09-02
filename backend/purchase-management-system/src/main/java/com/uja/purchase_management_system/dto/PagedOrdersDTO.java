package com.uja.purchase_management_system.dto;

import java.util.List;

public class PagedOrdersDTO {
    private List<PurchaseOrderDTO> orders;
    private int currentPage;
    private int totalPages;
    private long totalOrders;

    public PagedOrdersDTO() {}

    public PagedOrdersDTO(List<PurchaseOrderDTO> orders, int currentPage, int totalPages, long totalOrders) {
        this.orders = orders;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalOrders = totalOrders;
    }

    public List<PurchaseOrderDTO> getOrders() { return orders; }
    public void setOrders(List<PurchaseOrderDTO> orders) { this.orders = orders; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
}