/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
 */
package org.egovframe.rte.fdl.excel.util;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.view.AbstractView;

import java.io.IOException;
import java.util.Map;

/**
 * AbstractExcelView support for XSSFWorkbook (xlsx format; POI 3.5+).
 * 엑셀 서비스를 제공하기 위해 유용한 유틸을 포함하는 클래스이다.
 * <p>
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2013.05.22	이기하				최초 생성
 * 2015.06.02	장동한				filename 변경로직 추가
 */
public abstract class AbstractPOIExcelView extends AbstractView {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPOIExcelView.class);

    /**
     * The content type for an Excel response
     */
    private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Default Constructor. Sets the content type of the view for excel files
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
    protected final void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        LOGGER.debug("Created Excel Workbook from scratch");

        setContentType(CONTENT_TYPE_XLSX);

        buildExcelDocument(model, workbook, request, response);

        // 파일명 설정
        String sFilename = "";
        if (model.get("filename") != null) {
            sFilename = (String) model.get("filename");
        } else if (request.getAttribute("filename") != null) {
            sFilename = (String) request.getAttribute("filename");
        } else {
            sFilename = getClass().getSimpleName();
        }

        // 응답 설정
        response.setContentType(getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + sFilename + ".xlsx\"");

        // 스트림 처리
        ServletOutputStream out = response.getOutputStream();
        out.flush();
        workbook.write(out);
        out.flush();
    }

    /**
     * Subclasses must implement this method to create an Excel HSSFWorkbook document, given the model.
     */
    protected abstract void buildExcelDocument(
            Map<String, Object> model,
            XSSFWorkbook workbook,
            HttpServletRequest request,
            HttpServletResponse response);

    protected XSSFCell getCell(XSSFSheet sheet, int row, int col) {
        XSSFRow sheetRow = sheet.getRow(row);
        if (ObjectUtils.isEmpty(sheetRow)) {
            sheetRow = sheet.createRow(row);
        }

        XSSFCell cell = sheetRow.getCell(col);
        if (ObjectUtils.isEmpty(cell)) {
            cell = sheetRow.createCell(col);
        }

        return cell;
    }

    protected void setText(XSSFCell cell, String text) {
        cell.setCellType(CellType.STRING);
        cell.setCellValue(text);
    }

}
