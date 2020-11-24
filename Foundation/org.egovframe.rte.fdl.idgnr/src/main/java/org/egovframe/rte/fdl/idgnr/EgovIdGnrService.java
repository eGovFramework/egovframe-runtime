/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.fdl.idgnr;

import java.math.BigDecimal;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;

/**
 * Id Generation 서비스의 인터페이스 클래스
 * 
 * <p><b>NOTE</b>: 이 서비스를 통해 어플리케이션에서 UUID, DB에 저장된 값을 이용한 유일키를 제공 받을 수 있다.</p>
 * 
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.02.01	김태호				최초 생성
 * </pre>
 */
public interface EgovIdGnrService {

    /**
     * BigDecimal 형식의 Id 제공
     * @return 다음 ID
     * @throws FdlException 다음 BigDecimal Id 를 얻지 못했을 경우
     */
    BigDecimal getNextBigDecimalId() throws FdlException;

    /**
     * Long 형식의 Id 제공
     * @return 다음 ID
     * @throws FdlException 유효한 Long Id의 범위를 벗어 났을 경우
     */
    long getNextLongId() throws FdlException;

    /**
     * Integer 형식의 Id 제공
     * @return 다음 ID
     * @throws FdlException 유효한 Integer Id의 범위를 벗어 났을 경우
     */
    int getNextIntegerId() throws FdlException;

    /**
     * Short 형식의 Id 제공
     * @return 다음 ID
     * @throws FdlException 유효한 Short Id의 범위를 벗어 났을 경우
     */
    short getNextShortId() throws FdlException;

    /**
     * Byte 형식의 Id 제공
     * @return 다음 ID
     * @throws FdlException 유효한 Byte Id의 범위를 벗어 났을 경우
     */
    byte getNextByteId() throws FdlException;

    /**
     * String 형식의 Id 제공
     * @return 다음 ID
     * @throws FdlException 유효한 String Id의 범위를 벗어 났을 경우
     */
    String getNextStringId() throws FdlException;

    /**
     * 정책을 스트링으로 입력받고 String 형식의 Id 제공
     * @param strategyId 정책 String 정보
     * @return 다음 ID
     * @throws FdlException 유효한 String Id의 범위를 벗어 났을 경우
     */
    String getNextStringId(String strategyId) throws FdlException;

    /**
     * 정책을 정책클래스로 입력받고 String 형식의 Id 제공
     * @param strategy 정책 인스턴스 정보
     * @return the next Id.
     * @throws FdlException 유효한 String Id의 범위를 벗어 났을 경우
     */
    String getNextStringId(EgovIdGnrStrategy strategy) throws FdlException;

}
