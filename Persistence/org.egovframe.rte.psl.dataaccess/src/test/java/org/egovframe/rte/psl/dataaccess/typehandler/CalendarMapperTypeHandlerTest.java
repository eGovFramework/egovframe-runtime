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
package org.egovframe.rte.psl.dataaccess.typehandler;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link CalendarMapperTypeHandler} 단위 테스트.
 * <p>
 * 인덱스 기반 {@code getResult(ResultSet, int)} 는 컬럼명 기반 형제 메서드와 달리
 * NULL Timestamp 컬럼에 대한 가드가 없어 {@code Calendar.setTime(null)} 으로 인한
 * NullPointerException 을 유발했다. 본 테스트는 NULL 컬럼이 예외 없이 {@code null} 을
 * 반환하고, 비-NULL 컬럼은 종전과 동일하게 변환됨을 검증한다.
 * </p>
 */
public class CalendarMapperTypeHandlerTest {

    /** 지정한 컬럼인덱스에서 주어진 Timestamp 를 반환하는 ResultSet 프록시 (필요 메서드만 구현). */
    private ResultSet resultSet(final Timestamp ts) {
        return (ResultSet) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    if ("getTimestamp".equals(method.getName())) {
                        return ts;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    public void nullColumnReturnsNullWithoutNpe() throws Exception {
        CalendarMapperTypeHandler handler = new CalendarMapperTypeHandler();
        assertNull(handler.getResult(resultSet(null), 1));
    }

    @Test
    public void nonNullColumnConverts() throws Exception {
        CalendarMapperTypeHandler handler = new CalendarMapperTypeHandler();
        Timestamp ts = Timestamp.valueOf("2024-01-02 03:04:05");
        Calendar cal = handler.getResult(resultSet(ts), 1);
        assertEquals(ts.getTime(), cal.getTimeInMillis());
    }

    /** 지정한 컬럼인덱스에서 주어진 Timestamp 를 반환하는 CallableStatement 프록시 (필요 메서드만 구현). */
    private CallableStatement callableStatement(final Timestamp ts) {
        return (CallableStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{CallableStatement.class},
                (proxy, method, args) -> {
                    if ("getTimestamp".equals(method.getName())) {
                        return ts;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    public void callableStatementNullColumnReturnsNull() throws Exception {
        CalendarMapperTypeHandler handler = new CalendarMapperTypeHandler();
        assertNull(handler.getResult(callableStatement(null), 1));
    }

    @Test
    public void callableStatementNonNullColumnConverts() throws Exception {
        CalendarMapperTypeHandler handler = new CalendarMapperTypeHandler();
        Timestamp ts = Timestamp.valueOf("2024-01-02 03:04:05");
        Calendar cal = handler.getResult(callableStatement(ts), 1);
        assertEquals(ts.getTime(), cal.getTimeInMillis());
    }
}
