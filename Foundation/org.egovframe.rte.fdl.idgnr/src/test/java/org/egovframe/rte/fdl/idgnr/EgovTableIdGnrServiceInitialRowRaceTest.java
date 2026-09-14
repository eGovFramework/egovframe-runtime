package org.egovframe.rte.fdl.idgnr;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.idgnr.config.IdgnrTestConfig;
import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 채번 테이블에 아직 행이 없을 때 두 인스턴스가 동시에 초기 행을 INSERT 하는 경합을 재현한다.
 *
 * <p>같은 DB 를 쓰는 다른 인스턴스(winner)가 먼저 초기 행을 만들어 커밋한 직후에 이쪽(loser)의
 * INSERT 가 실행되도록, loser 의 DataSource 가 내주는 Connection 의 INSERT 직전에 winner 를 실행한다.
 * 수정 전에는 loser 의 INSERT 가 중복 키로 실패해 "select 실패" FdlException 으로 끝났다.</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IdgnrTestConfig.class})
public class EgovTableIdGnrServiceInitialRowRaceTest {

    private static final String TABLE = "idttest";
    private static final String RACE_NAME = "race";
    private static final int BLOCK_SIZE = 10;

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource
    private ApplicationContext applicationContext;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
        // 잠금 모드(LOCKS)에서는 loser 의 SELECT 가 테이블 공유 잠금을 잡아 winner 의 INSERT 가 대기하므로
        // 다중 인스턴스 배포에서 실제로 일어나는 순서(양쪽 SELECT → 한쪽 INSERT 커밋 → 다른 쪽 INSERT)를
        // 재현하려면 MVCC 가 필요하다.
        setTransactionControl("MVCC");
    }

    @AfterEach
    public void onTearDown() throws SQLException {
        setTransactionControl("LOCKS");
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @Test
    public void testLoserOfInitialRowRaceGetsNextBlockInsteadOfFailing() throws Exception {
        EgovTableIdGnrServiceImpl winner = newService(dataSource, false);
        long[] winnerId = new long[1];
        EgovTableIdGnrServiceImpl loser = newService(new InsertHookDataSource(dataSource, () -> {
            winnerId[0] = winner.getNextLongId();
            return null;
        }), false);

        long loserId = loser.getNextLongId();

        assertEquals(0L, winnerId[0], "먼저 초기 행을 만든 쪽은 첫 블록(0부터)을 받는다");
        assertEquals(BLOCK_SIZE, loserId, "경합에서 진 쪽은 실패하지 않고 다음 블록을 받는다");
        assertEquals(BLOCK_SIZE * 2L, readNextId(), "채번 테이블에는 두 블록이 모두 반영된다");

        // 각자 받은 블록 안에서 계속 채번된다
        assertEquals(1L, winner.getNextLongId());
        assertEquals(BLOCK_SIZE + 1L, loser.getNextLongId());
        assertNotEquals(winner.getNextLongId(), loser.getNextLongId());
    }

    @Test
    public void testLoserOfInitialRowRaceGetsNextBlockWithBigDecimals() throws Exception {
        EgovTableIdGnrServiceImpl winner = newService(dataSource, true);
        BigDecimal[] winnerId = new BigDecimal[1];
        EgovTableIdGnrServiceImpl loser = newService(new InsertHookDataSource(dataSource, () -> {
            winnerId[0] = winner.getNextBigDecimalId();
            return null;
        }), true);

        BigDecimal loserId = loser.getNextBigDecimalId();

        assertEquals(0, BigDecimal.ZERO.compareTo(winnerId[0]));
        assertEquals(0, new BigDecimal(BLOCK_SIZE).compareTo(loserId), "경합에서 진 쪽은 다음 블록을 받는다");
        assertEquals(BLOCK_SIZE * 2L, readNextId());
    }

    @Test
    public void testFirstCallWithoutRaceStillInsertsInitialRow() throws Exception {
        EgovTableIdGnrServiceImpl service = newService(dataSource, false);

        assertEquals(0L, service.getNextLongId(), "경합이 없으면 종전처럼 초기 행을 만들고 0부터 채번한다");
        assertEquals((long) BLOCK_SIZE, readNextId());
    }

    private EgovTableIdGnrServiceImpl newService(DataSource ds, boolean useBigDecimals) throws Exception {
        EgovTableIdGnrServiceImpl service = new EgovTableIdGnrServiceImpl();
        service.setApplicationContext(applicationContext);
        service.setDataSource(ds);
        service.setBlockSize(BLOCK_SIZE);
        service.setTable(TABLE);
        service.setTableName(RACE_NAME);
        service.setUseBigDecimals(useBigDecimals);
        service.afterPropertiesSet();
        return service;
    }

    private long readNextId() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT next_id FROM " + TABLE + " WHERE table_name = '" + RACE_NAME + "'")) {
            assertTrue(rs.next(), "채번 행이 있어야 한다");
            long nextId = rs.getLong(1);
            assertTrue(!rs.next(), "채번 행은 하나여야 한다");
            return nextId;
        }
    }

    private void setTransactionControl(String mode) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET DATABASE TRANSACTION CONTROL " + mode);
            conn.commit();
        }
    }

    /**
     * 내주는 Connection 의 첫 INSERT 직전에 한 번 hook 을 실행하는 DataSource — 다른 인스턴스가
     * 같은 순간 초기 행을 만들어 커밋하는 상황을 만든다.
     */
    private static final class InsertHookDataSource extends DelegatingDataSource {

        private final Callable<Void> hook;
        private final AtomicBoolean fired = new AtomicBoolean();

        private InsertHookDataSource(DataSource target, Callable<Void> hook) {
            super(target);
            this.hook = hook;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return wrap(super.getConnection());
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return wrap(super.getConnection(username, password));
        }

        private Connection wrap(Connection target) {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class},
                    (proxy, method, args) -> {
                        if ("prepareStatement".equals(method.getName()) && args != null && args.length > 0
                                && String.valueOf(args[0]).toUpperCase().startsWith("INSERT INTO")
                                && fired.compareAndSet(false, true)) {
                            hook.call();
                        }
                        try {
                            return method.invoke(target, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }
    }
}
