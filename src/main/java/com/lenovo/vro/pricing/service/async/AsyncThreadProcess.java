package com.lenovo.vro.pricing.service.async;

import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.CostTapeEo;
import com.lenovo.vro.pricing.entity.CostTapeGsc;
import com.lenovo.vro.pricing.entity.FutureBean;
import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.mapper.ext.CostTapeEoMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeGscMapperExt;
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

    @Async("asyncServiceExecutor")
    @Transactional(rollbackFor = Exception.class)
    public Future<FutureBean> processCostTapeEoThread(CostTapeEoMapperExt costTapeEoMapperExt, List<CostTapeEo> list, CountDownLatch latch) {

        try {
            costTapeEoMapperExt.insertBatch(list);

            logger.info("Insert cost tape eo mapping data success for size {}", list.size());
            FutureBean bean = new FutureBean();
            bean.setStatus(CodeConfig.OPERATION_SUCCESS);
            bean.setMessage("Insert Into cost tape eo mapping data For size: " + list.size() + " Success");

            return new AsyncResult<>(bean);
        } catch (Exception e) {
            logger.error("Error Insert Into cost tape eo mapping data Because： {}", e.getMessage());
            throw e;
        } finally {
            latch.countDown();
        }
    }

    @Async("asyncServiceExecutor")
    @Transactional(rollbackFor = Exception.class)
    public Future<FutureBean> processCostTapeGscThread(CostTapeGscMapperExt costTapeGscMapperExt, List<CostTapeGsc> list, CountDownLatch latch) {

        try {
            costTapeGscMapperExt.insertBatch(list);

            logger.info("Insert cost tape gsc mapping data success for size {}", list.size());
            FutureBean bean = new FutureBean();
            bean.setStatus(CodeConfig.OPERATION_SUCCESS);
            bean.setMessage("Insert Into cost tape gsc mapping data For size: " + list.size() + " Success");

            return new AsyncResult<>(bean);
        } catch (Exception e) {
            logger.error("Error Insert Into cost tape gsc mapping data Because： {}", e.getMessage());
            throw e;
        } finally {
            latch.countDown();
        }
    }
}
