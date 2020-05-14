package com.lenovo.vro.pricing.service.costtype.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lenovo.vro.pricing.configuration.CodeConfig;
import com.lenovo.vro.pricing.entity.CostTapeDetail;
import com.lenovo.vro.pricing.entity.CostTapeList;
import com.lenovo.vro.pricing.entity.CostTapeOrder;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderExt;
import com.lenovo.vro.pricing.entity.ext.CostTapeOrderForm;
import com.lenovo.vro.pricing.mapper.ext.CostTapeDetailMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeListMapperExt;
import com.lenovo.vro.pricing.mapper.ext.CostTapeOrderMapperExt;
import com.lenovo.vro.pricing.service.costtype.CostOrderListService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CostOrderListServiceImpl implements CostOrderListService {

    @Autowired
    private CostTapeOrderMapperExt costTapeOrderMapperExt;

    @Autowired
    private CostTapeDetailMapperExt costTapeDetailMapperExt;

    @Autowired
    private CostTapeListMapperExt costTapeListMapperExt;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public PageInfo<CostTapeOrder> selectCostTapeOrderList(CostTapeOrderForm form) {
        PageHelper.offsetPage(form.getPageNum(), form.getPageSize());
        List<CostTapeOrder> result = costTapeOrderMapperExt.selectCostTapeList(form);
        return new PageInfo<>(result);
    }

    @Override
    public CostTapeOrderExt selectCostTapeOrderDetail(Integer id) {
        CostTapeOrderExt order = costTapeOrderMapperExt.selectByPrimaryKey(id);
        List<CostTapeDetail> detailList = costTapeDetailMapperExt.selectCostTapeOrderDetail(id);
        List<CostTapeList> costTapeListList = costTapeListMapperExt.selectCostTapeList(id);

        if(order != null) {
            order.setCostTapeDetailList(detailList);
            order.setCostTapeListList(costTapeListList);
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
            List<CostTapeList> costTapeListList = form.getCostTapeListList();
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
    public String updateOrder(CostTapeOrderExt form) {
        CostTapeOrder updateDto = new CostTapeOrder();
        BeanUtils.copyProperties(form, updateDto);
        int id = form.getId();

        try {
            costTapeOrderMapperExt.updateByPrimaryKeySelective(updateDto);

            List<CostTapeList> costTapeListList = form.getCostTapeListList();
            List<CostTapeDetail> costTapeDetailList = form.getCostTapeDetailList();

            if(!CollectionUtils.isEmpty(costTapeListList)) {
                costTapeListMapperExt.deleteCostTapeList(id);
                costTapeListMapperExt.insertBatch(costTapeListList);
            }

            if(!CollectionUtils.isEmpty(costTapeDetailList)) {
                costTapeDetailMapperExt.deleteCostTapeDetail(id);
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
        CostTapeOrderExt costTapeOrderExt = selectCostTapeOrderDetail(id);
        exportDataProcess(costTapeOrderExt, response);
    }

    private void exportDataProcess(CostTapeOrderExt data, HttpServletResponse response) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("sheet1");

        Integer index = 0;
        // base info
        index = setInfoData(index, wb, sheet, data);
        index++;
        // list title
        XSSFRow row = sheet.createRow(index);
        setListTitle(wb, row);
        index++;

        // list data
        List<CostTapeList> costTapeListList = data.getCostTapeListList();
        if(!CollectionUtils.isEmpty(costTapeListList)) {
            List<List<CostTapeList>> dataList = new ArrayList<>();
            for(CostTapeList temp : costTapeListList) {
                List<CostTapeList> list = new ArrayList<>();
                if(temp.getPid() == 0) {
                    list.add(temp);
                    list.addAll(costTapeListList.stream().filter(n -> n.getPid().intValue() == temp.getId().intValue()).collect(Collectors.toList()));
                }

                dataList.add(list);
            }

            index = setCostTapeListData(wb, sheet, dataList, index);
        }

        index++;

        // detail data
        setDetailTitle(index, wb, sheet);

        try {
            response.reset();
            response.setContentType("application/vnd.ms-excel"); // 改成输出excel文件

            response.addHeader("Content-Disposition", "attachment;fileName=" + data.getOrderName() + ".xlsx");
            OutputStream o = response.getOutputStream();
            wb.write(o);
            o.flush();
            o.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Integer setInfoData(Integer index, XSSFWorkbook wb, XSSFSheet sheet, CostTapeOrder data) {
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

        ++index;
        row = sheet.createRow(index);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("Rebates: ");

        cell = row.createCell(1);
        cell.setCellValue(data.getExchangeRates().multiply(BigDecimal.valueOf(100)).doubleValue() + "%");
        cell.setCellStyle(dataStyle);

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
        cell.setCellValue("Fulfilment: ");

        cell = row.createCell(1);
        cell.setCellValue(data.getFulfilment());
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

    private void setListTitle(XSSFWorkbook wb, XSSFRow row) {
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

        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Part Number");
        cell.setCellStyle(styleGreen);

        cell = row.createCell(1);
        cell.setCellValue("Description");
        cell.setCellStyle(styleGreen);

        cell = row.createCell(2);
        cell.setCellValue("Price");
        cell.setCellStyle(styleGreen);

        cell = row.createCell(3);
        cell.setCellValue("QTY");
        cell.setCellStyle(styleGreen);

        cell = row.createCell(4);
        cell.setCellValue("BMC Cost");
        cell.setCellStyle(styleOther);

        cell = row.createCell(5);
        cell.setCellValue("Non BMC Cost");
        cell.setCellStyle(styleOther);

        cell = row.createCell(6);
        cell.setCellValue("TMC Cost");
        cell.setCellStyle(styleOther);

        cell = row.createCell(7);
        cell.setCellValue("Air cost");
        cell.setCellStyle(styleOther);

        cell = row.createCell(8);
        cell.setCellValue("Fundings/other cost adj");
        cell.setCellStyle(styleOther);

        cell = row.createCell(9);
        cell.setCellValue("Adj Cost");
        cell.setCellStyle(styleOther);

        cell = row.createCell(10);
        cell.setCellValue("TMC %");
        cell.setCellStyle(styleGreen);

    }

    private Integer setCostTapeListData(XSSFWorkbook wb, XSSFSheet sheet, List<List<CostTapeList>> dataList, Integer index) {
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
        ctoDollarStyle.setDataFormat(wb.createDataFormat().getFormat("_(\"$\"* #,##0.00_);_(\"$\"* (#,##0.00);_(\"$\"* \"-\"??_);_(@_)"));
        ctoDollarStyle.setBorderBottom(BorderStyle.THIN);
        ctoDollarStyle.setBorderLeft(BorderStyle.THIN);
        ctoDollarStyle.setBorderRight(BorderStyle.THIN);
        ctoDollarStyle.setBorderTop(BorderStyle.THIN);
        ctoDollarStyle.setFillForegroundColor(orangeColor);
        ctoDollarStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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

                // Part Number
                row = sheet.createRow(index + j);
                cell = row.createCell(0);
                cell.setCellValue(data.getPartNumber());
                cell.setCellStyle(purpleStyle);

                // Description
                cell = row.createCell(1);
                cell.setCellValue(data.getDescription());
                cell.setCellStyle(defaultStyle);

                if(list.size() > 1) {
                    // price
                    cell = row.createCell(2);
                    cell.setCellValue("");
                    cell.setCellStyle(purpleStyle);

                    // qty
                    String type = data.getType();
                    if(StringUtils.isEmpty(type)) {
                        cell = row.createCell(3);
                        cell.setCellValue("");
                        cell.setCellStyle(purpleStyle);
                    } else {
                        if(type.equals(CodeConfig.TYPE_ADD)) {
                            cell = row.createCell(3);
                            cell.setCellValue("ADD");
                            cell.setCellStyle(purpleRightStyle);
                        } else {
                            cell = row.createCell(3);
                            cell.setCellValue("DEL");
                            cell.setCellStyle(purpleRedStyle);
                        }
                    }

                    cell = row.createCell(4);
                    cell.setCellValue(data.getBmc()!=null?data.getBmc().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(5);
                    cell.setCellValue(data.getNbmc()!=null?data.getNbmc().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(6);
                    cell.setCellValue(data.getTmc()!=null?data.getTmc().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(7);
                    cell.setCellValue(data.getAirCost()!=null?data.getAirCost().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(8);
                    cell.setCellValue(data.getFundings()!=null?data.getFundings().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(9);
                    cell.setCellValue(data.getAdjCost()!=null?data.getAdjCost().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(10);
                    cell.setCellValue(data.getTmcPercent()!=null?data.getTmcPercent().doubleValue():BigDecimal.ZERO.doubleValue());
                    if(data.getTmcPercent()!=null && data.getTmcPercent().doubleValue() < 0) {
                        cell.setCellStyle(redStyle);
                    } else {
                        cell.setCellStyle(defaultStyle);
                    }

                    if(j == list.size() - 1) {
                        data = list.get(0);
                        row = sheet.createRow(index + j + 1);

                        cell = row.createCell(0);
                        cell.setCellValue("CTO");
                        cell.setCellStyle(ctoStyle);

                        cell = row.createCell(1);
                        cell.setCellValue("");
                        cell.setCellStyle(ctoStyle);

                        cell = row.createCell(2);
                        cell.setCellValue(data.getPricing() != null?data.getPricing().doubleValue(): BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(ctoDollarStyle);

                        cell = row.createCell(3);
                        cell.setCellValue(data.getQty() != null? data.getQty() :0);
                        cell.setCellStyle(ctoStyle);

                        cell = row.createCell(4);
                        cell.setCellValue(data.getBmc()!=null?data.getBmc().doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);

                        cell = row.createCell(5);
                        cell.setCellValue(data.getNbmc()!=null?data.getNbmc().doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);

                        cell = row.createCell(6);
                        cell.setCellValue(data.getTmc()!=null?data.getTmc().doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);

                        cell = row.createCell(7);
                        cell.setCellValue(data.getAirCost()!=null?data.getAirCost().doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);

                        cell = row.createCell(8);
                        cell.setCellValue(data.getFundings()!=null?data.getFundings().doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);

                        cell = row.createCell(9);
                        cell.setCellValue(data.getAdjCost()!=null?data.getAdjCost().doubleValue():BigDecimal.ZERO.doubleValue());
                        cell.setCellStyle(defaultStyle);

                        cell = row.createCell(10);
                        cell.setCellValue(data.getTmcPercent()!=null?data.getTmcPercent().doubleValue():BigDecimal.ZERO.doubleValue());
                        if(data.getTmcPercent() != null && data.getTmcPercent().doubleValue() < 0) {
                            cell.setCellStyle(redStyle);
                        } else {
                            cell.setCellStyle(defaultStyle);
                        }

                        index ++;
                    }
                } else {
                    // price
                    cell = row.createCell(2);
                    cell.setCellValue(data.getPricing() != null?data.getPricing().doubleValue(): BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(purpleStyle);

                    // QTY
                    cell = row.createCell(3);
                    cell.setCellValue(data.getQty() != null? data.getQty() :0);
                    cell.setCellStyle(purpleStyle);

                    cell = row.createCell(4);
                    cell.setCellValue(data.getBmc()!=null?data.getBmc().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(5);
                    cell.setCellValue(data.getNbmc()!=null?data.getNbmc().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(6);
                    cell.setCellValue(data.getTmc()!=null?data.getTmc().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(7);
                    cell.setCellValue(data.getAirCost()!=null?data.getAirCost().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(8);
                    cell.setCellValue(data.getFundings()!=null?data.getFundings().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(9);
                    cell.setCellValue(data.getAdjCost()!=null?data.getAdjCost().doubleValue():BigDecimal.ZERO.doubleValue());
                    cell.setCellStyle(defaultStyle);

                    cell = row.createCell(10);
                    cell.setCellValue(data.getTmcPercent()!=null?data.getTmcPercent().doubleValue():BigDecimal.ZERO.doubleValue());
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

    private void setDetailTitle(Integer index, XSSFWorkbook wb, XSSFSheet sheet) {
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

        int[] row1 = new int[]{index+1, index+2};

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

        int[] row2 = new int[]{index+1, index+2};


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
}
