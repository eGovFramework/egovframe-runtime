/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.string;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * EgovDateUtil.getBusinessDaysBetween 테스트.
 * <p>
 * 2026년 1월 1일은 목요일, 1월 31일은 토요일이라 그 달의 평일은 22일이다.
 * 주말·공휴일 제외 규칙과 날짜 형식 검증을 고정한다.
 */
public class EgovDateUtilBusinessDaysTest {

    private static Set<String> holidays(String... days) {
        return new HashSet<>(Arrays.asList(days));
    }

    @Test
    public void testWeekWithoutHolidays() {
        // 월(0105) ~ 금(0109)
        assertEquals(5, EgovDateUtil.getBusinessDaysBetween("20260105", "20260109", null));
        // 토(0103) ~ 일(0104)
        assertEquals(0, EgovDateUtil.getBusinessDaysBetween("20260103", "20260104", null));
        // 월(0105) ~ 일(0111) — 한 주 전체
        assertEquals(5, EgovDateUtil.getBusinessDaysBetween("20260105", "20260111", null));
        // 목(0101) ~ 토(0131) — 2026년 1월 전체
        assertEquals(22, EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", null));
        // 2026년 전체
        assertEquals(261, EgovDateUtil.getBusinessDaysBetween("20260101", "20261231", null));
    }

    @Test
    public void testBothEndsAreIncluded() {
        // 같은 날 하루 — 목요일이면 1일
        assertEquals(1, EgovDateUtil.getBusinessDaysBetween("20260101", "20260101", null));
        // 같은 날 하루 — 토요일이면 0일
        assertEquals(0, EgovDateUtil.getBusinessDaysBetween("20260103", "20260103", null));
    }

    @Test
    public void testReversedRangeIsZero() {
        assertEquals(0, EgovDateUtil.getBusinessDaysBetween("20260131", "20260101", null));
    }

    @Test
    public void testHolidaysAreExcluded() {
        // 신정(목요일) 하루를 뺀다
        assertEquals(21, EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays("20260101")));
        // 평일 공휴일 둘
        assertEquals(20, EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays("20260101", "20260102")));
    }

    @Test
    public void testWeekendAndOutOfRangeHolidaysDoNotChangeTheCount() {
        // 토요일(0103)·일요일(0104) 공휴일은 이미 빠져 있다
        assertEquals(22, EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays("20260103", "20260104")));
        // 범위 밖(3월 2일) 공휴일은 무시한다
        assertEquals(22, EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays("20260302")));
        // 빈 집합은 주말만 제외한 것과 같다
        assertEquals(22, EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays()));
    }

    @Test
    public void testDuplicatedHolidayIsSubtractedOnce() {
        // 같은 날을 형식만 달리해 두 번 넣어도 하루만 뺀다
        assertEquals(21, EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays("20260101", "2026-01-01")));
    }

    @Test
    public void testHyphenatedFormatIsAccepted() {
        assertEquals(22, EgovDateUtil.getBusinessDaysBetween("2026-01-01", "2026-01-31", null));
        // 시작일과 종료일의 형식이 달라도 된다
        assertEquals(21, EgovDateUtil.getBusinessDaysBetween("2026-01-01", "20260131", holidays("2026-01-01")));
    }

    @Test
    public void testLeapDayIsCounted() {
        // 2028-02-29 는 화요일 — 윤년의 2월 29일이 영업일로 잡힌다
        assertEquals(1, EgovDateUtil.getBusinessDaysBetween("20280229", "20280229", null));
        // 2026년은 윤년이 아니라 2월 28일(토)이 마지막
        assertEquals(20, EgovDateUtil.getBusinessDaysBetween("20260201", "20260228", null));
    }

    @Test
    public void testNullArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween(null, "20260131", null));
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("20260101", null, null));
    }

    @Test
    public void testMalformedDatesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("2026011", "20260131", null));
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("2026/01/01", "20260131", null));
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("abcdefgh", "20260131", null));
    }

    @Test
    public void testNonExistentDatesAreRejected() {
        // 2월 30일은 없는 날짜다 — 조용히 3월 2일로 넘어가지 않는다
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("20260230", "20260331", null));
        // 2026년은 윤년이 아니다
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("20260101", "20260229", null));
        // 13월은 없다
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("20261301", "20261331", null));
    }

    @Test
    public void testMalformedHolidayIsRejected() {
        // 공휴일 집합의 오타를 조용히 넘기면 영업일이 하루 늘어난 채 계산된다
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays("2026-0101")));
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getBusinessDaysBetween("20260101", "20260131", holidays("20260230")));
    }

}
