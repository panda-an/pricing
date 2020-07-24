package com.lenovo.vro.pricing.task;

import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.service.db.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.FileNotFoundException;

@Component
@EnableScheduling
public class DbScheduleTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DbService dbService;


    @Scheduled(cron = "0 0 1 2 * ? ")
    public void insertWarranty() {
        logger.info("Start schedule for insertWarranty");
        String code1 = "";
        try {
            code1 = dbService.insertWarranty();
            if(code1.equals(CodeConfig.OPERATION_FAILED)) {
                logger.error("Can not load warranty data file!");
            } else {
                logger.info("Load warranty data file process success!");
            }
        } catch (FileNotFoundException e) {
            logger.error("Can not load warranty data file!");
            e.printStackTrace();
        }

        logger.info("End schedule for insertWarranty");
    }

    @Scheduled(cron = "0 0 2 2 * ? ")
    public void insertCostTapeEo() {
        logger.info("Start schedule for insertCostTapeEo");
        String code1 = "";
        try {
            code1 = dbService.insertEo();
            if(code1.equals(CodeConfig.OPERATION_FAILED)) {
                logger.error("Can not load cost tape eo mapping data file!");
            } else {
                logger.info("Load cost tape eo mapping data file process success!");
            }
        } catch (FileNotFoundException e) {
            logger.error("Can not load cost tape eo mapping data file!");
            e.printStackTrace();
        }

        logger.info("End schedule for insertCostTapeEo");
    }

    @Scheduled(cron = "0 10 2 2 * ? ")
    public void insertCostTapeGsc() {
        logger.info("Start schedule for insertCostTapeGsc");
        String code1 = "";
        try {
            code1 = dbService.insertGsc();
            if(code1.equals(CodeConfig.OPERATION_FAILED)) {
                logger.error("Can not load cost tape gsc mapping data file!");
            } else {
                logger.info("Load cost tape gsc mapping data file process success!");
            }
        } catch (FileNotFoundException e) {
            logger.error("Can not load cost tape gsc mapping data file!");
            e.printStackTrace();
        }

        logger.info("End schedule for insertCostTapeGsc");
    }

    //@Scheduled(cron = "0 53 12 * * ? ")
    public void insertMbgFreightCost() {
        logger.info("Start schedule for insertMbgFreightCost");
        String code1 = "";
        try {
            code1 = dbService.insertMbgFreight();
            if(code1.equals(CodeConfig.OPERATION_FAILED)) {
                logger.error("Can not load mbg freight cost data file!");
            } else {
                logger.info("Load mbg freight cost data file process success!");
            }
        } catch (FileNotFoundException e) {
            logger.error("Can not load mbg freight cost data file!");
            e.printStackTrace();
        }

        logger.info("End schedule for insertMbgFreightCost");
    }
}