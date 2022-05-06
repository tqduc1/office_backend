package com.cmcglobal.backend.excel;

import com.cmcglobal.backend.dto.response.dot.ReportDotResponse;
import com.cmcglobal.backend.dto.response.report.ReportResponsePaging;
import com.cmcglobal.backend.entity.pojo.DotExcelEnum;
import com.cmcglobal.backend.utility.DateUtility;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.cmcglobal.backend.constant.Constant.YYYY_MM_DD;

public class ReportExcel {
    private XSSFSheet sheet;
    private final XSSFWorkbook workbook;
    private final List<ReportResponsePaging.ReportResponse> reportResponseList;
    private final LocalDate exportDate;

    public ReportExcel(List<ReportResponsePaging.ReportResponse> reportResponseList, String exportDate) {
        this.reportResponseList = reportResponseList;
        this.exportDate = LocalDate.parse(exportDate, DateTimeFormatter.ofPattern(YYYY_MM_DD));
        this.workbook = new XSSFWorkbook();
    }


    private void writeHeaderLine() {
        sheet = workbook.createSheet("Dot management - " + exportDate);

//        Row rowTitle = sheet.createRow(0);
        CellStyle styleTitle = workbook.createCellStyle();
//        styleTitle.setFo;
//        createCell(rowTitle,0, LocalDateTime.now().toString(),);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(14);
        style.setFont(font);
        int columnCount = 0;
        for (DotExcelEnum header : DotExcelEnum.values()) {
            createCell(row, columnCount++, header.getType(), style);
        }
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);

        int numbering = 0;
        for (ReportResponsePaging.ReportResponse reportResponse : reportResponseList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, numbering++, style);
            createCell(row, columnCount++, reportResponse.getGroup(), style);
            createCell(row, columnCount++, reportResponse.getDepartment(), style);
            createCell(row, columnCount++, reportResponse.getBuildingName(), style);
            createCell(row, columnCount++, reportResponse.getFloorName(), style);
            createCell(row, columnCount++, reportResponse.getNumberOfAllocatedDot(), style);
            createCell(row, columnCount++, reportResponse.getNumberOfOccupiedDot().toString(), style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
