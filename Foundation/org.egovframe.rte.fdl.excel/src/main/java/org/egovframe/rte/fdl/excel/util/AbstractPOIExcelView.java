/*
 * Copyright 2008-2014 MOPAS(Ministry of Public Administration and Security).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 출처 : https://jira.springsource.org/browse/SPR-6898?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel
 */

package org.egovframe.rte.fdl.excel.util;

import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;

/**
 * AbstractExcelView support for XSSFWorkbook (xmlx format; POI 3.5+).
 * 
 * <p><b>NOTE:</b> 엑셀 서비스를 제공하기 위해 유용한 유틸을 포함하는 클래스이다.</p>
 * 
 * @author 유지보수팀 이기하
 * @since 2013.05.22
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2013.05.22	이기하				최초 생성
 * 2015.06.02	장동한				filename 변경로직 추가
 * </pre>
 */
public abstract class AbstractPOIExcelView extends AbstractView {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPOIExcelView.class);

    /** The content type for an Excel response */
	 private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Default Constructor. Sets the content type of the view for excel files.
     */
    public AbstractPOIExcelView() {
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    /**
     * Renders the Excel view, given the specified model.
     */
    @Override
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	LOGGER.debug("Created Excel Workbook from scratch");
    	setContentType(CONTENT_TYPE_XLSX);
        buildExcelDocument(model, workbook, request, response);
        
        // Set the filename
        String sFilename = "";
        if (model.get("filename") != null) {
        	sFilename = (String)model.get("filename");
        } else if (request.getAttribute("filename") != null) {
        	sFilename = (String)request.getAttribute("filename");
        } else {
        	sFilename = getClass().getSimpleName();
        }

        // Set the content type.
        response.setContentType(getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + sFilename + ".xlsx\"");

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream();
        out.flush();
        workbook.write(out);
        out.flush();
    }

    /**
     * Subclasses must implement this method to create an Excel Workbook.
     * HSSFWorkbook and XSSFWorkbook are both possible formats.
     */
    //protected abstract Workbook createWorkbook();

	/**
     * Subclasses must implement this method to create an Excel HSSFWorkbook
     * document, given the model.
     * @param model the model Map
     * @param workbook the Excel workbook to complete
     * @param request in case we need locale etc. Shouldn't look at attributes.
     * @param response in case we need to set cookies. Shouldn't write to it.
     */
    protected abstract void buildExcelDocument(Map<String, Object> model, XSSFWorkbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
	 * Convenient method to obtain the cell in the given sheet, row and column.
	 * <p>Creates the row and the cell if they still doesn't already exist.
	 * Thus, the column can be passed as an int, the method making the needed downcasts.</p>
	 * @param sheet a sheet object. The first sheet is usually obtained by workbook.getSheetAt(0)
	 * @param row thr row number
	 * @param col the column number
	 * @return the XSSFCell
	 */
	protected XSSFCell getCell(XSSFSheet sheet, int row, int col) {
		XSSFRow sheetRow = sheet.getRow(row);
		if (sheetRow == null) {
			sheetRow = sheet.createRow(row);
		}
		XSSFCell cell = sheetRow.getCell((short) col);
		if (cell == null) {
			cell = sheetRow.createCell((short) col);
		}
		return cell;
	}

	/**
	 * Convenient method to set a String as text content in a cell.
	 * @param cell the cell in which the text must be put
	 * @param text the text to put in the cell
	 */
	protected void setText(XSSFCell cell, String text) {
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(text);
	}

}