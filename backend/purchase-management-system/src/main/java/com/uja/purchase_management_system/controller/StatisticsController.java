package com.uja.purchase_management_system.controller;

import com.uja.purchase_management_system.dto.StatisticEntryDTO;
import com.uja.purchase_management_system.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService service;

    public StatisticsController(StatisticsService service) { this.service = service; }

    @GetMapping("/by-supplier")
    public List<StatisticEntryDTO> bySupplier() { return service.bySupplier(); }

    @GetMapping("/by-product-type")
    public List<StatisticEntryDTO> byProductType() { return service.byProductType(); }

    @GetMapping("/by-expenditure-unit")
    public List<StatisticEntryDTO> byExpenditureUnit() { return service.byExpenditureUnit(); }

    @GetMapping("/by-period")
    public List<StatisticEntryDTO> byPeriod() { return service.byPeriod(); }
}