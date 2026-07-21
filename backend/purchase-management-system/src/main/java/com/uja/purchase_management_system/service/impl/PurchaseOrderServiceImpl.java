package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.OrderItemDTO;
import com.uja.purchase_management_system.dto.PurchaseOrderDTO;
import com.uja.purchase_management_system.entity.*;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.*;
import com.uja.purchase_management_system.service.PurchaseOrderService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ExpenditureUnitRepository unitRepository;
    private final AccessLogRepository accessLogRepository;

    public PurchaseOrderServiceImpl(PurchaseOrderRepository orderRepository,
                                     UserRepository userRepository,
                                     ExpenditureUnitRepository unitRepository,
                                     AccessLogRepository accessLogRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
        this.accessLogRepository = accessLogRepository;
    }

    @Override
    public List<PurchaseOrderDTO> findAllForUser(String username) {
        User user = getUser(username);

        List<PurchaseOrder> orders;
        if (user.getRole() == Role.TEACHER) {
            orders = orderRepository.findByRequestedBy_Id(user.getId());
        } else if (user.getRole() == Role.EXPENDITURE_UNIT_HEAD) {
            orders = orderRepository.findAll().stream()
                    .filter(o -> o.getExpenditureUnit().getResponsible() != null
                            && o.getExpenditureUnit().getResponsible().getId().equals(user.getId()))
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.MANAGEMENT) {
            orders = orderRepository.findAll().stream()
                    .filter(o -> user.getDepartment() != null
                            && o.getExpenditureUnit().getDepartment() != null
                            && o.getExpenditureUnit().getDepartment().getId().equals(user.getDepartment().getId()))
                    .collect(Collectors.toList());
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public PurchaseOrderDTO findById(Long id) {
        return toDTO(getOrder(id));
    }

    @Override
    @Transactional
    public PurchaseOrderDTO create(PurchaseOrderDTO dto, String username) {
        User requester = getUser(username);

        ExpenditureUnit unit = unitRepository.findById(dto.getExpenditureUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Expenditure unit not found: " + dto.getExpenditureUnitId()));

        long seq = System.currentTimeMillis() % 100000;
        int year = LocalDateTime.now().getYear();

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNumber("PO-" + year + "-" + seq);
        order.setFileNumber(year + "/" + String.format("%05d", seq));
        order.setRequestDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setRequestedBy(requester);
        order.setRequesterPhone(dto.getRequesterPhone());
        order.setExpenditureUnit(unit);
        order.setBudgetLineCode(dto.getBudgetLineCode());
        order.setPeriod(dto.getPeriod());
        order.setNotes(dto.getNotes());
        order.setDeliveryBuilding(dto.getDeliveryBuilding());
        order.setDeliveryRoom(dto.getDeliveryRoom());
        order.setDeliveryPhone(dto.getDeliveryPhone());
        order.setDeliveryContactPerson(dto.getDeliveryContactPerson());

        if (dto.getItems() != null) {
            for (OrderItemDTO itemDTO : dto.getItems()) {
                order.getItems().add(buildItem(order, itemDTO));
            }
        }

        PurchaseOrder saved = orderRepository.save(order);
        log(requester, "CREATE_ORDER", saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto, String username) {
        User user = getUser(username);
        PurchaseOrder order = getOrder(id);

        boolean isOwner = order.getRequestedBy().getId().equals(user.getId());
        if (!isOwner && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You can only edit your own purchase orders");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This order can no longer be edited: it has already been " + order.getStatus());
        }

        ExpenditureUnit unit = unitRepository.findById(dto.getExpenditureUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Expenditure unit not found: " + dto.getExpenditureUnitId()));

        order.setExpenditureUnit(unit);
        order.setRequesterPhone(dto.getRequesterPhone());
        order.setBudgetLineCode(dto.getBudgetLineCode());
        order.setPeriod(dto.getPeriod());
        order.setNotes(dto.getNotes());
        order.setDeliveryBuilding(dto.getDeliveryBuilding());
        order.setDeliveryRoom(dto.getDeliveryRoom());
        order.setDeliveryPhone(dto.getDeliveryPhone());
        order.setDeliveryContactPerson(dto.getDeliveryContactPerson());

        order.getItems().clear();
        if (dto.getItems() != null) {
            for (OrderItemDTO itemDTO : dto.getItems()) {
                order.getItems().add(buildItem(order, itemDTO));
            }
        }

        PurchaseOrder saved = orderRepository.save(order);
        log(user, "UPDATE_ORDER", saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO updateStatus(Long id, OrderStatus newStatus, String username) {
        User user = getUser(username);
        PurchaseOrder order = getOrder(id);

        if (user.getRole() == Role.MANAGEMENT) {
            Department orderDept = order.getExpenditureUnit().getDepartment();
            if (orderDept == null || user.getDepartment() == null
                    || !orderDept.getId().equals(user.getDepartment().getId())) {
                throw new AccessDeniedException("You can only approve orders within your own department");
            }
        } else if (user.getRole() == Role.EXPENDITURE_UNIT_HEAD) {
            User responsible = order.getExpenditureUnit().getResponsible();
            if (responsible == null || !responsible.getId().equals(user.getId())) {
                throw new AccessDeniedException("You can only approve orders for your own expenditure unit");
            }
        }

        order.setStatus(newStatus);
        PurchaseOrder saved = orderRepository.save(order);
        log(user, "UPDATE_STATUS_" + newStatus, saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Purchase order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderItem buildItem(PurchaseOrder order, OrderItemDTO itemDTO) {
        OrderItem item = new OrderItem();
        item.setPurchaseOrder(order);
        item.setProductName(itemDTO.getProductName());
        item.setDescription(itemDTO.getDescription());
        item.setQuantity(itemDTO.getQuantity());
        item.setUnitPrice(itemDTO.getUnitPrice());
        item.setVatRate(itemDTO.getVatRate() != null ? itemDTO.getVatRate() : BigDecimal.ZERO);
        item.setSupplier(itemDTO.getSupplier());
        return item;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private PurchaseOrder getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
    }

    private void log(User user, String action, Long orderId) {
        AccessLog log = new AccessLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntity("PurchaseOrder");
        log.setEntityId(orderId);
        accessLogRepository.save(log);
    }

    private PurchaseOrderDTO toDTO(PurchaseOrder o) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(o.getId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setFileNumber(o.getFileNumber());
        dto.setRequestDate(o.getRequestDate());
        dto.setStatus(o.getStatus());
        dto.setRequestedByUsername(o.getRequestedBy().getUsername());
        dto.setRequesterPhone(o.getRequesterPhone());
        dto.setExpenditureUnitId(o.getExpenditureUnit().getId());
        dto.setExpenditureUnitName(o.getExpenditureUnit().getName());
        dto.setExpenditureUnitCode(o.getExpenditureUnit().getCode());
        dto.setBudgetLineCode(o.getBudgetLineCode());
        dto.setPeriod(o.getPeriod());
        dto.setNotes(o.getNotes());
        dto.setDeliveryBuilding(o.getDeliveryBuilding());
        dto.setDeliveryRoom(o.getDeliveryRoom());
        dto.setDeliveryPhone(o.getDeliveryPhone());
        dto.setDeliveryContactPerson(o.getDeliveryContactPerson());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setItems(o.getItems().stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    private OrderItemDTO toItemDTO(OrderItem i) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(i.getId());
        dto.setProductName(i.getProductName());
        dto.setDescription(i.getDescription());
        dto.setQuantity(i.getQuantity());
        dto.setUnitPrice(i.getUnitPrice());
        dto.setVatRate(i.getVatRate());
        dto.setLineTotal(i.getLineTotal());
        dto.setSupplier(i.getSupplier());
        return dto;
    }
}