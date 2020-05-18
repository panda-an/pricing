package com.lenovo.vro.pricing.service.db.impl;

import com.google.common.collect.Lists;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.FutureBean;
import com.lenovo.vro.pricing.entity.Warranty;
import com.lenovo.vro.pricing.mapper.ext.WarrantyMapperExt;
import com.lenovo.vro.pricing.service.BaseService;
import com.lenovo.vro.pricing.service.async.AsyncThreadProcess;
import com.lenovo.vro.pricing.service.db.DbService;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DbServiceImpl extends BaseService implements DbService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WarrantyMapperExt warrantyMapperExt;

    @Autowired
    private AsyncThreadProcess asyncThreadProcess;

    @Override
    public String insertWarranty() throws FileNotFoundException {
        logger.info("************ Start load warranty data ************");
        final String FILE_PATH = "C:\\ftp\\data\\warranty\\";
        Path path = Paths.get(FILE_PATH);
        if(!Files.exists(path) || !Files.isDirectory(path)) {
            logger.error("Cant not find warranty data directory: {}", FILE_PATH);
            throw new FileNotFoundException("Cant not find warranty data directory!");
        }

        List<String> resultList = new ArrayList<>();

        String fileName = "";
        try {
            Stream<Path> fileList = Files.walk(path, 7);
            List<Path> list = fileList.filter(p -> !Files.isDirectory(p)).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(list)) {
                rollbackWarrantyData();
            }

            for(Path p : list) {
                fileName = p.toFile().getName();
                logger.info("Start load warranty data file: {}", fileName);

                String resultCode = loadWarrantyFile(p.toFile());
                resultList.add(resultCode);

                if(resultCode.equals(CodeConfig.OPERATION_SUCCESS)) {
                    //Files.delete(p);
                    logger.info("Load warranty data file: {} success", fileName);
                } else {
                    logger.error("Load warranty data file: {} error", fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Load Cost Type File: {} Error", fileName);
            resultList.add(CodeConfig.OPERATION_FAILED);
        }
        
        if(resultList.stream().anyMatch(n -> n.equals(CodeConfig.OPERATION_FAILED))) {
            logger.info("************ End load warranty data ************");
            return CodeConfig.OPERATION_FAILED;
        } else {
            logger.info("************ End load warranty data ************");
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    private String loadWarrantyFile(File file) {
        String resultCode = "";

        try (OPCPackage opcPackage = OPCPackage.open(file)) {

            XSSFReader reader = new XSSFReader(opcPackage);
            SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();
            StylesTable stylesTable = reader.getStylesTable();
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            WarrantySheetHandler handler = new WarrantySheetHandler();
            XSSFSheetXMLHandler xmlHandler = new XSSFSheetXMLHandler(stylesTable, sharedStringsTable, handler, new DataFormatter(), false);
            xmlReader.setContentHandler(xmlHandler);
            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) reader.getSheetsData();

            while (sheetIterator.hasNext()) {
                InputStream in = sheetIterator.next();
                InputSource source = new InputSource(in);
                xmlReader.parse(source);

                List<Warranty> dataList = handler.getDataList();
                if(file.getName().contains("TBG")) {
                    dataList.parallelStream().forEach(n -> n.setType(CodeConfig.WARRANTY_TYPE_TBG));
                } else if(file.getName().contains("LBG")) {
                    dataList.parallelStream().forEach(n -> n.setType(CodeConfig.WARRANTY_TYPE_LBG));
                }

                resultCode = insertDb(dataList);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
            logger.error("Read warranty data file {} error", file.getName());
            logger.error(e.getMessage());
        } catch (OpenXML4JException | SAXException e) {
            logger.error("Read warranty data file {} error!", file.getName());
            logger.error(e.getMessage());
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
        }

        return resultCode;
    }

    private String insertDb(List<Warranty> dataList) {
        List<List<Warranty>> warrantyDataList = Lists.partition(dataList, CodeConfig.LIST_NUMBER);

        List<Future<FutureBean>> futureList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(warrantyDataList.size());

        boolean allDone = true;

        try {
            for (List<Warranty> list : warrantyDataList) {
                Future<FutureBean> future = asyncThreadProcess.processWarrantyThread(warrantyMapperExt, list, countDownLatch);
                futureList.add(future);
            }

            countDownLatch.await();

            for(Future<FutureBean> future : futureList) {
                FutureBean bean = future.get();
                String status = bean.getStatus();
                if(StringUtils.isEmpty(status) || status.equalsIgnoreCase(CodeConfig.OPERATION_FAILED)) {
                    allDone = false;
                    break;
                }
            }
        } catch (Exception e) {
            allDone = false;
        }

        if(!allDone) {
            logger.error("Load warranty file Data Completed But Has Error, Will Roll back, Please Check Log");

            rollbackWarrantyData();
            return CodeConfig.OPERATION_FAILED;
        } else {
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    @Override
    public void insertCostTapeFamilyMapping() {

    }

    private void rollbackWarrantyData() {
        warrantyMapperExt.deleteAll();
    }

    public class WarrantySheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler{

        private Warranty warranty;
        private List<Warranty> dataList;

        Date date = getInsertDate();

        private List<Warranty> getDataList() {
            return dataList;
        }

        @Override
        public void startRow(int rowNum) {
            if(rowNum > 0) {
                warranty = new Warranty();
                warranty.setInsertTime(date);
            }
        }

        @Override
        public void endRow(int rowNum) {
            if(dataList == null) {
                dataList = new ArrayList<>();
            }

            if(warranty != null) {
                if(warranty.getTbaType().equals("US_DOLLAR")) {
                    dataList.add(warranty);
                }
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if(warranty != null) {
                String prefix = cellReference.replaceAll("\\d+", "");

                switch (prefix) {
                    case "A":
                        warranty.setWarrantyCode(formattedValue);
                        break;
                    case "B":
                        warranty.setCountry(formattedValue);
                        break;
                    case "C":
                        warranty.setTbaType(formattedValue);
                        break;
                    case "D":
                        warranty.setNbmc(StringUtils.isEmpty(formattedValue)?null:new BigDecimal(formattedValue));
                        break;
                }
            }
        }
    }
}
