package org.egovframe.rte.fdl.excel;

import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFHeaderFooter;
import org.egovframe.rte.fdl.excel.config.ExcelTestConfig;
import org.egovframe.rte.fdl.excel.util.EgovExcelUtil;
import org.egovframe.rte.fdl.excel.vo.PersonHourVO;
import org.egovframe.rte.fdl.filehandling.EgovFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jxls.builder.JxlsOutputFile;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExcelTestConfig.class)
public class EgovPOIExcelServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovPOIExcelServiceTest.class);
    private final String fileLocation = "testdata";
    @Resource(name = "excelService")
    private EgovExcelService excelService;

    /**
     * [Flow #-1] 엑셀 파일 생성 : 새 엑셀 파일을 생성함
     */
    @Test
    public void testWriteExcelFile() throws IOException {
        LOGGER.debug("### EgovPOIExcelServiceTest testWriteExcelFile() Start ");

        String sheetName1 = "first sheet";
        String sheetName2 = "second sheet";

        StringBuilder sb = new StringBuilder();
        sb.append(fileLocation).append("/").append("testWriteExcelFile.xlsx");
        if (EgovFileUtil.isExistsFile(sb.toString())) {
            EgovFileUtil.delete(new File(sb.toString()));
        }

        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet(sheetName1);
        workbook.createSheet(sheetName2);
        workbook.createSheet();

        // 엑셀 파일 생성
        Workbook workbook1 = excelService.createWorkbook(workbook, sb.toString());

        // 파일 존재 확인, 저장된 Sheet명 일치 점검
        assertTrue(EgovFileUtil.isExistsFile(sb.toString()));
        assertEquals(sheetName1, workbook1.getSheetName(0));
        assertEquals(sheetName2, workbook1.getSheetName(1));

        LOGGER.debug("### EgovPOIExcelServiceTest testWriteExcelFile() End ");
    }

    /**
     * [Flow #-2] 액셀 파일 수정 : 엑샐 파일 내 셀의 내용을 변경하고 저장할 수 있음
     */
    @Test
    public void testModifyCellContents() throws IOException {
        LOGGER.debug("### EgovPOIExcelServiceTest testModifyCellContents() Start ");

        String content = "Use \n with word wrap on to create a new line";
        short rownum = 2;
        int cellnum = 2;

        StringBuilder sb = new StringBuilder();
        sb.append(fileLocation).append("/").append("testModifyCellContents.xlsx");
        if (EgovFileUtil.isExistsFile(sb.toString())) {
            EgovFileUtil.delete(new File(sb.toString()));
        }

        // 엑셀 파일 생성
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("test");
        Font font = workbook.createFont();
        Sheet sheet = workbook.getSheetAt(0);
        sheet.setColumnWidth(20, (int) ((50 * 8) / ((double) 1 / 20)));
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        Row row = sheet.createRow(rownum);
        row.setHeight((short) 0x349);
        Cell cell = row.createCell(cellnum);
        cell.setCellValue(new XSSFRichTextString(content));
        cell.setCellStyle(cellStyle);

        // 엑셀 파일 저장
        excelService.createWorkbook(workbook, sb.toString());

        // 엑셀 파일 로드
        Workbook workbook2 = excelService.loadWorkbook(sb.toString(), new XSSFWorkbook());
        Sheet sheet1 = workbook2.getSheetAt(0);
        Row row1 = sheet1.getRow(rownum);
        Cell cell1 = row1.getCell(cellnum);

        // 수정된 셀의 내용 점검
        assertNotSame("TEST", cell1.getRichStringCellValue().toString());
        assertEquals(content, cell1.getRichStringCellValue().toString());

        LOGGER.debug("### EgovPOIExcelServiceTest testModifyCellContents() End ");
    }

    /**
     * [Flow #-3] 엑셀 파일 속성 수정 : 엑셀 파일의 속성(셀의 크기, Border의 속성, 셀의 색상, 정렬 등)을 수정함
     */
    @Test
    public void testWriteExcelFileAttribute() throws IOException {
        LOGGER.debug("### EgovPOIExcelServiceTest testWriteExcelFileAttribute() Start ");

        short rowheight = 40 * 10;
        int columnwidth = 30;

        StringBuilder sb = new StringBuilder();
        sb.append(fileLocation).append("/").append("testWriteExcelFileAttribute.xlsx");
        if (EgovFileUtil.isExistsFile(sb.toString())) {
            EgovFileUtil.delete(new File(sb.toString()));
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("new sheet");
        workbook.createSheet("second sheet");
        sheet.setDefaultRowHeight(rowheight);
        sheet.setDefaultColumnWidth(columnwidth);
        Font font = workbook.createFont();
        XSSFCellStyle xssfCellStyle = workbook.createCellStyle();
        xssfCellStyle.setFont(font);
        xssfCellStyle.setWrapText(true);
        xssfCellStyle.setAlignment(HorizontalAlignment.RIGHT); // 정렬
        xssfCellStyle.setFillPattern(FillPatternType.DIAMONDS); // 무늬 스타일
        xssfCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex()); // 무늬 색
        xssfCellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex()); // 배경색
        sheet.setDefaultColumnStyle((short) 0, xssfCellStyle);

        Workbook workbook1 = excelService.createWorkbook(workbook, sb.toString());
        Sheet sheet1 = workbook1.getSheetAt(0);
        assertEquals(rowheight, sheet1.getDefaultRowHeight());
        assertEquals(columnwidth, sheet1.getDefaultColumnWidth());

        CellStyle cellStyle1 = workbook1.getCellStyleAt((short) (workbook1.getNumCellStyles() - 1));
        assertEquals(HorizontalAlignment.RIGHT, cellStyle1.getAlignment());
        assertEquals(IndexedColors.BLUE.getIndex(), cellStyle1.getFillForegroundColor());
        assertEquals(IndexedColors.RED.getIndex(), cellStyle1.getFillBackgroundColor());

        LOGGER.debug("### EgovPOIExcelServiceTest testWriteExcelFileAttribute() End ");
    }

    /**
     * [Flow #-4] 엑셀 문서 속성 수정 : 엑셀 파일 문서의 속성(Header, Footer)을 수정함
     */
    @Test
    public void testModifyDocAttribute() throws IOException {
        LOGGER.debug("### EgovPOIExcelServiceTest testModifyDocAttribute() Start ");

        StringBuilder sb = new StringBuilder();
        sb.append(fileLocation).append("/").append("testModifyDocAttribute.xls");
        if (EgovFileUtil.isExistsFile(sb.toString())) {
            EgovFileUtil.delete(new File(sb.toString()));
        }

        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        Sheet sheet = workbook.createSheet("doc test sheet");
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(1);
        cell.setCellValue(new XSSFRichTextString("Header/Footer Test"));

        // Header
        Header header = sheet.getHeader();
        header.setCenter("Center Header");
        header.setLeft("Left Header");
        header.setRight(XSSFHeaderFooter.stripFields("Stencil-Normal Italic font and size 16"));

        // Footer
        Footer footer = sheet.getFooter();
        footer.setCenter(XSSFHeaderFooter.stripFields("Fixedsys"));
        footer.setLeft("Left Footer");
        footer.setRight("Right Footer");

        // 엑셀 파일 저장
        excelService.createWorkbook(workbook, sb.toString());
        assertTrue(EgovFileUtil.isExistsFile(sb.toString()));

        // 검증
        Workbook workbook1 = excelService.loadWorkbook(sb.toString(), new XSSFWorkbook());
        Sheet sheet1 = workbook1.getSheet("doc test sheet");

        Header header1 = sheet1.getHeader();
        assertEquals("Center Header", header1.getCenter());
        assertEquals("Left Header", header1.getLeft());
        assertEquals(XSSFHeaderFooter.stripFields("Stencil-Normal Italic font and size 16"), header1.getRight());

        Footer footer1 = sheet1.getFooter();
        assertEquals("Right Footer", footer1.getRight());
        assertEquals("Left Footer", footer1.getLeft());
        assertEquals(XSSFHeaderFooter.stripFields("Fixedsys"), footer1.getCenter());

        LOGGER.debug("### EgovPOIExcelServiceTest testModifyDocAttribute() End ");
    }

    /**
     * [Flow #-5] 셀 내용 추출 : 엑셀 파일을 읽어 특정 셀의 값을 얻어 옴
     */
    @Test
    public void testGetCellContents() throws IOException {
        LOGGER.debug("### EgovPOIExcelServiceTest testGetCellContents() Start ");

        StringBuilder sb = new StringBuilder();
        sb.append(fileLocation).append("/").append("testGetCellContents.xls");
        if (EgovFileUtil.isExistsFile(sb.toString())) {
            EgovFileUtil.delete(new File(sb.toString()));
        }

        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        Sheet sheet = workbook.createSheet("cell test sheet");
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        for (int i = 0; i < 100; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < 5; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(new XSSFRichTextString("row " + i + ", cell " + j));
                cell.setCellStyle(cellStyle);
            }
        }

        // 엑셀 파일 저장
        excelService.createWorkbook(workbook, sb.toString());

        // 검증
        Workbook workbook1 = excelService.loadWorkbook(sb.toString(), new XSSFWorkbook());
        Sheet sheet1 = workbook1.getSheet("cell test sheet");
        for (int i = 0; i < 100; i++) {
            Row row1 = sheet1.getRow(i);
            for (int j = 0; j < 5; j++) {
                Cell cell1 = row1.getCell(j);
                assertEquals("row " + i + ", cell " + j, cell1.getRichStringCellValue().toString());
            }
        }

        LOGGER.debug("### EgovPOIExcelServiceTest testGetCellContents() End ");
    }

    /**
     * [Flow #-6] 셀 속성 수정 : 특정 셀의 속성(폰트, 사이즈 등)을 수정함
     */
    @Test
    public void testModifyCellAttribute() throws IOException {
        LOGGER.debug("### EgovPOIExcelServiceTest testModifyCellAttribute() Start ");

        StringBuilder sb = new StringBuilder();
        sb.append(fileLocation).append("/").append("testModifyCellAttribute.xls");
        if (EgovFileUtil.isExistsFile(sb.toString())) {
            EgovFileUtil.delete(new File(sb.toString()));
            LOGGER.debug("Delete file....{}", sb);
        }

        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        Sheet sheet = workbook.createSheet("cell test sheet2");
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeight((short) 16);
        font.setBold(true);
        font.setFontName("fixedsys");
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.RIGHT); // cell 정렬
        cellStyle.setWrapText(true);
        for (int i = 0; i < 100; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < 5; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(new XSSFRichTextString("row " + i + ", cell " + j));
                cell.setCellStyle(cellStyle);
            }
        }

        // 엑셀 파일 저장
        excelService.createWorkbook(workbook, sb.toString());

        // 검증
        XSSFWorkbook workbook1 = excelService.loadWorkbook(sb.toString(), new XSSFWorkbook());
        Sheet sheet1 = workbook1.getSheet("cell test sheet2");
        XSSFCellStyle cellStyle1 = workbook1.getCellStyleAt((short) (workbook1.getNumCellStyles() - 1));
        XSSFFont font1 = cellStyle1.getFont();

        for (int i = 0; i < 100; i++) {
            Row row1 = sheet1.getRow(i);
            for (int j = 0; j < 5; j++) {
                Cell cell1 = row1.getCell(j);
                assertEquals(16, font1.getFontHeight());
                assertTrue(font1.getBold());
                assertEquals(HorizontalAlignment.RIGHT, cellStyle1.getAlignment());
                assertTrue(cellStyle1.getWrapText());
            }
        }

        LOGGER.debug("### EgovPOIExcelServiceTest testModifyCellAttribute() End ");
    }

    /**
     * [Flow #-7] 공통 템플릿 사용 : 공통 템플릿을 사용하여 일관성을 유지함
     */
    @Test
    public void testUseTemplate1() throws IOException {
        LOGGER.debug("### EgovPOIExcelServiceTest testUseTemplate1() Start ");

        StringBuilder sb = new StringBuilder().append(fileLocation).append("/template/").append("template1.xlsx");
        StringBuilder sbResult = new StringBuilder().append(fileLocation).append("/").append("testUseTemplate1.xlsx");

        Object[][] sample_data = {
                {"Yegor Kozlov", "YK", 5.0, 8.0, 10.0, 5.0, 5.0, 7.0, 6.0},
                {"Gisella Bronzetti", "GB", 4.0, 3.0, 1.0, 3.5, null, null, 4.0},
        };

        FileInputStream in = new FileInputStream(sb.toString());
        Workbook workbook = new XSSFWorkbook(in);
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < sample_data.length; i++) {
            Row row = sheet.getRow(i + 2);
            for (int j = 0; j < sample_data[i].length; j++) {
                if (sample_data[i][j] == null) continue;
                Cell cell = row.getCell(j);
                if (sample_data[i][j] instanceof String) {
                    cell.setCellValue(new XSSFRichTextString((String) sample_data[i][j]));
                } else {
                    cell.setCellValue((Double) sample_data[i][j]);
                }
            }
        }

        // 수식 재계산
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        FileOutputStream out = new FileOutputStream(sbResult.toString());
        workbook.write(out);
        out.close();
        in.close();

        // 검증
        Workbook workbook1 = excelService.loadWorkbook(sbResult.toString(), new XSSFWorkbook());
        Sheet sheet1 = workbook1.getSheetAt(0);
        for (int i = 0; i < sample_data.length; i++) {
            Row row = sheet1.getRow(2 + i);
            for (int j = 0; j < sample_data[i].length; j++) {
                Cell cell = row.getCell(j);
                if (sample_data[i][j] == null) {
                    assertEquals(cell.getCellType(), CellType.BLANK);
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    assertEquals(sample_data[i][j], cell.getNumericCellValue());
                } else {
                    assertEquals(sample_data[i][j], cell.getRichStringCellValue().getString());
                }
            }
        }

        LOGGER.debug("### EgovPOIExcelServiceTest testUseTemplate1() End ");
    }

    /**
     * [Flow #-7-1] 공통 템플릿 사용 : 공통 템플릿을 사용하여 일관성을 유지함
     * jXLS 사용
     */
    @Test
    public void testUseTemplate2() throws FileNotFoundException {
        LOGGER.debug("### EgovPOIExcelServiceTest testUseTemplate2() Start ");

        StringBuilder sb = new StringBuilder().append(fileLocation).append("/template/").append("template2.xlsx");
        StringBuilder sbResult = new StringBuilder().append(fileLocation).append("/").append("testUseTemplate2.xlsx");

        Double[][] value = {{5.0, 8.0, 10.0, 5.0, 5.0, 7.0, 6.0}, {4.0, 3.0, 1.0, 3.5, null, null, 4.0}};

        List<PersonHourVO> persons = new ArrayList<>();
        PersonHourVO person = new PersonHourVO();
        person.setName("Yegor Kozlov");
        person.setId("YK");
        person.setMon(5.0);
        person.setTue(8.0);
        person.setWed(10.0);
        person.setThu(5.0);
        person.setFri(5.0);
        person.setSat(7.0);
        person.setSun(6.0);
        persons.add(person);

        PersonHourVO person1 = new PersonHourVO();
        person1.setName("Gisella Bronzetti");
        person1.setId("GB");
        person1.setMon(4.0);
        person1.setTue(3.0);
        person1.setWed(1.0);
        person1.setThu(3.5);
        person1.setSun(4.0);
        persons.add(person1);

        Map<String, Object> data = new HashMap<>();
        data.put("persons", persons);
        JxlsPoiTemplateFillerBuilder.newInstance()
                .withTemplate(sb.toString())
                .build()
                .fill(data, new JxlsOutputFile(new File(sbResult.toString())));

        // 검증
        Workbook workbook1 = excelService.loadWorkbook(sbResult.toString(), new XSSFWorkbook());
        Sheet sheet = workbook1.getSheetAt(0);
        for (int i = 0; i < 2; i++) {
            Row rowValue = sheet.getRow(i + 2);
            for (int j = 0; j < 7; j++) {
                Cell cellValue = rowValue.getCell((j + 2));
                if (value[i][j] == null) {
                    assertEquals(cellValue.getCellType(), CellType.BLANK);
                } else if (cellValue.getCellType() == CellType.NUMERIC) {
                    assertEquals(value[i][j], Double.valueOf(cellValue.getNumericCellValue()));
                } else {
                    assertEquals(value[i][j], cellValue.getRichStringCellValue().getString());
                }
            }
        }

        LOGGER.debug("### EgovPOIExcelServiceTest testUseTemplate2() End ");
    }

    /**
     * 셀 Data type 테스트
     * 셀의 값이 Null 인 경우 오류발생
     */
    @Test
    public void testCellDataFormat() {
        Workbook workbook = excelService.loadWorkbook(fileLocation + "/" + "testDataFormat.xlsx", new XSSFWorkbook());
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(7);
        Cell cell = row.getCell(0);
        assertEquals("2009/04/01", EgovExcelUtil.getValue(cell));

        row = sheet.getRow(8);
        cell = row.getCell(0);
        assertEquals("2009/04/02", EgovExcelUtil.getValue(cell));
    }

}
