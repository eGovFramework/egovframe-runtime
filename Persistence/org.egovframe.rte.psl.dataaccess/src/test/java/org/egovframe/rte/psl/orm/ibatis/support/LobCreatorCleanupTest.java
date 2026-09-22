package org.egovframe.rte.psl.orm.ibatis.support;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * setParameter 가 만든 LobCreator 는 성공하든 실패하든 정리돼야 한다.
 *
 * <p>드라이버가 파라미터 설정을 거부하면 setParameterInternal 이 SQLException 을 던지는데,
 * 이때도 LobCreator.close 가 불리거나 close 를 부르는 동기화가 등록돼 있어야
 * 임시 LOB 이 남지 않는다.</p>
 */
public class LobCreatorCleanupTest {

    @Test
    public void testLobCreatorIsClosedWhenSetParameterFails() {
        AtomicInteger closeCount = new AtomicInteger();
        LobHandler lobHandler = countingLobHandler(closeCount);
        BlobByteArrayTypeHandler typeHandler = new BlobByteArrayTypeHandler(lobHandler);
        PreparedStatement ps = failingPreparedStatement();

        TransactionSynchronizationManager.initSynchronization();
        try {
            assertThrows(SQLException.class,
                    () -> typeHandler.setParameter(ps, 1, new byte[]{1, 2, 3}, "BLOB"));

            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.beforeCompletion();
            }

            assertEquals(1, closeCount.get(),
                    "setParameter 가 실패해도 LobCreator 는 닫혀야 한다");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private LobHandler countingLobHandler(AtomicInteger closeCount) {
        LobCreator delegate = new DefaultLobHandler().getLobCreator();
        LobCreator counting = (LobCreator) Proxy.newProxyInstance(
                LobCreator.class.getClassLoader(),
                new Class<?>[]{LobCreator.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        closeCount.incrementAndGet();
                    }
                    try {
                        return method.invoke(delegate, args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                });
        return (LobHandler) Proxy.newProxyInstance(
                LobHandler.class.getClassLoader(),
                new Class<?>[]{LobHandler.class},
                (proxy, method, args) -> "getLobCreator".equals(method.getName()) ? counting : null);
    }

    private PreparedStatement failingPreparedStatement() {
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("setBytes".equals(method.getName())) {
                        throw new SQLException("데이터 타입이 맞지 않습니다");
                    }
                    return null;
                });
    }
}
