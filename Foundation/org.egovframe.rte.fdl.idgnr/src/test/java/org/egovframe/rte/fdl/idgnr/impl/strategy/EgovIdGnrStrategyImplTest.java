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
package org.egovframe.rte.fdl.idgnr.impl.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * EgovIdGnrStrategyImplTest 클래스
 * <p>
 * ID 생성 정책 구현({@link EgovIdGnrStrategyImpl})의 fillString/makeId 동작을
 * 결정적으로 검증한다. 소스는 수정하지 않고 현행 동작을 그대로 문서화한다.
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2026.07.28  개발팀          최초 생성
 */
public class EgovIdGnrStrategyImplTest {

    /**
     * fillString - 원본보다 자리수가 크면 앞을 채움 문자로 패딩한다.
     */
    @Test
    public void testFillStringPadsWithFillChar() {
        assertEquals("00007", EgovIdGnrStrategyImpl.fillString("7", '0', 5));
        assertEquals("00042", EgovIdGnrStrategyImpl.fillString("42", '0', 5));
        assertEquals("xxxab", EgovIdGnrStrategyImpl.fillString("ab", 'x', 5));
    }

    /**
     * fillString - 자리수와 원본 길이가 같으면(cipers == length) 원본을 그대로 반환한다.
     */
    @Test
    public void testFillStringBoundaryEqualLength() {
        assertEquals("12345", EgovIdGnrStrategyImpl.fillString("12345", '0', 5));
        assertEquals("A", EgovIdGnrStrategyImpl.fillString("A", '0', 1));
    }

    /**
     * fillString - 자리수가 원본 길이보다 작으면(cipers &lt; length, 오버플로) null 을 반환한다.
     */
    @Test
    public void testFillStringOverflowReturnsNull() {
        assertNull(EgovIdGnrStrategyImpl.fillString("123456", '0', 5));
        assertNull(EgovIdGnrStrategyImpl.fillString("AB", '0', 1));
    }

    /**
     * makeId - prefix 와 패딩된 문자열이 결합된다.
     */
    @Test
    public void testMakeIdCombinesPrefixAndPaddedId() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl("TEST-", 5, '0');
        assertEquals("TEST-00007", strategy.makeId("7"));
    }

    /**
     * makeId - 기본 생성자 + setter 로 정책을 구성한 경우(기본 fillChar '0', 기본 cipers 5).
     */
    @Test
    public void testMakeIdWithDefaultConstructorAndSetters() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("ID");
        // 기본 cipers(5), 기본 fillChar('0') 적용 확인
        assertEquals("ID00009", strategy.makeId("9"));

        strategy.setCipers(3);
        strategy.setFillChar('*');
        assertEquals("ID**1", strategy.makeId("1"));
    }

    /**
     * makeId - 경계(cipers == 원본 길이)에서는 패딩 없이 prefix 와 원본이 결합된다.
     */
    @Test
    public void testMakeIdBoundaryEqualLength() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl("P", 5, '0');
        assertEquals("P12345", strategy.makeId("12345"));
    }

    /**
     * makeId - 오버플로(cipers &lt; 원본 길이) 시 fillString 이 null 을 반환하여
     * 문자열 결합 과정에서 "prefix" + "null" 리터럴이 만들어지는 현행 동작을 문서화한다.
     * (소스 수정 없이 현재 구현의 동작만 검증)
     */
    @Test
    public void testMakeIdOverflowProducesNullLiteral() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl("PRE", 2, '0');
        assertEquals("PREnull", strategy.makeId("12345"));
    }
}
