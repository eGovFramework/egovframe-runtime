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
import org.springframework.web.servlet.view.AbstractView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 엑셀 뷰 다운로드 파일명의 <b>헤더 인젝션(CWE-113)</b> 회귀 검증.
 *
 * <p>파일명은 모델 속성 {@code filename} 또는 요청 속성에서 오므로 신뢰할 수 없다. 값 생성 규칙 자체는
 * {@code EgovContentDispositions} 가 담당하고 그쪽 테스트가 입력 변형 배터리를 맡는다 — 여기서는
 * 두 뷰(xls·xlsx)와 두 입력 경로(모델·요청 속성)가 실제로 그 규칙을 타는지, 헤더가 정확히 한 번만
 * 설정되는지를 고정한다.</p>
 */
public class EgovExcelViewFilenameSecurityTest {

    private static final String BS = String.valueOf((char) 92);

    private static final class XlsView extends AbstractExcelView {
        @Override
        protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook,
                                          HttpServletRequest request, HttpServletResponse response) {
            workbook.createSheet("s");
        }
    }

    private static final class XlsxView extends AbstractPOIExcelView {
        @Override
        protected void buildExcelDocument(Map<String, Object> model, XSSFWorkbook workbook,
                                          HttpServletRequest request, HttpServletResponse response) {
            workbook.createSheet("s");
        }
    }

    @Test
    public void 파일명의_CR_LF는_헤더에_남지_않는다() throws Exception {
        for (AbstractView view : List.of(new XlsView(), new XlsxView())) {
            for (boolean viaRequestAttribute : new boolean[] {false, true}) {
                MockHttpServletResponse response = render(view, "evil\r\nSet-Cookie: a=b\rX: y\n", viaRequestAttribute);
                String header = response.getHeader("Content-Disposition");
                assertFalse(header.contains("\r") || header.contains("\n"), "CR/LF 잔존: " + header);
                assertEquals(1, response.getHeaders("Content-Disposition").size(), "헤더는 한 번만 설정된다");
            }
        }
    }

    @Test
    public void 경로가_섞인_파일명은_이름만_남는다() throws Exception {
        assertTrue(render(new XlsView(), "../../etc/passwd", false).getHeader("Content-Disposition")
                .contains("filename=\"passwd.xls\""));
        String backslash = render(new XlsxView(), ".." + BS + ".." + BS + "win.ini", false).getHeader("Content-Disposition");
        assertTrue(backslash.contains("filename=\"win.ini.xlsx\""), backslash);
        assertFalse(backslash.contains("..") || backslash.contains(BS), backslash);
    }

    @Test
    public void 인용부호는_이스케이프되어_파라미터_경계가_유지된다() throws Exception {
        String header = render(new XlsView(), "a\"; filename=\"b.exe", false).getHeader("Content-Disposition");
        // 이스케이프된 \" 와 \\ 를 걷어내면 quoted-string 을 감싸는 인용부호 2개만 남아야 한다
        String stripped = header.replace(BS + BS, "").replace(BS + "\"", "");
        assertEquals(2, stripped.length() - stripped.replace("\"", "").length(), "인용부호 경계가 깨짐: " + header);
        assertTrue(header.startsWith("attachment; filename=\""), header);
    }

    @Test
    public void 파일명이_없어도_헤더는_한_번_유효하게_설정된다() throws Exception {
        MockHttpServletResponse response = render(new XlsView(), null, false);
        assertEquals(1, response.getHeaders("Content-Disposition").size());
        assertTrue(response.getHeader("Content-Disposition").startsWith("attachment; filename=\""));
    }

    private static MockHttpServletResponse render(AbstractView view, String filename, boolean viaRequestAttribute) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> model = new HashMap<>();
        if (filename != null) {
            if (viaRequestAttribute) {
                request.setAttribute("filename", filename);
            } else {
                model.put("filename", filename);
            }
        }
        view.render(model, request, response);
        return response;
    }
}
