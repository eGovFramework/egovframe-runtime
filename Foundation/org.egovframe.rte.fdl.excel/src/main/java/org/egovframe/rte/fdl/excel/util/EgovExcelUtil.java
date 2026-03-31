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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.egovframe.rte.fdl.string.EgovDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 엑셀 서비스 제공을 위한 유틸 클래스.
 * <p>
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자			   수정내용
 * ----------------------------------------------
 * 2009.06.01   윤성종             최초 생성
 * 2013.05.22	이기하             XSSFCell 추가
 * 2014.05.14	이기하             Cell로 통합(HSSFCell, XSSFCell)
 * 2014.09.03	이기하             수식 반환 값이 문자열 아니면 숫자여서 예외처리
 * 2017.02.15 	장동한             시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 */
public final class EgovExcelUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovExcelUtil.class);

    private EgovExcelUtil() {
    }

    /**
     * 엑셀의 셀값을 String 타입으로 변환하여 리턴한다.
     */
    public static String getValue(Cell cell) {
        String result = "";
        if (null == cell) {
            return "";
        }

        if (cell.getCellType() == CellType.BOOLEAN) {
            LOGGER.debug("### EgovExcelUtil Cell.CELL_TYPE_BOOLEAN : {}", CellType.BOOLEAN);
            result = String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == CellType.ERROR) {
            LOGGER.debug("### EgovExcelUtil Cell.CELL_TYPE_ERROR : {}", CellType.ERROR);
        } else if (cell.getCellType() == CellType.FORMULA) {
            LOGGER.debug("### EgovExcelUtil Cell.CELL_TYPE_FORMULA : {}", CellType.FORMULA);
            String stringValue = null;
            String longValue = null;
            try {
                stringValue = cell.getRichStringCellValue().getString();
                longValue = doubleToString(cell.getNumericCellValue());
                //2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
            } catch (IllegalArgumentException e) {
                LOGGER.debug("[{}] EgovExcelUtil getValue() : {}", e.getClass().getName(), e.getMessage());
            }
            if (stringValue != null) {
                result = stringValue;
            } else if (longValue != null) {
                result = longValue;
            } else {
                result = cell.getCellFormula();
            }
        } else if (cell.getCellType() == CellType.NUMERIC) {
            LOGGER.debug("### EgovExcelUtil Cell.CELL_TYPE_NUMERIC : {}", CellType.NUMERIC);
            result = DateUtil.isCellDateFormatted(cell)
                    ? EgovDateUtil.toString(cell.getDateCellValue(), "yyyy/MM/dd", null)
                    : doubleToString(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            LOGGER.debug("### EgovExcelUtil Cell.CELL_TYPE_STRING : {}", CellType.STRING);
            result = cell.getRichStringCellValue().getString();
        } else if (cell.getCellType() == CellType.BLANK) {
            LOGGER.debug("### EgovExcelUtil Cell.CELL_TYPE_BLANK : {}", CellType.BLANK);
        }
        return result;
    }

    /**
     * double 형의 셀 데이터를 String 형으로 변환하여 리턴한다.
     */
    public static String doubleToString(double d) {
        long lValue = (long) d;
        return (lValue == d) ? Long.toString(lValue) : Double.toString(d);
    }

}
