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
package org.egovframe.rte.fdl.logging.util;

import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EgovResourceReleaserTest 클래스
 * <p>
 * 리소스 정리 유틸리티({@link EgovResourceReleaser})의 단위 테스트.
 * Mockito 등 목킹 프레임워크 없이 순수 Java 스텁/Proxy 로 각 분기(null 안전,
 * 정상 close 호출, 미지원 Wrapper 예외)를 결정적으로 검증한다.
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2026.07.28  개발팀          최초 생성
 */
public class EgovResourceReleaserTest {

    /**
     * close(Closeable...) - 정상 리소스가 실제로 close 되는지 확인.
     */
    @Test
    public void testCloseInvokesCloseOnResources() {
        FlagCloseable c1 = new FlagCloseable();
        FlagCloseable c2 = new FlagCloseable();

        EgovResourceReleaser.close(c1, c2);

        assertTrue(c1.closed, "첫 번째 리소스는 close 되어야 한다");
        assertTrue(c2.closed, "두 번째 리소스는 close 되어야 한다");
    }

    /**
     * close(Closeable...) - null 요소가 섞여 있어도 예외 없이 안전하게 처리.
     */
    @Test
    public void testCloseIsNullSafe() {
        FlagCloseable c1 = new FlagCloseable();

        assertDoesNotThrow(() -> EgovResourceReleaser.close(c1, null));
        assertTrue(c1.closed, "null 요소가 있어도 유효한 리소스는 close 되어야 한다");
        assertDoesNotThrow(() -> EgovResourceReleaser.close((Closeable) null));
    }

    /**
     * close(Closeable...) - close 중 IOException 이 발생해도 밖으로 전파되지 않고 무시.
     */
    @Test
    public void testCloseSwallowsIOException() {
        Closeable throwing = () -> {
            throw new IOException("boom");
        };
        assertDoesNotThrow(() -> EgovResourceReleaser.close(throwing));
    }

    /**
     * closeDBObjects(Wrapper...) - ResultSet/Statement/Connection 각 타입이 실제로 close 되는지 확인.
     */
    @Test
    public void testCloseDBObjectsClosesEachJdbcType() {
        CloseTracker rsTracker = new CloseTracker();
        CloseTracker stmtTracker = new CloseTracker();
        CloseTracker connTracker = new CloseTracker();

        ResultSet rs = newJdbcProxy(ResultSet.class, rsTracker);
        Statement stmt = newJdbcProxy(Statement.class, stmtTracker);
        Connection conn = newJdbcProxy(Connection.class, connTracker);

        EgovResourceReleaser.closeDBObjects(rs, stmt, conn);

        assertTrue(rsTracker.closed, "ResultSet 은 close 되어야 한다");
        assertTrue(stmtTracker.closed, "Statement 는 close 되어야 한다");
        assertTrue(connTracker.closed, "Connection 은 close 되어야 한다");
    }

    /**
     * closeDBObjects(Wrapper...) - null 요소는 예외 없이 건너뛴다.
     */
    @Test
    public void testCloseDBObjectsIsNullSafe() {
        CloseTracker rsTracker = new CloseTracker();
        ResultSet rs = newJdbcProxy(ResultSet.class, rsTracker);

        assertDoesNotThrow(() -> EgovResourceReleaser.closeDBObjects(rs, null));
        assertTrue(rsTracker.closed);
        assertDoesNotThrow(() -> EgovResourceReleaser.closeDBObjects((Wrapper) null));
    }

    /**
     * closeDBObjects(Wrapper...) - close 중 SQLException 이 발생해도 전파되지 않고 무시.
     */
    @Test
    public void testCloseDBObjectsSwallowsSQLException() {
        CloseTracker tracker = new CloseTracker();
        tracker.throwOnClose = true;
        ResultSet rs = newJdbcProxy(ResultSet.class, tracker);

        assertDoesNotThrow(() -> EgovResourceReleaser.closeDBObjects(rs));
    }

