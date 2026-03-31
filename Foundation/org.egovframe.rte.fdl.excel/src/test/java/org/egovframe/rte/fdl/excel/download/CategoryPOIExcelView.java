package org.egovframe.rte.fdl.excel.download;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.excel.util.AbstractPOIExcelView;
import org.egovframe.rte.fdl.excel.vo.UsersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CategoryPOIExcelView extends AbstractPOIExcelView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryPOIExcelView.class);

    @Override
    protected void buildExcelDocument(Map<String, Object> model, XSSFWorkbook workbook, HttpServletRequest request, HttpServletResponse response) {

        LOGGER.debug("### CategoryPOIExcelView buildExcelDocument() Start ");

        XSSFSheet sheet = workbook.createSheet("User List");
        XSSFCell cell = getCell(sheet, 0, 0);
        setText(cell, "User List");

        // set header information
        setText(getCell(sheet, 2, 0), "id");
        setText(getCell(sheet, 2, 1), "name");
        setText(getCell(sheet, 2, 2), "description");
        setText(getCell(sheet, 2, 3), "use_yn");
        setText(getCell(sheet, 2, 4), "reg_user");

        LOGGER.debug("### CategoryPOIExcelView buildExcelDocument() cast");

        Map<String, Object> map = (Map<String, Object>) model.get("categoryMap");
        List<Object> categories = (List<Object>) map.get("category");

        boolean isVO = false;

        if (!categories.isEmpty()) {
            Object obj = categories.get(0);
            isVO = obj instanceof UsersVO;
        }

        for (int i = 0; i < categories.size(); i++) {
            if (isVO) { // VO
                LOGGER.debug("### CategoryPOIExcelView buildExcelDocument() VO : {} started!!", i);

                UsersVO category = (UsersVO) categories.get(i);
                cell = getCell(sheet, 3 + i, 0);
                setText(cell, category.getId());
                cell = getCell(sheet, 3 + i, 1);
                setText(cell, category.getName());
                cell = getCell(sheet, 3 + i, 2);
                setText(cell, category.getDescription());
                cell = getCell(sheet, 3 + i, 3);
                setText(cell, category.getUseYn());
                cell = getCell(sheet, 3 + i, 4);
                setText(cell, category.getRegUser());

                LOGGER.debug("### CategoryPOIExcelView buildExcelDocument() VO : {} end!!", i);
            } else {    // Map
                LOGGER.debug("### CategoryPOIExcelView buildExcelDocument() Map : {} started!!", i);

                Map<String, String> category = (Map<String, String>) categories.get(i);
                cell = getCell(sheet, 3 + i, 0);
                setText(cell, category.get("id"));
                cell = getCell(sheet, 3 + i, 1);
                setText(cell, category.get("name"));
                cell = getCell(sheet, 3 + i, 2);
                setText(cell, category.get("description"));
                cell = getCell(sheet, 3 + i, 3);
                setText(cell, category.get("useyn"));
                cell = getCell(sheet, 3 + i, 4);
                setText(cell, category.get("reguser"));

                LOGGER.debug("### CategoryPOIExcelView buildExcelDocument() Map : {} end!!", i);
            }
        }
    }

}
