package org.egovframe.rte.fdl.excel.download;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.egovframe.rte.fdl.excel.vo.UsersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class CategoryExcelView extends AbstractView {

	private static final Logger LOGGER  = LoggerFactory.getLogger(CategoryExcelView.class);

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		response.setContentType("application/vnd.ms-excel");

		LOGGER.debug("### buildExcelDocument start !!!");

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("User List");

		HSSFRow row = sheet.createRow(0);
		row.createCell(0).setCellValue("User List");

		// set header information
		row = sheet.createRow(2);
		row.createCell(0).setCellValue("id");
		row.createCell(1).setCellValue("name");
		row.createCell(2).setCellValue("description");
		row.createCell(3).setCellValue("use_yn");
		row.createCell(4).setCellValue("reg_user");

		LOGGER.debug("### buildExcelDocument cast");

		Map<String, Object> map= (Map<String, Object>) model.get("categoryMap");
		List<Object> categories = (List<Object>) map.get("category");

		boolean isVO = false;

		if (categories.size() > 0) {
			Object obj = categories.get(0);
			isVO = obj instanceof UsersVO;
		}

		int rowIndex = 3;

		for (int i = 0; i < categories.size(); i++) {

			row = sheet.createRow(rowIndex);

			if (isVO) {

				LOGGER.debug("### buildExcelDocument VO : {} started!!", i);

				UsersVO category = (UsersVO) categories.get(i);
				row.createCell(0).setCellValue(category.getId());
				row.createCell(1).setCellValue(category.getName());
				row.createCell(2).setCellValue(category.getDescription());
				row.createCell(3).setCellValue(category.getUseYn());
				row.createCell(4).setCellValue(category.getRegUser());

				LOGGER.debug("### buildExcelDocument VO : {} end!!", i);

			} else {	// Map

				LOGGER.debug("### buildExcelDocument Map : {} started!!", i);

				Map<String, String> category = (Map<String, String>) categories.get(i);
				row.createCell(0).setCellValue(category.get("id"));
				row.createCell(1).setCellValue(category.get("name"));
				row.createCell(2).setCellValue(category.get("description"));
				row.createCell(3).setCellValue(category.get("useyn"));
				row.createCell(4).setCellValue(category.get("reguser"));

				LOGGER.debug("### buildExcelDocument Map : {} end!!", i);
			}

			rowIndex++;

		}
	}
}
