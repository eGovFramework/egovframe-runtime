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

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CalendarTypeHandler} 단위 테스트.
 * <p>
 * iBatis 의 {@link ResultGetter} / {@link ParameterSetter} 를
 * (Mockito 미사용) JDK {@link Proxy} 로 대체하여 필요한 메서드만 구현하고,
 * {@code java.util.Calendar} 와 {@code java.sql.Timestamp} 간 변환 경로를 검증한다.
 * 형제 테스트 {@code StringTimestampTypeHandlerTest} 와 동일한 스타일을 따른다.
 * </p>
 */
public class CalendarTypeHandlerTest {

    private static final Timestamp EXPECTED_TS = Timestamp.valueOf("2024-01-02 03:04:05");

    /** 지정한 wasNull/Timestamp 를 반환하는 ResultGetter 프록시 (필요 메서드만 구현). */
    private ResultGetter resultGetter(final boolean wasNull, final Timestamp ts) {
        return (ResultGetter) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultGetter.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "wasNull":
                            return wasNull;
                        case "getTimestamp":
                            return ts;
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    /** setTimestamp 호출의 첫 인자(Timestamp)를 캡처하는 ParameterSetter 프록시. */
    private ParameterSetter capturingTimestampSetter(final AtomicReference<Timestamp> captured) {
        return (ParameterSetter) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ParameterSetter.class},
                (proxy, method, args) -> {
                    if ("setTimestamp".equals(method.getName()) && args != null && args.length >= 1) {
                        captured.set((Timestamp) args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    /** setNull 호출의 SQL 타입 인자를 캡처하는 ParameterSetter 프록시. */
    private ParameterSetter capturingNullSetter(final AtomicInteger capturedType) {
        return (ParameterSetter) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ParameterSetter.class},
                (proxy, method, args) -> {
                    if ("setNull".equals(method.getName()) && args != null && args.length == 1) {
                        capturedType.set((Integer) args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    public void getResultReturnsNullWhenWasNull() throws Exception {
        CalendarTypeHandler handler = new CalendarTypeHandler();
        // wasNull=true 이면 getTimestamp 는 호출되지 않고 null 을 반환해야 한다.
        assertNull(handler.getResult(resultGetter(true, null)));
    }

    @Test
    public void getResultConvertsTimestampToCalendar() throws Exception {
        CalendarTypeHandler handler = new CalendarTypeHandler();
        Object result = handler.getResult(resultGetter(false, EXPECTED_TS));

        assertInstanceOf(Calendar.class, result);
        Calendar cal = (Calendar) result;
        // Calendar 의 시각은 조회된 Timestamp 와 밀리초 단위로 동일해야 한다.
        assertEquals(EXPECTED_TS.getTime(), cal.getTimeInMillis());
    }

    @Test
    public void setParameterUsesSetNullForNullParameter() throws Exception {
        CalendarTypeHandler handler = new CalendarTypeHandler();
        AtomicInteger capturedType = new AtomicInteger(Integer.MIN_VALUE);

        handler.setParameter(capturingNullSetter(capturedType), null);

        // null 파라미터는 Types.DATE 로 setNull 되어야 한다.
        assertEquals(Types.DATE, capturedType.get());
    }

    @Test
    public void setParameterConvertsCalendarToTimestamp() throws Exception {
        CalendarTypeHandler handler = new CalendarTypeHandler();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(EXPECTED_TS.getTime());

        AtomicReference<Timestamp> captured = new AtomicReference<>();
        handler.setParameter(capturingTimestampSetter(captured), cal);

        // Calendar 의 밀리초로 만든 Timestamp 가 세팅되어야 한다.
        assertEquals(new Timestamp(cal.getTimeInMillis()), captured.get());
        assertEquals(EXPECTED_TS.getTime(), captured.get().getTime());
    }

    @Test
    public void valueOfReturnsInputString() {
        CalendarTypeHandler handler = new CalendarTypeHandler();
        // 현재 구현은 입력 문자열을 그대로 반환한다.
        assertEquals("20240102", handler.valueOf("20240102"));
    }
}
