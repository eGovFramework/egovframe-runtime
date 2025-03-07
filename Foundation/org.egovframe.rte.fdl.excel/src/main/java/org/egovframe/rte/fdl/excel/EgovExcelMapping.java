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
 */
package org.egovframe.rte.fdl.excel;

import org.apache.poi.ss.usermodel.Row;

/**
 * 엑셀파일의 DB업로드용 VO매핑 클래스.
 * 
 * <p><b>NOTE:</b> 엑셀파일의 DB 업로드 기능을 제공하기 위한 사용자 VO 매핑 추상클래스이다.</p>
 * 
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01   윤성종         최초 생성
 * 2014.05.14   이기하         HSSFRow -> Row 변경
 * 2024.08.17   이백행         시큐어코딩 Exception 제거
 * </pre>
 */
// CHECKSTYLE:OFF
public abstract class EgovExcelMapping {
// CHECKSTYLE:ON

    /**
     * 엑셀파일의 DB 업로드를 위한 사용자 VO 매핑 메소드
     *
     * @param row
    * @return
    */
    public abstract Object mappingColumn(Row row);

}
