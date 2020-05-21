package com.lenovo.vro.pricing.task;

import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.service.db.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 1 2 * ? ")
    public void insertWarranty() {
        logger.info("Start schedule for insertWarranty");
        String code1 = "";
        try {
            code1 = dbService.insertWarranty();
            if(code1.equals(CodeConfig.OPERATION_FAILED)) {
                logger.error("Can not load warranty data file!");
            } else {
                redisTemplate.delete("warranty");
                logger.info("Load warranty data file process success!");
            }
        } catch (FileNotFoundException e) {
            logger.error("Can not load warranty data file!");
            e.printStackTrace();
        }

        logger.info("End schedule for insertWarranty");
    }

}
