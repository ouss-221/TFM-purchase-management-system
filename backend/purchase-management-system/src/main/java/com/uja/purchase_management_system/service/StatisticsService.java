package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.StatisticEntryDTO;
import java.util.List;

public interface StatisticsService {
    List<StatisticEntryDTO> bySupplier();
    List<StatisticEntryDTO> byProductType();
    List<StatisticEntryDTO> byExpenditureUnit();
    List<StatisticEntryDTO> byPeriod();
}