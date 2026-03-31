package org.egovframe.rte.fdl.excel;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.excel.config.ExcelTestConfig;
import org.egovframe.rte.fdl.excel.download.CategoryPOIExcelController;
import org.egovframe.rte.fdl.excel.download.CategoryPOIExcelView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExcelTestConfig.class)
public class EgovPOIExcelControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        ViewResolver viewResolver = new BeanNameViewResolver();
        this.mockMvc = MockMvcBuilders.standaloneSetup(new CategoryPOIExcelController())
                .setViewResolvers(viewResolver)
                .setSingleView(new CategoryPOIExcelView())
                .build();
    }

    /**
     * [Flow #-6] 엑셀 파일 생성 : 멥으로 데이터를 전송하여 엑셀로 다운로드
     */
    @Test
    public void testExcelDownloadMap() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/sale/listPOIExcelCategory.do"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // 엑셀 내용을 파싱하여 검증
        InputStream is = new ByteArrayInputStream(response.getContentAsByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        XSSFSheet sheet = workbook.getSheetAt(0);
        assertEquals("User List", sheet.getRow(0).getCell(0).getStringCellValue());
    }

    /**
     * [Flow #-7] 엑셀 파일 생성 :  VO로 데이터를 전송하여 엑셀로 다운로드
     */
    @Test
    public void testExcelDownloadVO() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/sale/listPOIExcelVOCategory.do"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // 엑셀 내용을 파싱하여 검증
        InputStream is = new ByteArrayInputStream(response.getContentAsByteArray());
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        XSSFSheet sheet = workbook.getSheetAt(0);
        assertEquals("User List", sheet.getRow(0).getCell(0).getStringCellValue());
    }

}
