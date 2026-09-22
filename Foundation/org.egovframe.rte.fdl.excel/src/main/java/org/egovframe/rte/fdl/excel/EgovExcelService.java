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
package org.egovframe.rte.fdl.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

/**
 * 엑셀 서비스를 제공하기 위해 여러 기능들을 선언하는 인터페이스이다.
 * <p>
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종			최초 생성
 * 2013.05.22	이기하			XSSF, SXSSF 형식 추가
 * 2014.05.14	이기하			XSSF형식 구분자 추가 및 workbook으로 변경
 * 2024.08.17	이백행			시큐어코딩 Exception 제거
 * 2026.09.10	실행환경 개발팀		xls·xlsx 자동 감지 업로드(uploadExcelAuto) 추가
 */
public interface EgovExcelService {

    /**
     * Workbook 객체를 생성하여 엑셀파일을 생성한다.
     * 전달받은 Workbook을 닫지 않고 그대로 반환하며, 호출자가 이어서 사용·수정·재저장할 수 있다. 닫는 책임은 호출자에게 있다.
     */
    Workbook createWorkbook(Workbook wb, String filepath);

    /**
     * 엑셀 Template를 로딩하여 엑셀파일을 생성한다.
     */
    Workbook loadExcelTemplate(String templateName);

    /**
     * xlsx 엑셀 Template를 로딩하여 엑셀파일을 생성한다.
     */
    XSSFWorkbook loadExcelTemplate(String templateName, XSSFWorkbook type);

    /**
     * 엑셀 파일을 로딩한다.
     */
    Workbook loadWorkbook(String filepath);

    /**
     * xlsx 엑셀 파일을 로딩한다.
     */
    XSSFWorkbook loadWorkbook(String filepath, XSSFWorkbook type);

    /**
     * 엑셀 파일을 로딩한다.
     */
    Workbook loadWorkbook(InputStream fileIn);

    /**
     *
     */
    XSSFWorkbook loadWorkbook(InputStream fileIn, XSSFWorkbook type);

    /**
     * 엑셀파일을 업로드하여 DB에 일괄저장한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     * commit할 건수를 입력한다.
     */
    Integer uploadExcel(String queryId, Sheet sheet, int start, long commitCnt);

    /**
     * 엑셀파일을 업로드하여 DB에 일괄저장한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn, int start, long commitCnt);

    /**
     * xlxs 엑셀파일을 업로드하여 DB에 일괄저장한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn, int start, long commitCnt, XSSFWorkbook type);

    /**
     * 엑셀파일을 저장한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn);

    /**
     * xlsx 엑셀파일을 저장한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn, XSSFWorkbook type);

    /**
     * 엑셀파일을 업로드하여 DB에 일괄저장한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn, short sheetIndex, int start, long commitCnt);

    /**
     * xlsx 엑셀파일을 업로드하여 DB에 일괄저장한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn, short sheetIndex, int start, long commitCnt, XSSFWorkbook type);

    /**
     * 엑셀파일을 업로드하여 DB에 일괄저장한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn, String sheetName, int start, long commitCnt);

    /**
     * xlsx 엑셀파일을 업로드하여 DB에 일괄저장한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     */
    Integer uploadExcel(String queryId, InputStream fileIn, String sheetName, int start, long commitCnt, XSSFWorkbook type);

    /**
     * 엑셀 파일을 xls·xlsx <b>자동 감지</b>로 로딩해 첫 번째 시트를 DB 에 일괄 저장한다.
     * <p>
     * 기존 API 는 xls 가 기본 오버로드이고 xlsx 는 타입 구분용 {@code XSSFWorkbook} 인자로 가르는 구조라 호출자가 형식을 미리
     * 알아야 했다. 이 메서드는 확장자가 아닌 내용으로 형식을 감지하는 단일 진입점이다. 기존 구현체와의 호환을 위해 default
     * 메서드로 제공하며, 지원하지 않는 구현체는 {@link UnsupportedOperationException} 을 던진다.
     *
     * @param queryId   저장 SQL 매핑 쿼리 ID
     * @param fileIn    엑셀 입력 스트림(xls·xlsx 자동 감지, 닫는 책임은 호출자)
     * @param start     업로드 시작 행(0부터)
     * @param commitCnt 커밋 단위 행 수(0 이면 전체를 한 번에)
     * @return 저장된 행 수
     */
    default Integer uploadExcelAuto(String queryId, InputStream fileIn, int start, long commitCnt) {
        throw new UnsupportedOperationException("uploadExcelAuto is not supported by this implementation");
    }

}
