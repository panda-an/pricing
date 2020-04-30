package com.lenovo.vro.pricing.service.async;

import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.FutureBean;
import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.mapper.ext.WarrantyMapperExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

@Component
public class AsyncThreadProcess {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Async("asyncServiceExecutor")
    @Transactional(rollbackFor = Exception.class)
    public Future<FutureBean> processWarrantyThread(WarrantyMapperExt warrantyMapperExt, List<Warranty> list, CountDownLatch latch) {

        try {
            warrantyMapperExt.insertBatch(list);

            logger.info("Insert warranty success for size {}", list.size());
            FutureBean bean = new FutureBean();
            bean.setStatus(CodeConfig.OPERATION_SUCCESS);
            bean.setMessage("Insert Into warranty Data For size: " + list.size() + " Success");

            return new AsyncResult<>(bean);
        } catch (Exception e) {
            logger.error("Error Insert Into warranty Because： {}", e.getMessage());
            throw e;
        } finally {
            latch.countDown();
        }
    }

}