    /**
     * closeDBObjects(Wrapper...) - ResultSet/Statement/Connection 이 아닌 Wrapper 는 IllegalArgumentException.
     */
    @Test
    public void testCloseDBObjectsThrowsOnUnsupportedWrapper() {
        Wrapper unsupported = (Wrapper) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Wrapper.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) {
                        return "unsupportedWrapper";
                    }
                    return defaultReturn(method);
                });

        assertThrows(IllegalArgumentException.class,
                () -> EgovResourceReleaser.closeDBObjects(unsupported));
    }

    /**
     * closeSockets(Socket...) - 각 소켓의 shutdownOutput/close 가 호출되는지 확인.
     */
    @Test
    public void testCloseSocketsClosesEachSocket() {
        StubSocket s1 = new StubSocket();
        StubSocket s2 = new StubSocket();

        EgovResourceReleaser.closeSockets(s1, s2);

        assertTrue(s1.shutdownOutputCalled && s1.closed);
        assertTrue(s2.shutdownOutputCalled && s2.closed);
    }

    /**
     * closeSockets(Socket...) - null 요소는 예외 없이 건너뛴다.
     */
    @Test
    public void testCloseSocketsIsNullSafe() {
        StubSocket s1 = new StubSocket();

        assertDoesNotThrow(() -> EgovResourceReleaser.closeSockets(s1, null));
        assertTrue(s1.closed);
        assertDoesNotThrow(() -> EgovResourceReleaser.closeSockets((Socket) null));
    }

    /**
     * closeSocketObjects(Socket, ServerSocket) - Socket 과 ServerSocket 이 모두 close 되는지 확인.
     */
    @Test
    public void testCloseSocketObjectsClosesBoth() throws IOException {
        StubSocket socket = new StubSocket();
        StubServerSocket server = new StubServerSocket();

        EgovResourceReleaser.closeSocketObjects(socket, server);

        assertTrue(socket.shutdownOutputCalled && socket.closed);
        assertTrue(server.closed);
    }

    /**
     * closeSocketObjects(Socket, ServerSocket) - null 인자는 예외 없이 처리.
     */
    @Test
    public void testCloseSocketObjectsIsNullSafe() {
        assertDoesNotThrow(() -> EgovResourceReleaser.closeSocketObjects(null, null));
    }

    /**
     * exceptionHandling(Socket) - shutdownOutput/close 시 IOException 이 발생해도 무시.
     */
    @Test
    public void testExceptionHandlingSwallowsIOException() {
        StubSocket socket = new StubSocket();
        socket.throwOnShutdown = true;
        socket.throwOnClose = true;

        assertDoesNotThrow(() -> EgovResourceReleaser.exceptionHandling(socket));
    }

    // ---------------------------------------------------------------------
    // 순수 Java 스텁/헬퍼
    // ---------------------------------------------------------------------

    /** close 호출 여부를 기록하는 간단한 Closeable 스텁. */
    private static final class FlagCloseable implements Closeable {
        boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }

    /** JDBC Proxy 의 close 호출 여부 및 예외 발생을 제어하는 트래커. */
    private static final class CloseTracker {
        boolean closed;
        boolean throwOnClose;
    }

    /** JDBC 인터페이스(ResultSet/Statement/Connection)용 동적 Proxy 생성. */
    @SuppressWarnings("unchecked")
    private static <T> T newJdbcProxy(Class<T> jdbcType, CloseTracker tracker) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("close".equals(method.getName()) && method.getParameterCount() == 0) {
                if (tracker.throwOnClose) {
                    throw new SQLException("boom");
                }
                tracker.closed = true;
                return null;
            }
            if ("toString".equals(method.getName())) {
                return jdbcType.getSimpleName() + "Proxy";
            }
            return defaultReturn(method);
        };
        return (T) Proxy.newProxyInstance(
                EgovResourceReleaserTest.class.getClassLoader(),
                new Class<?>[]{jdbcType},
                handler);
    }

    /** Proxy 메서드 기본 반환값(primitive 안전값 포함). */
    private static Object defaultReturn(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class) {
            return Boolean.FALSE;
        }
        if (returnType == int.class || returnType == short.class || returnType == byte.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == double.class) {
            return 0.0d;
        }
        if (returnType == float.class) {
            return 0.0f;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }

    /** shutdownOutput/close 호출을 기록하는 Socket 스텁. */
    private static class StubSocket extends Socket {
        boolean shutdownOutputCalled;
        boolean closed;
        boolean throwOnShutdown;
        boolean throwOnClose;

        @Override
        public void shutdownOutput() throws IOException {
            shutdownOutputCalled = true;
            if (throwOnShutdown) {
                throw new IOException("shutdown boom");
            }
        }

        @Override
        public synchronized void close() throws IOException {
            closed = true;
            if (throwOnClose) {
                throw new IOException("close boom");
            }
        }
    }

    /** close 호출을 기록하는 ServerSocket 스텁. */
    private static class StubServerSocket extends ServerSocket {
        boolean closed;

        StubServerSocket() throws IOException {
            super();
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }
}
