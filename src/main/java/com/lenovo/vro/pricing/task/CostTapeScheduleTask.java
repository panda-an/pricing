package com.lenovo.vro.pricing.task;

import com.lenovo.vro.pricing.service.costtype.CostTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class CostTapeScheduleTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CostTypeService costTypeService;

    @Scheduled(cron = "0 49 14 ? * * ")
    public void createMonthlyReport() {
        logger.info("Start schedule for createMonthlyReport");
        costTypeService.getReportListMonthly();

        logger.info("End schedule for createMonthlyReport");
    }
}
