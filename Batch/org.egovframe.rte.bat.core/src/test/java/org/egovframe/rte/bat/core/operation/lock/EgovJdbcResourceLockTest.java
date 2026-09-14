package org.egovframe.rte.bat.core.operation.lock;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovJdbcResourceLock 의 배타 획득·재진입·해제, 고아 락 탈취, 대기 획득, Step 리스너, 재진입 갱신 경합을 검증한다.
 */
public class EgovJdbcResourceLockTest {

    private static JDBCDataSource dataSource;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:batchlock");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE EGOV_BATCH_RESOURCE_LOCK IF EXISTS");
            statement.execute("CREATE TABLE EGOV_BATCH_RESOURCE_LOCK ("
                    + "RESOURCE_ID VARCHAR(200) NOT NULL PRIMARY KEY,"
                    + "OWNER_ID VARCHAR(200) NOT NULL,"
                    + "ACQUIRED_TIME TIMESTAMP NOT NULL)");
        }
    }

    @BeforeEach
    public void clearLocks() throws Exception {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM EGOV_BATCH_RESOURCE_LOCK");
        }
    }

    @Test
    public void testExclusiveAcquireReentryAndRelease() {
        EgovJdbcResourceLock lock = new EgovJdbcResourceLock(dataSource);

        assertTrue(lock.tryAcquire("/data/out/daily.dat", "jobA#1"));
        assertTrue(lock.isLocked("/data/out/daily.dat"));
        assertFalse(lock.tryAcquire("/data/out/daily.dat", "jobB#2"));
        assertTrue(lock.tryAcquire("/data/out/daily.dat", "jobA#1"));
        assertFalse(lock.release("/data/out/daily.dat", "jobB#2"));
        assertTrue(lock.release("/data/out/daily.dat", "jobA#1"));
        assertFalse(lock.isLocked("/data/out/daily.dat"));
        assertTrue(lock.tryAcquire("/data/out/daily.dat", "jobB#2"));
    }

    @Test
    public void testExpiredOrphanLockIsTakenOver() throws Exception {
        EgovJdbcResourceLock lock = new EgovJdbcResourceLock(dataSource);
        lock.setExpireMillis(1000L);
        assertTrue(lock.tryAcquire("orphaned", "deadServer#1"));
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE EGOV_BATCH_RESOURCE_LOCK SET ACQUIRED_TIME = TIMESTAMP '2000-01-01 00:00:00'"
                    + " WHERE RESOURCE_ID = 'orphaned'");
        }

        assertTrue(lock.tryAcquire("orphaned", "aliveServer#2"));
        assertFalse(lock.tryAcquire("orphaned", "deadServer#1"));
    }

    @Test
    public void testAcquireWaitsUntilReleased() throws Exception {
        EgovJdbcResourceLock lock = new EgovJdbcResourceLock(dataSource);
        assertTrue(lock.tryAcquire("waited", "holder#1"));

        assertFalse(lock.acquire("waited", "waiter#2", 300L, 50L));

        Thread releaser = new Thread(() -> {
            try {
                Thread.sleep(300L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            lock.release("waited", "holder#1");
        });
        releaser.start();
        assertTrue(lock.acquire("waited", "waiter#2", 5000L, 50L));
        releaser.join();
    }

    @Test
    public void testStepListenerAcquiresBeforeStepAndReleasesAfterStep() {
        EgovJdbcResourceLock lock = new EgovJdbcResourceLock(dataSource);
        EgovResourceLockStepListener listener = new EgovResourceLockStepListener(lock, "step-resource");
        JobExecution jobExecution = new JobExecution(7L);
        jobExecution.setJobInstance(new JobInstance(3L, "lockJob"));
        StepExecution stepExecution = new StepExecution("s1", jobExecution);

        listener.beforeStep(stepExecution);
        assertTrue(lock.isLocked("step-resource"));

        listener.afterStep(stepExecution);
        assertFalse(lock.isLocked("step-resource"));
    }

    @Test
    public void testStepListenerFailsFastWhenResourceIsHeldByAnotherExecution() {
        EgovJdbcResourceLock lock = new EgovJdbcResourceLock(dataSource);
        assertTrue(lock.tryAcquire("busy-resource", "otherJob#99"));
        EgovResourceLockStepListener listener = new EgovResourceLockStepListener(lock, "busy-resource");
        listener.setWaitMillis(200L);
        listener.setPollMillis(50L);
        JobExecution jobExecution = new JobExecution(8L);
        jobExecution.setJobInstance(new JobInstance(4L, "blockedJob"));
        StepExecution stepExecution = new StepExecution("s1", jobExecution);

        assertThrows(EgovResourceLockException.class, () -> listener.beforeStep(stepExecution));
    }

    @Test
    public void testInvalidTableNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EgovJdbcResourceLock(dataSource, "LOCK; DROP TABLE X"));
        assertThrows(IllegalArgumentException.class, () -> new EgovJdbcResourceLock(null));
    }

    /**
     * SELECT 로 자신이 소유자임을 확인한 직후 다른 프로세스가 만료 탈취·forceRelease 로 소유권을 가져가는 경쟁을
     * UPDATE 직전 훅으로 재현한다. 재진입 갱신이 0건이면 락을 보유했다고 보고하면 안 된다.
     */
    @Test
    public void testReentrantRefreshDoesNotReportOwnershipWhenUpdateAffectsNoRow() throws Exception {
        String resourceId = "/data/out/race.dat";
        AtomicBoolean raceArmed = new AtomicBoolean(false);
        DataSource racing = racingDataSource(dataSource, sql -> {
            if (sql.startsWith("UPDATE") && raceArmed.compareAndSet(true, false)) {
                try (Connection connection = dataSource.getConnection();
                        Statement statement = connection.createStatement()) {
                    statement.execute("DELETE FROM EGOV_BATCH_RESOURCE_LOCK WHERE RESOURCE_ID = '" + resourceId + "'");
                    statement.execute("INSERT INTO EGOV_BATCH_RESOURCE_LOCK (RESOURCE_ID, OWNER_ID, ACQUIRED_TIME) VALUES ('"
                            + resourceId + "', 'jobC#9', CURRENT_TIMESTAMP)");
                }
            }
        });
        EgovJdbcResourceLock lock = new EgovJdbcResourceLock(racing);
        assertTrue(lock.tryAcquire(resourceId, "jobA#1"));

        raceArmed.set(true);
        assertFalse(lock.tryAcquire(resourceId, "jobA#1"));

        assertFalse(raceArmed.get());
        assertFalse(lock.release(resourceId, "jobA#1"));
        assertTrue(lock.release(resourceId, "jobC#9"));
    }

    /** 특정 SQL 이 준비되기 직전에 훅을 실행하는 DataSource 프록시(경쟁 조건 재현용). */
    private static DataSource racingDataSource(DataSource target, SqlHook beforePrepare) {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[] {DataSource.class},
                (proxy, method, args) -> {
                    Object result = invoke(target, method, args);
                    if ("getConnection".equals(method.getName())) {
                        Connection connection = (Connection) result;
                        return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class},
                                (connectionProxy, connectionMethod, connectionArgs) -> {
                                    if ("prepareStatement".equals(connectionMethod.getName()) && connectionArgs != null
                                            && connectionArgs.length > 0) {
                                        beforePrepare.beforePrepare(String.valueOf(connectionArgs[0]));
                                    }
                                    return invoke(connection, connectionMethod, connectionArgs);
                                });
                    }
                    return result;
                });
    }

    private static Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @FunctionalInterface
    private interface SqlHook {
        void beforePrepare(String sql) throws Exception;
    }

}
