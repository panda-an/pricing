package com.lenovo.vro.pricing.service.db.impl;

import com.google.common.collect.Lists;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.*;
import com.lenovo.vro.pricing.mapper.ext.CostTapeEoMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeGscMapperExt;
import com.lenovo.vro.pricing.mapper.ext.MbgFreightCostMapperExt;
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
    private CostTapeGscMapperExt costTapeGscMapperExt;

    @Autowired
    private CostTapeEoMapperExt costTapeEoMapperExt;

    @Autowired
    private MbgFreightCostMapperExt mbgFreightCostMapperExt;

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

    @Override
    public String insertEo() throws FileNotFoundException {
        logger.info("************ Start load cost tape eo mapping data ************");
        final String FILE_PATH = "C:\\ftp\\data\\price_mapping\\eo\\";
        Path path = Paths.get(FILE_PATH);
        if(!Files.exists(path) || !Files.isDirectory(path)) {
            logger.error("Cant not find cost tape eo mapping data directory: {}", FILE_PATH);
            throw new FileNotFoundException("Cant not find cost tape eo mapping data directory!");
        }

        List<String> resultList = new ArrayList<>();

        String fileName = "";
        try {
            Stream<Path> fileList = Files.walk(path, 7);
            List<Path> list = fileList.filter(p -> !Files.isDirectory(p)).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(list)) {
                rollbackCostTapeEoData();
            }

            for(Path p : list) {
                fileName = p.toFile().getName();
                logger.info("Start load warranty data file: {}", fileName);

                String resultCode = loadCostTapeEoFile(p.toFile());
                resultList.add(resultCode);

                if(resultCode.equals(CodeConfig.OPERATION_SUCCESS)) {
                    //Files.delete(p);
                    logger.info("Load cost tape eo mapping data file: {} success", fileName);
                } else {
                    logger.error("Load cost tape eo mapping data file: {} error", fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Load cost tape eo mapping File: {} Error", fileName);
            resultList.add(CodeConfig.OPERATION_FAILED);
        }

        if(resultList.stream().anyMatch(n -> n.equals(CodeConfig.OPERATION_FAILED))) {
            logger.info("************ End load cost tape eo mapping data ************");
            return CodeConfig.OPERATION_FAILED;
        } else {
            logger.info("************ End load cost tape eo mapping data ************");
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    private String loadCostTapeEoFile(File file) {
        String resultCode = "";

        try (OPCPackage opcPackage = OPCPackage.open(file)) {

            XSSFReader reader = new XSSFReader(opcPackage);
            SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();
            StylesTable stylesTable = reader.getStylesTable();
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            CostTapeEoSheetHandler handler = new CostTapeEoSheetHandler();
            XSSFSheetXMLHandler xmlHandler = new XSSFSheetXMLHandler(stylesTable, sharedStringsTable, handler, new DataFormatter(), false);
            xmlReader.setContentHandler(xmlHandler);
            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) reader.getSheetsData();

            while (sheetIterator.hasNext()) {
                InputStream in = sheetIterator.next();
                InputSource source = new InputSource(in);
                xmlReader.parse(source);

                List<CostTapeEo> dataList = handler.getDataList();
                resultCode = insertCostTapeEoMappingDb(dataList);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
            logger.error("Read cost tape eo mapping data file {} error", file.getName());
            logger.error(e.getMessage());
        } catch (OpenXML4JException | SAXException e) {
            logger.error("Read cost tape eo mapping data file {} error!", file.getName());
            logger.error(e.getMessage());
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
        }

        return resultCode;
    }

    private String insertCostTapeEoMappingDb(List<CostTapeEo> dataList) {
        List<List<CostTapeEo>> warrantyDataList = Lists.partition(dataList, CodeConfig.LIST_NUMBER);

        List<Future<FutureBean>> futureList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(warrantyDataList.size());

        boolean allDone = true;

        try {
            for (List<CostTapeEo> list : warrantyDataList) {
                Future<FutureBean> future = asyncThreadProcess.processCostTapeEoThread(costTapeEoMapperExt, list, countDownLatch);
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
            logger.error("Load cost tape eo mapping file Data Completed But Has Error, Will Roll back, Please Check Log");

            rollbackCostTapeEoData();
            return CodeConfig.OPERATION_FAILED;
        } else {
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    @Override
    public String insertGsc() throws FileNotFoundException {
        logger.info("************ Start load cost tape gsc mapping data ************");
        final String FILE_PATH = "C:\\ftp\\data\\price_mapping\\gsc\\";
        Path path = Paths.get(FILE_PATH);
        if(!Files.exists(path) || !Files.isDirectory(path)) {
            logger.error("Cant not find cost tape gsc mapping data directory: {}", FILE_PATH);
            throw new FileNotFoundException("Cant not find cost tape gsc mapping data directory!");
        }

        List<String> resultList = new ArrayList<>();

        String fileName = "";
        try {
            Stream<Path> fileList = Files.walk(path, 7);
            List<Path> list = fileList.filter(p -> !Files.isDirectory(p)).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(list)) {
                rollbackCostTapeGscData();
            }

            for(Path p : list) {
                fileName = p.toFile().getName();
                logger.info("Start load cost tape gsc mapping data file: {}", fileName);

                String resultCode = loadCostTapeGscFile(p.toFile());
                resultList.add(resultCode);

                if(resultCode.equals(CodeConfig.OPERATION_SUCCESS)) {
                    //Files.delete(p);
                    logger.info("Load cost tape gsc mapping data file: {} success", fileName);
                } else {
                    logger.error("Load cost tape gsc mapping data file: {} error", fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Load cost tape gsc mapping File: {} Error", fileName);
            resultList.add(CodeConfig.OPERATION_FAILED);
        }

        if(resultList.stream().anyMatch(n -> n.equals(CodeConfig.OPERATION_FAILED))) {
            logger.info("************ End load cost tape gsc mapping data ************");
            return CodeConfig.OPERATION_FAILED;
        } else {
            logger.info("************ End load cost tape gsc mapping data ************");
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    @Override
    public String insertMbgFreight() throws FileNotFoundException {
        logger.info("************ Start load mbg freight cost data ************");
        final String FILE_PATH = "C:\\ftp\\data\\mbg_freight\\";
        Path path = Paths.get(FILE_PATH);
        if(!Files.exists(path) || !Files.isDirectory(path)) {
            logger.error("Cant not find mbg freight cost data directory: {}", FILE_PATH);
            throw new FileNotFoundException("Cant not find mbg freight cost data directory!");
        }

        List<String> resultList = new ArrayList<>();

        String fileName = "";
        try {
            Stream<Path> fileList = Files.walk(path, 7);
            List<Path> list = fileList.filter(p -> !Files.isDirectory(p)).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(list)) {
                rollbackCostTapeGscData();
            }

            for(Path p : list) {
                fileName = p.toFile().getName();
                logger.info("Start load mbg freight cost data file: {}", fileName);

                String resultCode = loadMbgFreightCostFile(p.toFile());
                resultList.add(resultCode);

                if(resultCode.equals(CodeConfig.OPERATION_SUCCESS)) {
                    //Files.delete(p);
                    logger.info("Load mbg freight cost data file: {} success", fileName);
                } else {
                    logger.error("Load mbg freight cost data file: {} error", fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Load mbg freight cost data File: {} Error", fileName);
            resultList.add(CodeConfig.OPERATION_FAILED);
        }

        if(resultList.stream().anyMatch(n -> n.equals(CodeConfig.OPERATION_FAILED))) {
            logger.info("************ End load mbg freight cost data ************");
            return CodeConfig.OPERATION_FAILED;
        } else {
            logger.info("************ End load mbg freight cost data ************");
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    private String loadMbgFreightCostFile(File file) {
        String resultCode = "";

        try (OPCPackage opcPackage = OPCPackage.open(file)) {

            XSSFReader reader = new XSSFReader(opcPackage);
            SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();
            StylesTable stylesTable = reader.getStylesTable();
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            MbgFreightSheetHandler handler = new MbgFreightSheetHandler();
            XSSFSheetXMLHandler xmlHandler = new XSSFSheetXMLHandler(stylesTable, sharedStringsTable, handler, new DataFormatter(), false);
            xmlReader.setContentHandler(xmlHandler);
            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) reader.getSheetsData();

            while (sheetIterator.hasNext()) {
                InputStream in = sheetIterator.next();
                InputSource source = new InputSource(in);
                xmlReader.parse(source);

                List<MbgFreightCost> dataList = handler.getDataList();
                resultCode = insertMbgFreightCostDb(dataList);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
            logger.error("Read mbg freight cost data file {} error", file.getName());
            logger.error(e.getMessage());
        } catch (OpenXML4JException | SAXException e) {
            logger.error("Read mbg freight cost data file {} error!", file.getName());
            logger.error(e.getMessage());
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
        }

        return resultCode;
    }

    private String insertMbgFreightCostDb(List<MbgFreightCost> dataList) {
        List<List<MbgFreightCost>> mbgFreightDataList = Lists.partition(dataList, CodeConfig.LIST_NUMBER);

        List<Future<FutureBean>> futureList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(mbgFreightDataList.size());

        boolean allDone = true;

        try {
            for (List<MbgFreightCost> list : mbgFreightDataList) {
                Future<FutureBean> future = asyncThreadProcess.processMbgFreightCostThread(mbgFreightCostMapperExt, list, countDownLatch);
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
            logger.error("Load mbg freight cost file Data Completed But Has Error, Will Roll back, Please Check Log");

            rollbackMbgFreightCostData();
            return CodeConfig.OPERATION_FAILED;
        } else {
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    private String loadCostTapeGscFile(File file) {
        String resultCode = "";

        try (OPCPackage opcPackage = OPCPackage.open(file)) {

            XSSFReader reader = new XSSFReader(opcPackage);
            SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();
            StylesTable stylesTable = reader.getStylesTable();
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            CostTapeGscSheetHandler handler = new CostTapeGscSheetHandler();
            XSSFSheetXMLHandler xmlHandler = new XSSFSheetXMLHandler(stylesTable, sharedStringsTable, handler, new DataFormatter(), false);
            xmlReader.setContentHandler(xmlHandler);
            XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator) reader.getSheetsData();

            while (sheetIterator.hasNext()) {
                InputStream in = sheetIterator.next();
                InputSource source = new InputSource(in);
                xmlReader.parse(source);

                List<CostTapeGsc> dataList = handler.getDataList();
                resultCode = insertCostTapeGscMappingDb(dataList);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
            logger.error("Read cost tape gsc mapping data file {} error", file.getName());
            logger.error(e.getMessage());
        } catch (OpenXML4JException | SAXException e) {
            logger.error("Read cost tape gsc mapping data file {} error!", file.getName());
            logger.error(e.getMessage());
            e.printStackTrace();
            resultCode = CodeConfig.OPERATION_FAILED;
        }

        return resultCode;
    }

    private String insertCostTapeGscMappingDb(List<CostTapeGsc> dataList) {
        List<List<CostTapeGsc>> warrantyDataList = Lists.partition(dataList, CodeConfig.LIST_NUMBER);

        List<Future<FutureBean>> futureList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(warrantyDataList.size());

        boolean allDone = true;

        try {
            for (List<CostTapeGsc> list : warrantyDataList) {
                Future<FutureBean> future = asyncThreadProcess.processCostTapeGscThread(costTapeGscMapperExt, list, countDownLatch);
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
            logger.error("Load cost tape gsc mapping file Data Completed But Has Error, Will Roll back, Please Check Log");

            rollbackCostTapeEoData();
            return CodeConfig.OPERATION_FAILED;
        } else {
            return CodeConfig.OPERATION_SUCCESS;
        }
    }

    private void rollbackWarrantyData() {
        warrantyMapperExt.deleteAll();
    }

    private void rollbackCostTapeEoData() {
        costTapeEoMapperExt.deleteAll();
    }

    private void rollbackCostTapeGscData() {
        costTapeGscMapperExt.deleteAll();
    }

    private void rollbackMbgFreightCostData() {
        mbgFreightCostMapperExt.deleteAll();
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
                dataList.add(warranty);
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if(warranty != null) {
                String prefix = cellReference.replaceAll("\\d+", "");

                switch (prefix) {
                    case "A":
                        warranty.setType(formattedValue);
                        break;
                    case "B":
                        warranty.setPartNumber(formattedValue);
                        break;
                    case "C":
                        warranty.setBrand(formattedValue);
                        break;
                    case "F":
                        warranty.setSubGeo(formattedValue);
                        break;
                    case "G":
                        warranty.setCountry(formattedValue);
                        break;
                    case "H":
                        warranty.setWarrantyCode(formattedValue);
                        break;
                    case "J":
                        warranty.setNbmc(StringUtils.isEmpty(formattedValue)?null:new BigDecimal(formattedValue));
                        break;
                }
            }
        }
    }

    public class CostTapeEoSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler{

        private CostTapeEo costTapeEo;
        private List<CostTapeEo> dataList;

        Date date = getInsertDate();

        private List<CostTapeEo> getDataList() {
            return dataList;
        }

        @Override
        public void startRow(int rowNum) {
            if(rowNum > 0) {
                costTapeEo = new CostTapeEo();
                costTapeEo.setInsertTime(date);
            }
        }

        @Override
        public void endRow(int rowNum) {
            if(dataList == null) {
                dataList = new ArrayList<>();
            }

            if(costTapeEo != null) {
                dataList.add(costTapeEo);
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if(costTapeEo != null) {
                String prefix = cellReference.replaceAll("\\d+", "");

                switch (prefix) {
                    case "A":
                        costTapeEo.setBrand(formattedValue);
                        break;
                    case "B":
                        costTapeEo.setSubgeo(formattedValue);
                        break;
                    case "D":
                        costTapeEo.setNbmc(StringUtils.isEmpty(formattedValue)?null:new BigDecimal(formattedValue));
                        break;
                }
            }
        }
    }

    public class CostTapeGscSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler{

        private CostTapeGsc costTapeGsc;
        private List<CostTapeGsc> dataList;

        Date date = getInsertDate();

        private List<CostTapeGsc> getDataList() {
            return dataList;
        }

        @Override
        public void startRow(int rowNum) {
            if(rowNum > 0) {
                costTapeGsc = new CostTapeGsc();
                costTapeGsc.setInsertTime(date);
            }
        }

        @Override
        public void endRow(int rowNum) {
            if(dataList == null) {
                dataList = new ArrayList<>();
            }

            if(costTapeGsc != null && !costTapeGsc.getCountry().equals("US") && !costTapeGsc.getCountry().equals("AT") &&
                    !costTapeGsc.getCountry().equals("DE") && !costTapeGsc.getCountry().equals("GR")) {
                dataList.add(costTapeGsc);
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if(costTapeGsc != null) {
                String prefix = cellReference.replaceAll("\\d+", "");

                switch (prefix) {
                    case "B":
                        costTapeGsc.setBrand(formattedValue);
                        break;
                    case "C":
                        costTapeGsc.setFamily(formattedValue);
                        break;
                    case "D":
                        costTapeGsc.setCountry(formattedValue);
                        break;
                    case "F":
                        costTapeGsc.setType(formattedValue);
                        break;
                    case "G":
                        costTapeGsc.setNbmc(StringUtils.isEmpty(formattedValue)?null:new BigDecimal(formattedValue));
                        break;
                }
            }
        }
    }

    public class MbgFreightSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler{

        private MbgFreightCost mbgFreightCost;
        private List<MbgFreightCost> dataList;

        Date date = getInsertDate();

        private List<MbgFreightCost> getDataList() {
            return dataList;
        }

        @Override
        public void startRow(int rowNum) {
            if(rowNum > 0) {
                mbgFreightCost = new MbgFreightCost();
                mbgFreightCost.setInsertTime(date);
            }
        }

        @Override
        public void endRow(int rowNum) {
            if(dataList == null) {
                dataList = new ArrayList<>();
            }

            if(mbgFreightCost != null && !StringUtils.isEmpty(mbgFreightCost.getMot())) {
                dataList.add(mbgFreightCost);
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if(mbgFreightCost != null) {
                String prefix = cellReference.replaceAll("\\d+", "");

                switch (prefix) {
                    case "A":
                        mbgFreightCost.setProductFamily(formattedValue);
                        break;
                    case "B":
                        mbgFreightCost.setProductNumber(formattedValue);
                        break;
                    case "D":
                        mbgFreightCost.setCountry(formattedValue);
                        break;
                    case "E":
                        if(formattedValue.equalsIgnoreCase("air")) {
                            mbgFreightCost.setMot("1");
                        } else if(formattedValue.equalsIgnoreCase("ocean")) {
                            mbgFreightCost.setMot("0");
                        } else if(formattedValue.equalsIgnoreCase("TRUCK")){
                            mbgFreightCost.setMot("2");
                        } else {
                            mbgFreightCost.setMot("");
                        }
                        break;
                    case "F":
                        mbgFreightCost.setFee(StringUtils.isEmpty(formattedValue)?null:new BigDecimal(formattedValue));
                        break;
                }
            }
        }
    }
}
