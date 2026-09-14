package org.egovframe.rte.fdl.excel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.excel.util.AbstractExcelView;
import org.egovframe.rte.fdl.excel.util.AbstractPOIExcelView;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 다운로드 파일명 RFC 6266/5987 인코딩 검증 — 한글 파일명 filename*=UTF-8'' 처리.
 */
public class EgovExcelViewFilenameTest {

    private static final class TestXlsView extends AbstractExcelView {
        @Override
        protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook,
                                          HttpServletRequest request, HttpServletResponse response) {
            workbook.createSheet("s");
        }
    }

    private static final class TestXlsxView extends AbstractPOIExcelView {
        @Override
        protected void buildExcelDocument(Map<String, Object> model, XSSFWorkbook workbook,
                                          HttpServletRequest request, HttpServletResponse response) {
            workbook.createSheet("s");
        }
    }

    private String renderAndGetDisposition(Object view, String filename) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> model = new HashMap<>();
        model.put("filename", filename);
        if (view instanceof TestXlsView) {
            ((TestXlsView) view).render(model, request, response);
        } else {
            ((TestXlsxView) view).render(model, request, response);
        }
        return response.getHeader("Content-Disposition");
    }

    @Test
    public void 한글_파일명은_RFC5987로_인코딩된다_xls() throws Exception {
        String header = renderAndGetDisposition(new TestXlsView(), "연차보고서");
        assertNotNull(header);
        assertTrue(header.contains("filename*=UTF-8''"), "비ASCII 파일명은 filename* 확장 파라미터 사용: " + header);
        assertFalse(header.contains("연차보고서"), "헤더에 원시 한글이 그대로 실리면 안 된다: " + header);
        // EgovContentDispositions 위임으로 구형 에이전트용 ASCII 폴백(filename=)이 병기된다(RFC 6266 부록 D)
        assertTrue(header.contains("filename=\""), "ASCII 폴백 filename= 병기: " + header);
    }

    @Test
    public void 한글_파일명은_RFC5987로_인코딩된다_xlsx() throws Exception {
        String header = renderAndGetDisposition(new TestXlsxView(), "정산내역");
        assertNotNull(header);
        assertTrue(header.contains("filename*=UTF-8''"));
        assertTrue(header.toLowerCase().startsWith("attachment"));
    }

    @Test
    public void ASCII_파일명은_기존_형식을_유지한다() throws Exception {
        String header = renderAndGetDisposition(new TestXlsView(), "report");
        assertNotNull(header);
        assertTrue(header.contains("filename=\"report.xls\""), header);
    }

}
