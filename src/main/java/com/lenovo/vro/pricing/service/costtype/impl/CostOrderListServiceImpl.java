package com.lenovo.vro.pricing.service.costtype.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.*;
import com.lenovo.vro.pricing.entity.ext.CostTapeListExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderForm;
import com.lenovo.vro.pricing.mapper.ext.CostTapeDetailMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeListMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeOrderMapperExt;
import com.lenovo.vro.pricing.mapper.ext.WarrantyMapperExt;
import com.lenovo.vro.pricing.service.costtype.CostOrderListService;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CostOrderListServiceImpl extends CostTapeBaseService implements CostOrderListService {

    @Autowired
    private CostTapeOrderMapperExt costTapeOrderMapperExt;

    @Autowired
    private CostTapeDetailMapperExt costTapeDetailMapperExt;

    @Autowired
    private CostTapeListMapperExt costTapeListMapperExt;

    @Autowired
    private WarrantyMapperExt warrantyMapperExt;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public PageInfo<CostTapeOrder> selectCostTapeOrderList(CostTapeOrderForm form) {
        PageHelper.startPage(form.getPageNum(), form.getPageSize());
        List<CostTapeOrder> result = costTapeOrderMapperExt.selectCostTapeList(form);
        return new PageInfo<>(result);
    }

    @Override
    public CostTapeOrderExt selectCostTapeOrderDetail(Integer id, String type) {
        CostTapeOrderExt order = costTapeOrderMapperExt.selectById(id);

        List<CostTapeDetail> detailList = costTapeDetailMapperExt.selectCostTapeOrderDetail(id);
        List<CostTapeListExt> costTapeListList = costTapeListMapperExt.selectCostTapeList(id);
        if(!CollectionUtils.isEmpty(costTapeListList) && order!= null) {
            String country = order.getCountry();
            for (CostTapeListExt costTapeList : costTapeListList) {
                if(costTapeList.getPid().contains("*") && (!costTapeList.getPartNumber().startsWith("ZA") || !costTapeList.getPartNumber().startsWith("ZG"))) {
                    String partNumber = costTapeList.getPartNumber();
                    if (!StringUtils.isEmpty(partNumber)) {
                        HashMap<String, List<Warranty>> warrantyMap = getWarrantyDataList(partNumber, country,
                                costTapeList.getBrand(), redisTemplate, warrantyMapperExt);
                        if (!MapUtils.isEmpty(warrantyMap)) {
                            /*costTapeList.setWarrantyList(warrantyMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));*/
                            costTapeList.setWarrantyMap(warrantyMap);
                        }
                    }
                }

                if(country.equalsIgnoreCase("jp") && type.equalsIgnoreCase("0")) {
                    String rebate = costTapeList.getRebate();
                    if(!StringUtils.isEmpty(rebate) && rebate.contains("|")) {
                        costTapeList.setRebate(rebate.split("\\|")[0]);
                    }
                }
            }
        }

        if(order != null) {
            order.setCostTapeDetailList(detailList);
            order.setCostTapeListList(costTapeListList);
            setOrderModify(order);
        }

        return order;
    }

    @Override
    public String deleteCostTapeOrder(Integer id) {
        costTapeOrderMapperExt.deleteCostTapeOrder(id);
        costTapeDetailMapperExt.deleteCostTapeDetail(id);
        costTapeListMapperExt.deleteCostTapeList(id);

        return CodeConfig.OPERATION_SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrder(CostTapeOrderExt form) {

        CostTapeOrder insertDto = new CostTapeOrder();
        BeanUtils.copyProperties(form, insertDto);

        costTapeOrderMapperExt.insertSelective(insertDto);
        int id = insertDto.getId();

        if(id != 0) {
            List<CostTapeListExt> costTapeListList = form.getCostTapeListList();
            List<CostTapeDetail> costTapeDetailList = form.getCostTapeDetailList();

            try{
                if(!CollectionUtils.isEmpty(costTapeDetailList)) {
                    costTapeDetailList.forEach(n -> n.setCostId(id));
                    costTapeDetailMapperExt.insertBatch(costTapeDetailList);
                }

                if(!CollectionUtils.isEmpty(costTapeListList)) {
                    costTapeListList.forEach(n -> n.setCostId(id));
                    costTapeListMapperExt.insertBatch(costTapeListList);
                }

                return CodeConfig.OPERATION_SUCCESS;
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw e;
            }
        }

        logger.error("Insert id is 0");
        return CodeConfig.OPERATION_FAILED;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateOrder(CostTapeOrderExt form) throws Exception {
        if(form.getId() == null) {
            throw new Exception("Order id is null");
        }

        CostTapeOrder updateDto = new CostTapeOrder();
        BeanUtils.copyProperties(form, updateDto);
        int id = form.getId();

        try {
            costTapeOrderMapperExt.updateByPrimaryKeySelective(updateDto);

            List<CostTapeListExt> costTapeListList = form.getCostTapeListList();
            List<CostTapeDetail> costTapeDetailList = form.getCostTapeDetailList();

            if(!CollectionUtils.isEmpty(costTapeListList)) {
                costTapeListMapperExt.deleteCostTapeList(id);
                costTapeListList.forEach(n -> n.setCostId(id));
                costTapeListMapperExt.insertBatch(costTapeListList);
            }

            if(!CollectionUtils.isEmpty(costTapeDetailList)) {
                costTapeDetailMapperExt.deleteCostTapeDetail(id);
                costTapeDetailList.forEach(n -> n.setCostId(id));
                costTapeDetailMapperExt.insertBatch(costTapeDetailList);
            }

            return CodeConfig.OPERATION_SUCCESS;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }

    @Override
    public void exportData(CostTapeOrderExt form, HttpServletResponse response) {
        if(form != null) {
            exportDataProcess(form, response);
        }
    }

    @Override
    public void exportData(int id, HttpServletResponse response) {
        CostTapeOrderExt costTapeOrderExt = selectCostTapeOrderDetail(id, "1");
        exportDataProcess(costTapeOrderExt, response);
    }

    private void exportDataProcess(CostTapeOrderExt data, HttpServletResponse response) {
        CostTapeOrderExt normalData = new CostTapeOrderExt();
        BeanUtils.copyProperties(data, normalData);

        normalData.setCostTapeListList(data.getCostTapeListList().stream().filter(n -> n.getRecoveryType().equals("0")).collect(Collectors.toList()));
        normalData.setCostTapeDetailList(data.getCostTapeDetailList().stream().filter(n -> n.getRecoveryType().equals("0")).collect(Collectors.toList()));

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("normal");

        createReport(normalData, wb, sheet);

        normalData.setCostTapeListList(data.getCostTapeListList().stream().filter(n -> n.getRecoveryType().equals("1")).collect(Collectors.toList()));
        normalData.setCostTapeDetailList(data.getCostTapeDetailList().stream().filter(n -> n.getRecoveryType().equals("1")).collect(Collectors.toList()));

        sheet = wb.createSheet("recovery");

        createReport(normalData, wb, sheet);

        sheet = wb.createSheet("total");
        //total detail data
        Map<String, int[]> indexMap = setDetailTitle(0, wb, sheet);
        List<CostTapeDetail> detailList = data.getCostTapeDetailList().stream().filter(n -> n.getRecoveryType().equals("2")).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(detailList)) {
            setDetailData(indexMap, sheet, detailList, data.getExchangeRates());
        }

        try {
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + data.getOrderName() + ".xlsx");
            OutputStream o = response.getOutputStream();
            wb.write(o);
            o.flush();
            o.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createReport(CostTapeOrderExt data, XSSFWorkbook wb, XSSFSheet sheet) {
        Integer index = 0;
        // base info
        index = setInfoData(index, wb, sheet, data);
        index++;
        // list title
        XSSFRow row = sheet.createRow(index);
        setListTitle(wb, row, data);
        index++;


        // list data
        List<CostTapeListExt> costTapeListList = data.getCostTapeListList();
        if(!CollectionUtils.isEmpty(costTapeListList)) {
            List<List<CostTapeList>> dataList = new ArrayList<>();
            for(CostTapeList temp : costTapeListList) {
                List<CostTapeList> list = new ArrayList<>();
                if(temp.getPid().contains("*")) {
                    list.add(temp);
                    list.addAll(costTapeListList.stream().filter(n -> n.getPid().substring(1).equals(temp.getPid())).collect(Collectors.toList()));
                }

                dataList.add(list);
            }

            index = setCostTapeListData(wb, data.getCountry(), sheet, dataList, index);
        }

        index++;

        // detail data
        Map<String, int[]> indexMap = setDetailTitle(index, wb, sheet);
        List<CostTapeDetail> detailList = data.getCostTapeDetailList();
        if(!CollectionUtils.isEmpty(detailList)) {
            setDetailData(indexMap, sheet, detailList, data.getExchangeRates());
        }
    }

    private Integer setInfoData(Integer index, XSSFWorkbook wb, XSSFSheet sheet, CostTapeOrderExt data) {
        IndexedColorMap colorMap = wb.getStylesSource().getIndexedColors();

        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        XSSFColor xssfColor = new XSSFColor(new java.awt.Color(255, 255, 255), colorMap);
        font.setColor(xssfColor);
        style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFColor otherColor = new XSSFColor(new java.awt.Color(47,117,181), colorMap);
        style.setFillForegroundColor(otherColor);
        style.setFont(font);

        XSSFCellStyle dataStyle = wb.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.LEFT);

        XSSFRow row = sheet.createRow(index);

        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("Exchange Rates: ");

        cell = row.createCell(1);
        cell.setCellValue(data.getExchangeRates().doubleValue());
        cell.setCellStyle(dataStyle);

        if(!data.getCountry().equalsIgnoreCase("jp")) {
            ++index;
            row = sheet.createRow(index);
            cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue("Rebates: ");

            cell = row.createCell(1);
            if(data.getRebateValue() != null) {
                cell.setCellValue(data.getRebateValue().multiply(BigDecimal.valueOf(100)).doubleValue() + "%");
                cell.setCellStyle(dataStyle);
            }
        }

        ++index;
        row = sheet.createRow(index);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("Country: ");

        cell = row.createCell(1);
        cell.setCellValue(data.getCountry());
        cell.setCellStyle(dataStyle);

        ++index;
        row = sheet.createRow(index);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("MOT: ");

        cell = row.createCell(1);
        cell.setCellValue(getFulfilmentValue(data.getFulfilment()));
        cell.setCellStyle(dataStyle);

        ++index;
        row = sheet.createRow(index);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("Currency: ");

        cell = row.createCell(1);
        cell.setCellValue(data.getLocalCurrency());
        cell.setCellStyle(dataStyle);

        ++index;
        return index;
    }

    private void setListTitle(XSSFWorkbook wb, XSSFRow row, CostTapeOrderExt data) {
        XSSFFont font = wb.createFont();
        font.setBold(true);

        XSSFCellStyle styleGreen = wb.createCellStyle();
        styleGreen.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        styleGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleGreen.setFont(font);
        styleGreen.setBorderBottom(BorderStyle.THIN);
        styleGreen.setBorderLeft(BorderStyle.THIN);
        styleGreen.setBorderRight(BorderStyle.THIN);
        styleGreen.setBorderTop(BorderStyle.THIN);

        XSSFCellStyle styleOther = wb.createCellStyle();
        IndexedColorMap colorMap = wb.getStylesSource().getIndexedColors();
        XSSFColor otherColor = new XSSFColor(new java.awt.Color(255,242,204), colorMap);
        styleOther.setFillForegroundColor(otherColor);
        styleOther.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleOther.setFont(font);
        styleOther.setBorderBottom(BorderStyle.THIN);
        styleOther.setBorderLeft(BorderStyle.THIN);
        styleOther.setBorderRight(BorderStyle.THIN);
        styleOther.setBorderTop(BorderStyle.THIN);

        int cellIndex = 0;

        XSSFCell cell = row.createCell(cellIndex);
        cell.setCellValue("Part Number");
        cell.setCellStyle(styleGreen);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("Description");
        cell.setCellStyle(styleGreen);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("Price");
        cell.setCellStyle(styleGreen);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("QTY");
        cell.setCellStyle(styleGreen);
        cellIndex++;

        if(data.getCountry().equalsIgnoreCase("jp")) {
            cell = row.createCell(cellIndex);
            cell.setCellValue("Rebate");
            cell.setCellStyle(styleOther);
            cellIndex++;
        }

        cell = row.createCell(cellIndex);
        cell.setCellValue("BMC Cost");
        cell.setCellStyle(styleOther);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("Non BMC Cost");
        cell.setCellStyle(styleOther);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("TMC Cost");
        cell.setCellStyle(styleOther);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("Air cost");
        cell.setCellStyle(styleOther);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("Fundings/other cost adj");
        cell.setCellStyle(styleOther);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("Adj Cost");
        cell.setCellStyle(styleOther);
        cellIndex++;

        cell = row.createCell(cellIndex);
        cell.setCellValue("TMC %");
        cell.setCellStyle(styleGreen);

    }

    private Integer setCostTapeListData(XSSFWorkbook wb, String country, XSSFSheet sheet, List<List<CostTapeList>> dataList, Integer index) {
        // 带$ 的格式
        XSSFCellStyle priceStyle = wb.createCellStyle();
        priceStyle.setDataFormat(wb.createDataFormat().getFormat("_(\"$\"* #,##0.00_);_(\"$\"* (#,##0.00);_(\"$\"* \"-\"??_);_(@_)"));
        priceStyle.setBorderBottom(BorderStyle.THIN);
        priceStyle.setBorderLeft(BorderStyle.THIN);
        priceStyle.setBorderRight(BorderStyle.THIN);
        priceStyle.setBorderTop(BorderStyle.THIN);
        IndexedColorMap colorMap = wb.getStylesSource().getIndexedColors();
        XSSFColor purpleColor = new XSSFColor(new java.awt.Color(217,225,242), colorMap);
        priceStyle.setFillForegroundColor(purpleColor);
        priceStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 紫色背景
        XSSFCellStyle purpleStyle = wb.createCellStyle();
        purpleStyle.setBorderBottom(BorderStyle.THIN);
        purpleStyle.setBorderLeft(BorderStyle.THIN);
        purpleStyle.setBorderRight(BorderStyle.THIN);
        purpleStyle.setBorderTop(BorderStyle.THIN);
        purpleStyle.setFillForegroundColor(purpleColor);
        purpleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 紫色背景 靠右
        XSSFCellStyle purpleRightStyle = wb.createCellStyle();
        purpleRightStyle.setBorderBottom(BorderStyle.THIN);
        purpleRightStyle.setBorderLeft(BorderStyle.THIN);
        purpleRightStyle.setBorderRight(BorderStyle.THIN);
        purpleRightStyle.setBorderTop(BorderStyle.THIN);
        purpleRightStyle.setFillForegroundColor(purpleColor);
        purpleRightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        purpleRightStyle.setAlignment(HorizontalAlignment.RIGHT);

        // 红色背景
        XSSFCellStyle redStyle = wb.createCellStyle();
        redStyle.setBorderBottom(BorderStyle.THIN);
        redStyle.setBorderLeft(BorderStyle.THIN);
        redStyle.setBorderRight(BorderStyle.THIN);
        redStyle.setBorderTop(BorderStyle.THIN);
        redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // CTO 格式
        XSSFCellStyle ctoStyle = wb.createCellStyle();
        ctoStyle.setBorderBottom(BorderStyle.THIN);
        ctoStyle.setBorderLeft(BorderStyle.THIN);
        ctoStyle.setBorderRight(BorderStyle.THIN);
        ctoStyle.setBorderTop(BorderStyle.THIN);
        XSSFColor orangeColor = new XSSFColor(new java.awt.Color(255,192,0), colorMap);
        ctoStyle.setFillForegroundColor(orangeColor);
        ctoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // CTO 格式 带$格式
        XSSFCellStyle ctoDollarStyle = wb.createCellStyle();
        ctoDollarStyle.setBorderBottom(BorderStyle.THIN);
        ctoDollarStyle.setBorderLeft(BorderStyle.THIN);
        ctoDollarStyle.setBorderRight(BorderStyle.THIN);
        ctoDollarStyle.setBorderTop(BorderStyle.THIN);
        ctoDollarStyle.setFillForegroundColor(orangeColor);
        ctoDollarStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        ctoDollarStyle.setAlignment(HorizontalAlignment.RIGHT);

        // default 格式
        XSSFCellStyle defaultStyle = wb.createCellStyle();
        defaultStyle.setBorderBottom(BorderStyle.THIN);
        defaultStyle.setBorderLeft(BorderStyle.THIN);
        defaultStyle.setBorderRight(BorderStyle.THIN);
        defaultStyle.setBorderTop(BorderStyle.THIN);
        XSSFColor whiteColor = new XSSFColor(new java.awt.Color(255, 255, 255), colorMap);
        defaultStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        defaultStyle.setFillForegroundColor(whiteColor);

        XSSFFont font = wb.createFont();
        XSSFColor redColor = new XSSFColor(new java.awt.Color(255, 0, 0), colorMap);
        font.setColor(redColor);

        XSSFCellStyle purpleRedStyle = wb.createCellStyle();
        purpleRedStyle.setBorderBottom(BorderStyle.THIN);
        purpleRedStyle.setBorderLeft(BorderStyle.THIN);
        purpleRedStyle.setBorderRight(BorderStyle.THIN);
        purpleRedStyle.setBorderTop(BorderStyle.THIN);
        purpleRedStyle.setFillForegroundColor(purpleColor);
        purpleRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        purpleRedStyle.setAlignment(HorizontalAlignment.RIGHT);
        purpleRedStyle.setFont(font);

        XSSFRow row;
        XSSFCell cell;

        for(int i=0;i<dataList.size();i++) {
            List<CostTapeList> list = dataList.get(i);
            for(int j=0;j<list.size();j++) {
                CostTapeList data = list.get(j);

                int cellIndex = 0;

                // Part Number
                row = sheet.createRow(index + j);
                cell = row.createCell(cellIndex);
                cell.setCellValue(data.getPartNumber());
                cell.setCellStyle(purpleStyle);
                cellIndex++;

                // Description
                cell = row.createCell(cellIndex);
                cell.setCellValue(data.getDescription());
                cell.setCellStyle(defaultStyle);
                cellIndex++;

                if(list.size() > 1) {
                    // price
                    cell = row.createCell(cellIndex);
                    cell.setCellValue("");
                    cell.setCellStyle(purpleStyle);
                    cellIndex++;

                    // qty
                    String type = data.getType();
                    if(StringUtils.isEmpty(type)) {
                        cell = row.createCell(cellIndex);
                        cell.setCellValue("");
                        cell.setCellStyle(purpleStyle);
                    } else {
                        if(type.equals(CodeConfig.TYPE_ADD)) {
                            cell = row.createCell(cellIndex);
                            cell.setCellValue("ADD");
                            cell.setCellStyle(purpleRightStyle);
                        } else {
                            cell = row.createCell(cellIndex);
                            cell.setCellValue("DEL");
                            cell.setCellStyle(purpleRedStyle);
                        }
                    }
                    cellIndex++;

                    if(!StringUtils.isEmpty(country) && country.equalsIgnoreCase("jp")) {
                        cell = row.createCell(cellIndex);

                        String rebate = data.getRebate();
                        if(!StringUtils.isEmpty(rebate) && rebate.contains("|")) {
                            String value = rebate.split("\\|")[1];
                            if(!StringUtils.isEmpty(value)) {
                                cell.setCellValue(new BigDecimal(value).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "%");
                                cell.setCellStyle(defaultStyle);
                            }
                        }
                        cellIndex++;
                    }

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getBmc()!=null?data.getBmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getNbmc()!=null?data.getNbmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getTmc()!=null?data.getTmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getAirCost()!=null?data.getAirCost().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getFundings()!=null?data.getFundings().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getAdjCost()!=null?data.getAdjCost().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getTmcPercent()!=null?data.getTmcPercent().multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+"%":BigDecimal.ZERO.doubleValue()+"%");
                    if(data.getTmcPercent()!=null && data.getTmcPercent().doubleValue() < 0) {
                        cell.setCellStyle(redStyle);
                    } else {
                        cell.setCellStyle(defaultStyle);
                    }

                    if(j == list.size() - 1) {
                        cellIndex = 0;
                        data = list.get(0);
                        row = sheet.createRow(index + j + 1);

                        cell = row.createCell(cellIndex);
                        cell.setCellValue("CTO");
                        cell.setCellStyle(ctoStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue("");
                        cell.setCellStyle(ctoStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getPricing() != null?data.getPricing().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(): BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(ctoDollarStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getQty() != null? data.getQty() :0);
                        cell.setCellStyle(ctoStyle);
                        cellIndex++;

                        if(!StringUtils.isEmpty(country) && country.equalsIgnoreCase("jp")) {
                            cell = row.createCell(cellIndex);
                            cell.setCellValue("");
                            cell.setCellStyle(defaultStyle);
                            cellIndex++;
                        }

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getBmc()!=null?data.getBmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getNbmc()!=null?data.getNbmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getTmc()!=null?data.getTmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getAirCost()!=null?data.getAirCost().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getFundings()!=null?data.getFundings().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getAdjCost()!=null?data.getAdjCost().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);
                        cellIndex++;

                        cell = row.createCell(cellIndex);
                        cell.setCellValue(data.getTmcPercent()!=null?data.getTmcPercent().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+"%":BigDecimal.ZERO.doubleValue()+"%");
                        if(data.getTmcPercent() != null && data.getTmcPercent().doubleValue() < 0) {
                            cell.setCellStyle(redStyle);
                        } else {
                            cell.setCellStyle(defaultStyle);
                        }

                        index ++;
                    }
                } else {
                    // price
                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getPricing() != null?data.getPricing().doubleValue(): BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(purpleStyle);
                    cellIndex++;

                    // QTY
                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getQty() != null? data.getQty() :0);
                    cell.setCellStyle(purpleStyle);
                    cellIndex++;

                    if(!StringUtils.isEmpty(country) && country.equalsIgnoreCase("jp")) {
                        cell = row.createCell(cellIndex);

                        String rebate = data.getRebate();
                        if(!StringUtils.isEmpty(rebate) && rebate.contains("|")) {
                            String value = rebate.split("\\|")[1];
                            if(!StringUtils.isEmpty(value)) {
                                cell.setCellValue(new BigDecimal(value).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "%");
                                cell.setCellStyle(defaultStyle);
                            }
                        }
                        cellIndex++;
                    }

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getBmc()!=null?data.getBmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getNbmc()!=null?data.getNbmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getTmc()!=null?data.getTmc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getAirCost()!=null?data.getAirCost().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getFundings()!=null?data.getFundings().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getAdjCost()!=null?data.getAdjCost().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);
                    cellIndex++;

                    cell = row.createCell(cellIndex);
                    cell.setCellValue(data.getTmcPercent()!=null?data.getTmcPercent().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+"%":BigDecimal.ZERO.doubleValue()+"%");
                    if(data.getTmcPercent()!=null && data.getTmcPercent().doubleValue() < 0) {
                        cell.setCellStyle(redStyle);
                    } else {
                        cell.setCellStyle(defaultStyle);
                    }
                }

            }

            index += list.size();
        }

        return index;
    }

    private Map<String, int[]> setDetailTitle(Integer index, XSSFWorkbook wb, XSSFSheet sheet) {
        XSSFCellStyle bigTitleStyle = wb.createCellStyle();
        IndexedColorMap colorMap = wb.getStylesSource().getIndexedColors();
        XSSFColor bigTitleColor = new XSSFColor(new java.awt.Color(255,230,153), colorMap);
        bigTitleStyle.setFillForegroundColor(bigTitleColor);
        bigTitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        bigTitleStyle.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle titleStyle = wb.createCellStyle();
        XSSFColor titleColor = new XSSFColor(new java.awt.Color(255,242,204), colorMap);
        titleStyle.setFillForegroundColor(titleColor);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFRow row = sheet.createRow(index);
        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(bigTitleStyle);
        cell.setCellValue("Summary(Dollar currency)");

        CellRangeAddress region = new CellRangeAddress(index, index, 0, 9);
        sheet.addMergedRegion(region);

        ++index;
        setDetailTitleProcess(index, sheet, titleStyle);
        ++index;

        int[] row1 = new int[]{index, index+1};

        index = index + 3;
        row = sheet.createRow(index);
        cell = row.createCell(0);
        cell.setCellStyle(bigTitleStyle);
        cell.setCellValue("Summary(Local currency)");

        region = new CellRangeAddress(index, index, 0, 9);
        sheet.addMergedRegion(region);

        ++index;
        setDetailTitleProcess(index, sheet, titleStyle);
        ++index;

        int[] row2 = new int[]{index, index+1};

        Map<String, int[]> resultMap = new HashMap<>();
        resultMap.put("1", row1);
        resultMap.put("2", row2);

        return resultMap;
    }

    private void setDetailTitleProcess(Integer index, XSSFSheet sheet, XSSFCellStyle titleStyle) {
        XSSFCell cell;
        XSSFRow row = sheet.createRow(index);
        cell = row.createCell(0);
        cell.setCellValue("Category");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(1);
        cell.setCellValue("Total Quantity");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(2);
        cell.setCellValue("Total Gross Rev");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(3);
        cell.setCellValue("Total Net Rev");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(4);
        cell.setCellValue("Total BMC GP$ (w/o cc)");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(5);
        cell.setCellValue("Total TMC GP$ (w/o cc)");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(6);
        cell.setCellValue("Total TMC GP% (w/o cc)");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(7);
        cell.setCellValue("Total TMC GP% (w/o cc)");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(8);
        cell.setCellValue("Total TMC GP$ (with cc)");
        cell.setCellStyle(titleStyle);

        cell = row.createCell(9);
        cell.setCellValue("Total TMC GP% (with cc)");
        cell.setCellStyle(titleStyle);
    }

    private void setDetailData(Map<String, int[]> indexMap, XSSFSheet sheet, List<CostTapeDetail> detailList, BigDecimal rate) {
        if(MapUtils.isNotEmpty(indexMap)) {
            int[] dollarRow = indexMap.get("1");
            int[] currencyRow = indexMap.get("2");

            for(int i=0;i<dollarRow.length;i++) {
                setDetailDataProcess(detailList.get(i), sheet, dollarRow[i], null);
            }

            for(int i=0;i<currencyRow.length;i++) {
                setDetailDataProcess(detailList.get(i), sheet, currencyRow[i], rate);
            }
        }
    }

    private void setDetailDataProcess(CostTapeDetail data, XSSFSheet sheet, int index, BigDecimal rate) {
        XSSFRow row = sheet.createRow(index);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue(data.getCategory());

        cell = row.createCell(1);
        cell.setCellValue(data.getTotalQuantity()!=null?data.getTotalQuantity():0);

        cell = row.createCell(2);
        if(data.getTotalGrossRev() == null) {
            cell.setCellValue(0);
        } else {
            cell.setCellValue(rate != null?data.getTotalGrossRev().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():data.getTotalGrossRev().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }

        cell = row.createCell(3);
        if(data.getTotalNetRev() == null) {
            cell.setCellValue(0);
        } else {
            cell.setCellValue(rate != null?data.getTotalNetRev().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():data.getTotalNetRev().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

        }

        cell = row.createCell(4);
        if(data.getTotalBmcGpUsdWocc() == null) {
            cell.setCellValue(0);
        } else {
            cell.setCellValue(rate != null?data.getTotalBmcGpUsdWocc().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():data.getTotalBmcGpUsdWocc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }

        cell = row.createCell(5);
        cell.setCellValue(data.getTotalBmcGpPercentWocc()!=null?data.getTotalBmcGpPercentWocc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+"%":"");

        cell = row.createCell(6);
        if(data.getTotalTmcGpUsdWocc() == null) {
            cell.setCellValue(0);
        } else {
            cell.setCellValue(rate != null?data.getTotalTmcGpUsdWocc().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():data.getTotalTmcGpUsdWocc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }

        cell = row.createCell(7);
        cell.setCellValue(data.getTotalTmcGpPercentWocc()!=null?data.getTotalTmcGpPercentWocc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+"%":"");


        cell = row.createCell(8);
        if(data.getTotalTmcGpUsdCc() == null) {
            cell.setCellValue(0);
        } else {
            cell.setCellValue(rate != null?data.getTotalTmcGpUsdCc().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue():data.getTotalTmcGpUsdCc().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }

        cell = row.createCell(9);
        cell.setCellValue(data.getTotalTmcGpPercentCc()!=null?data.getTotalTmcGpPercentCc().doubleValue()+"%":"");
    }

    private String getFulfilmentValue(String value) {
        switch (value){
            case "0":
                return "Ocean";
            case "1":
                return "Air";
            case "2":
                return "trunk";
            default:
                return "";
        }
    }

    private void setOrderModify(CostTapeOrderExt order) {
        if(order.getInsertTime() != null) {
            LocalDate localDate = LocalDate.now();
            Date monday = Date.from(localDate.with(DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault()).toInstant());
            if(order.getInsertTime().after(monday) || order.getInsertTime().equals(monday)) {
                order.setModify("1");
            } else {
                order.setModify("0");
            }
        } else {
            order.setModify("0");
        }
    }
}
