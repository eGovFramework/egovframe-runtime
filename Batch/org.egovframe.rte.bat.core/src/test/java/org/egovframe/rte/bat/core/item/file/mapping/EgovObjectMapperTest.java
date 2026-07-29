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
package org.egovframe.rte.bat.core.item.file.mapping;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.IncorrectTokenCountException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * EgovObjectMapper 단위 테스트
 *
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.07.28	z3rotig4r			최초 생성
 * </pre>
 */
class EgovObjectMapperTest {

    /**
     * setNames(null) 호출 시 NullPointerException 이 아니라
     * Assert.notNull 이 던지는 IllegalArgumentException 이 노출되어야 한다.
     */
    @Test
    void setNames_null_throwsIllegalArgumentException() {
        EgovObjectMapper<SampleVO> mapper = new EgovObjectMapper<>();
        assertThrows(IllegalArgumentException.class, () -> mapper.setNames(null));
    }

    /**
     * 정상 매핑: token 수와 names 수가 일치하면 VO 각 필드에 값이 세팅된다.
     */
    @Test
    void mapObject_happyPath() {
        EgovObjectMapper<SampleVO> mapper = new EgovObjectMapper<>();
        mapper.setType(SampleVO.class);
        mapper.setNames(new String[]{"col1", "col2"});
        mapper.afterPropertiesSet();

        SampleVO vo = mapper.mapObject(Arrays.asList("v1", "v2"));

        assertEquals("v1", vo.getCol1());
        assertEquals("v2", vo.getCol2());
    }

    /**
     * 계약 확인: token 수와 names 수가 다르면 IncorrectTokenCountException 을 던진다.
     */
    @Test
    void mapObject_tokenCountMismatch_throwsIncorrectTokenCountException() {
        EgovObjectMapper<SampleVO> mapper = new EgovObjectMapper<>();
        mapper.setType(SampleVO.class);
        mapper.setNames(new String[]{"col1", "col2"});
        mapper.afterPropertiesSet();

        assertThrows(IncorrectTokenCountException.class,
                () -> mapper.mapObject(Arrays.asList("v1", "v2", "v3")));
    }

    /**
     * 테스트용 VO. reflection 매핑을 위해 public 무인자 생성자와 setter/getter 를 갖는다.
     */
    public static class SampleVO {

        private String col1;

        private String col2;

        public String getCol1() {
            return col1;
        }

        public void setCol1(String col1) {
            this.col1 = col1;
        }

        public String getCol2() {
            return col2;
        }

        public void setCol2(String col2) {
            this.col2 = col2;
        }

    }

}
