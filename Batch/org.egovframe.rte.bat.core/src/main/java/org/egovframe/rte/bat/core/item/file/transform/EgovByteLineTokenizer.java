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
package org.egovframe.rte.bat.core.item.file.transform;

import java.util.List;

/**
 * Byte단위로 아이템을 잘라서 처리하는 인터페이스
 *
 * @author 실행환경 개발팀 이도형
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.20	배치실행개발팀		최초 생성
 * </pre>
 * @since 2012.07.20
 */
public interface EgovByteLineTokenizer<T> {

    /**
     * Token 목록을 생성한다. 실제 구현은 하위 클래스에서 이루어 진다.
     *
     * @return List String  : token 목록
     */
    List<String> tokenize(byte[] line) throws Exception;

}
