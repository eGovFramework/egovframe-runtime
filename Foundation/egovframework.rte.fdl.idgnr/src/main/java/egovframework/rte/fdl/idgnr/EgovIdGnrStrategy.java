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
package egovframework.rte.fdl.idgnr;

/**
 * Id Generation 정책 Interface 클래스
 * 
 * <p><b>NOTE</b>: 이 서비스를 통해 ID 생성 시 정책기반의 ID가공을 처리할 수 있다.</p>
 * 
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.01  김태호          최초 생성
 * 
 * </pre>
 */
public interface EgovIdGnrStrategy {
    /**
     * 정책을 담은 아이디 생성하여 결과 리턴
     * 
     * @param originalId original id to be converted
     * @return assembled id
     */
    String makeId(String originalId);
}
