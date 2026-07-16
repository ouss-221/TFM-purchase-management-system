package com.uja.purchase_management_system.config;

import com.uja.purchase_management_system.entity.*;
import com.uja.purchase_management_system.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final ExpenditureUnitRepository expenditureUnitRepository;
    private final SupplierRepository supplierRepository;
    private final ProductTypeRepository productTypeRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DepartmentRepository departmentRepository,
                            UserRepository userRepository,
                            ExpenditureUnitRepository expenditureUnitRepository,
                            SupplierRepository supplierRepository,
                            ProductTypeRepository productTypeRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.expenditureUnitRepository = expenditureUnitRepository;
        this.supplierRepository = supplierRepository;
        this.productTypeRepository = productTypeRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("Data already initialized, skipping seed.");
            return;
        }

        Department telecom = departmentRepository.save(
                new Department("Ingeniería de Telecomunicación", "Departamento de Telecomunicación"));

        User admin = new User("admin", passwordEncoder.encode("admin123"),
                "admin@ujaen.es", "System Admin", Role.ADMIN, telecom);
        User teacher = new User("teacher1", passwordEncoder.encode("teacher123"),
                "teacher1@ujaen.es", "Juan Profesor", Role.TEACHER, telecom);
        User unitHead = new User("unithead1", passwordEncoder.encode("unithead123"),
                "unithead1@ujaen.es", "Maria Jefa de Unidad", Role.EXPENDITURE_UNIT_HEAD, telecom);
        User management = new User("manager1", passwordEncoder.encode("manager123"),
                "manager1@ujaen.es", "Carlos Gestor", Role.MANAGEMENT, telecom);

        userRepository.save(admin);
        userRepository.save(teacher);
        userRepository.save(unitHead);
        userRepository.save(management);

        ExpenditureUnit unit = expenditureUnitRepository.save(
                new ExpenditureUnit("Unidad de Gasto Telecomunicación", "UGT-001", telecom, unitHead));

        Supplier supplier = supplierRepository.save(
                new Supplier("Electronica Jaén S.L.", "B12345678",
                        "ventas@electronicajaen.es", "953000000", "Calle Ejemplo 1, Jaén"));

        ProductType pt = productTypeRepository.save(new ProductType("Material Informático"));

PurchaseOrder order = new PurchaseOrder();
        order.setOrderNumber("PO-2026-0001");
        order.setRequestDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setRequestedBy(teacher);
        order.setExpenditureUnit(unit);
        order.setPeriod("2026-Q1");
        order.setNotes("Pedido de prueba inicial");

        OrderItem item = new OrderItem();
        item.setPurchaseOrder(order);
        item.setProductName("Router WiFi 6");
        item.setDescription("Router para laboratorio");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("89.99"));
        item.setProductType(pt);
        item.setSupplier(supplier);
        order.getItems().add(item);

        purchaseOrderRepository.save(order);

        System.out.println("=== Seed data created ===");
        System.out.println("Login as: admin/admin123, teacher1/teacher123, unithead1/unithead123, manager1/manager123");
    }
}
