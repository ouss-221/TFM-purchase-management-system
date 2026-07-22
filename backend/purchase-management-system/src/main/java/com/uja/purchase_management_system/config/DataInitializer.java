package com.uja.purchase_management_system.config;

import com.uja.purchase_management_system.entity.*;
import com.uja.purchase_management_system.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                                DepartmentRepository departmentRepository,
                                ExpenditureUnitRepository expenditureUnitRepository,
                                PurchaseOrderRepository purchaseOrderRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                System.out.println("Data already initialized, skipping seed.");
                return;
            }

            // Department
            Department telecom = departmentRepository.save(
                    new Department("Ingeniería de Telecomunicación", "Departamento de Telecomunicación"));

            // Users
            User admin = new User("admin", passwordEncoder.encode("admin123"),
                    "admin@ujaen.es", "Administrator", Role.ADMIN, telecom);

            User teacher1 = new User("teacher1", passwordEncoder.encode("teacher123"),
                    "teacher1@ujaen.es", "Teacher One", Role.TEACHER, telecom);
            teacher1.setSigningAlias("sign-teacher1");

            User unithead1 = new User("unithead1", passwordEncoder.encode("unithead123"),
                    "unithead1@ujaen.es", "Juan Carlos Cuevas Martinez", Role.EXPENDITURE_UNIT_HEAD, telecom);
            unithead1.setSigningAlias("sign-unithead1");

            User manager1 = new User("manager1", passwordEncoder.encode("manager123"),
                    "manager1@ujaen.es", "Manager One", Role.MANAGEMENT, telecom);
            manager1.setSigningAlias("sign-manager1");

            userRepository.save(admin);
            userRepository.save(teacher1);
            userRepository.save(unithead1);
            userRepository.save(manager1);

            // Expenditure unit, with unithead1 as responsible
            ExpenditureUnit unit = expenditureUnitRepository.save(
                    new ExpenditureUnit("Unidad de Gasto Telecomunicación", "UGT-001", telecom, unithead1));

            // Sample order
            PurchaseOrder order = new PurchaseOrder();
            order.setOrderNumber("PO-2026-0001");
            order.setRequestDate(java.time.LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);
            order.setRequestedBy(teacher1);
            order.setExpenditureUnit(unit);
            order.setPeriod("2026-Q1");

            OrderItem item = new OrderItem();
            item.setPurchaseOrder(order);
            item.setProductName("Router WiFi 6");
            item.setDescription("Router para laboratorio");
            item.setQuantity(2);
            item.setUnitPrice(new BigDecimal("89.99"));
            item.setVatRate(new BigDecimal("21"));
            item.setSupplier("Electronica Jaén S.L.");
            order.getItems().add(item);

            purchaseOrderRepository.save(order);

            System.out.println("Seed data initialized.");
        };
    }
}