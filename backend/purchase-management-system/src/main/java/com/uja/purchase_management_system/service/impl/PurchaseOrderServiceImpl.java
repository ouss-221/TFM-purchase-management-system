package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.ItemSearchResultDTO;
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
    private final DocumentSigningServiceImpl documentSigningService;

    public PurchaseOrderServiceImpl(PurchaseOrderRepository orderRepository,
                                     UserRepository userRepository,
                                     ExpenditureUnitRepository unitRepository,
                                     AccessLogRepository accessLogRepository,
                                     DocumentSigningServiceImpl documentSigningService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
        this.accessLogRepository = accessLogRepository;
        this.documentSigningService = documentSigningService;
    }

    @Override
    public List<PurchaseOrderDTO> findAllForUser(String username) {
        User user = getUser(username);
        return getScopedOrders(user).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public PurchaseOrderDTO findById(Long id, String username) {
        User user = getUser(username);
        PurchaseOrder order = getOrder(id);
        checkViewScope(user, order);
        return toDTO(order);
    }

    @Override
    public List<ItemSearchResultDTO> searchItems(String productTerm, String username) {
        User user = getUser(username);
        List<PurchaseOrder> scoped = getScopedOrders(user);

        return scoped.stream()
            .flatMap(o -> o.getItems().stream()
                .filter(i -> i.getProductName() != null &&
                        i.getProductName().toLowerCase().contains(productTerm.toLowerCase()))
                .map(i -> toSearchResultDTO(o, i)))
            .collect(Collectors.toList());
    }

    // Shared scoping logic used by both findAllForUser and searchItems, so
    // there is one single source of truth for "which orders can this user see."
    private List<PurchaseOrder> getScopedOrders(User user) {
        if (user.getRole() == Role.TEACHER) {
            return orderRepository.findByRequestedBy_Id(user.getId());
        } else if (user.getRole() == Role.EXPENDITURE_UNIT_HEAD) {
            return orderRepository.findAll().stream()
                    .filter(o -> o.getExpenditureUnit().getResponsible() != null
                            && o.getExpenditureUnit().getResponsible().getId().equals(user.getId()))
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.MANAGEMENT) {
            return orderRepository.findAll().stream()
                    .filter(o -> user.getDepartment() != null
                            && o.getExpenditureUnit().getDepartment() != null
                            && o.getExpenditureUnit().getDepartment().getId().equals(user.getDepartment().getId()))
                    .collect(Collectors.toList());
        } else {
            return orderRepository.findAll();
        }
    }

    private void checkViewScope(User user, PurchaseOrder order) {
        switch (user.getRole()) {
            case TEACHER:
                if (!order.getRequestedBy().getId().equals(user.getId())) {
                    throw new AccessDeniedException("You can only view your own purchase orders");
                }
                break;
            case EXPENDITURE_UNIT_HEAD:
                User responsible = order.getExpenditureUnit().getResponsible();
                if (responsible == null || !responsible.getId().equals(user.getId())) {
                    throw new AccessDeniedException("You can only view orders for your own expenditure unit");
                }
                break;
            case MANAGEMENT:
                Department orderDept = order.getExpenditureUnit().getDepartment();
                if (orderDept == null || user.getDepartment() == null
                        || !orderDept.getId().equals(user.getDepartment().getId())) {
                    throw new AccessDeniedException("You can only view orders within your own department");
                }
                break;
            case ADMIN:
                // no restriction
                break;
        }
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

        // FIRST signature: the requesting lecturer signs the order on submission.
        if (requester.getSigningAlias() != null) {
            try {
                byte[] signed = documentSigningService.generateAndSignByRequester(
                        saved, requester.getSigningAlias(), requester.getFullName());
                saved.setSignedDocument(signed);
                saved = orderRepository.save(saved);
            } catch (Exception e) {
                throw new RuntimeException("Failed to sign order on submission", e);
            }
        }

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

        // The order content changed while still PENDING, so the lecturer re-signs the updated document.
        if (order.getRequestedBy().getSigningAlias() != null) {
            try {
                byte[] signed = documentSigningService.generateAndSignByRequester(
                        order, order.getRequestedBy().getSigningAlias(), order.getRequestedBy().getFullName());
                order.setSignedDocument(signed);
            } catch (Exception e) {
                throw new RuntimeException("Failed to re-sign order after edit", e);
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

        // SECOND signature: when the authorizer approves, add their signature on top of the
        // lecturer's existing signature, producing a double-signed document.
        if (newStatus == OrderStatus.APPROVED
                && user.getSigningAlias() != null
                && order.getSignedDocument() != null) {
            try {
                byte[] doubleSigned = documentSigningService.addAuthorizerSignature(
                        order.getSignedDocument(), user.getSigningAlias(), user.getFullName());
                order.setSignedDocument(doubleSigned);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add authorizer signature", e);
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

    private ItemSearchResultDTO toSearchResultDTO(PurchaseOrder o, OrderItem i) {
        ItemSearchResultDTO dto = new ItemSearchResultDTO();
        dto.setOrderId(o.getId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setRequestDate(o.getRequestDate());
        dto.setStatus(o.getStatus().name());
        dto.setExpenditureUnitName(o.getExpenditureUnit().getName());
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