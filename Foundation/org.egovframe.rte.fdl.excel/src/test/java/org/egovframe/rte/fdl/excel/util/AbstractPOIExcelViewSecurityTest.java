package org.egovframe.rte.fdl.excel.util;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 엑셀 다운로드 뷰의 수식 인젝션(CWE-1236)·Content-Disposition 헤더 인젝션 방어를 검증한다.
 */
public class AbstractPOIExcelViewSecurityTest {

    private static final String[] PAYLOADS = {"=HYPERLINK(\"http://evil\")", "+1+1", "-2+3", "@SUM(A1)", "\tcmd", "\rcmd"};

    @Test
    public void 수식_시작_문자는_리터럴로_기록한다() throws Exception {
        MockHttpServletResponse response = render("report");
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i < PAYLOADS.length; i++) {
                assertEquals("'" + PAYLOADS[i], sheet.getRow(i).getCell(0).getStringCellValue());
            }
            assertEquals("plain", sheet.getRow(PAYLOADS.length).getCell(0).getStringCellValue());
        }
    }

    @Test
    public void 파일명의_개행과_따옴표는_헤더에_그대로_실리지_않는다() throws Exception {
        MockHttpServletResponse response = render("a\"; x=\"y\r\nSet-Cookie: s=1");
        String header = response.getHeader("Content-Disposition");
        assertFalse(header, header.contains("\r") || header.contains("\n"));
        assertFalse(header, header.contains("\"; x=\""));
        assertTrue(header, header.startsWith("attachment;"));
    }

    @Test
    public void 한글_파일명은_RFC_5987_로_인코딩한다() throws Exception {
        String header = render("보고서").getHeader("Content-Disposition");
        assertTrue(header, header.contains("filename*=UTF-8''"));
    }

    private static MockHttpServletResponse render(String filename) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("filename", filename);
        MockHttpServletResponse response = new MockHttpServletResponse();
        new AbstractPOIExcelView() {
            @Override
            protected void buildExcelDocument(Map<String, Object> m, XSSFWorkbook workbook,
                                              HttpServletRequest request, HttpServletResponse resp) {
                XSSFSheet sheet = workbook.createSheet("data");
                for (int i = 0; i < PAYLOADS.length; i++) {
                    setText(getCell(sheet, i, 0), PAYLOADS[i]);
                }
                setText(getCell(sheet, PAYLOADS.length, 0), "plain");
            }
        }.render(model, new MockHttpServletRequest(), response);
        return response;
    }

}
