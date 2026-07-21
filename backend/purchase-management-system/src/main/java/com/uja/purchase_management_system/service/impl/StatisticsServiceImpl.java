package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.StatisticEntryDTO;
import com.uja.purchase_management_system.repository.PurchaseOrderRepository;
import com.uja.purchase_management_system.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final PurchaseOrderRepository repository;

    public StatisticsServiceImpl(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StatisticEntryDTO> bySupplier() { return map(repository.sumBySupplier()); }

    @Override
    public List<StatisticEntryDTO> byExpenditureUnit() { return map(repository.sumByExpenditureUnit()); }

    @Override
    public List<StatisticEntryDTO> byPeriod() { return map(repository.sumByPeriod()); }

    private List<StatisticEntryDTO> map(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new StatisticEntryDTO((String) r[0], (BigDecimal) r[1], (Long) r[2]))
                .collect(Collectors.toList());
    }
}