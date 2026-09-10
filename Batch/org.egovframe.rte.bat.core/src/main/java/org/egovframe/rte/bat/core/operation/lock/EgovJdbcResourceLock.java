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
package org.egovframe.rte.bat.core.operation.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Pattern;

/**
 * DB 기반 멀티서버 배치 자원 배타 락.
 *
 * <p>여러 서버의 배치가 같은 파일·자원을 동시에 조작하는 것을 막는다. 락 테이블의 PK INSERT 성공·실패의 원자성으로 락을
 * 판정하므로 표준 JDBC 만 쓰며 DBMS 에 중립적이다.</p>
 *
 * <p>테이블 규약(DBMS 에 맞게 타입 조정):</p>
 * <pre>
 * CREATE TABLE EGOV_BATCH_RESOURCE_LOCK (
 *     RESOURCE_ID   VARCHAR(200) NOT NULL PRIMARY KEY,
 *     OWNER_ID      VARCHAR(200) NOT NULL,
 *     ACQUIRED_TIME TIMESTAMP    NOT NULL
 * );
 * </pre>
 *
 * <ul>
 *   <li>같은 소유자의 재획득(재진입)은 성공으로 처리하고 획득 시각을 갱신한다.</li>
 *   <li>{@code expireMillis > 0} 이면 획득 후 그 시간이 지난 락(서버 다운 등으로 남은 고아 락)을 지우고 탈취를 시도한다.</li>
 * </ul>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovJdbcResourceLock {

    /** 기본 락 테이블명 */
    public static final String DEFAULT_TABLE_NAME = "EGOV_BATCH_RESOURCE_LOCK";

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovJdbcResourceLock.class);

    /** SQL 에 그대로 이어 붙이는 테이블명은 식별자 문자만 허용한다. */
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_.]+");

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;

    /** 고아 락 만료 시간(ms). 0 이하이면 만료 탈취를 하지 않는다. */
    private long expireMillis = 0L;

    public EgovJdbcResourceLock(DataSource dataSource) {
        this(dataSource, DEFAULT_TABLE_NAME);
    }

    /**
     * @param dataSource 락 테이블이 있는 DataSource
     * @param tableName  락 테이블명(null 또는 빈 값이면 {@value #DEFAULT_TABLE_NAME})
     * @throws IllegalArgumentException dataSource 가 null 이거나 테이블명에 식별자 문자 외의 문자가 있는 경우
     */
    public EgovJdbcResourceLock(DataSource dataSource, String tableName) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null");
        }
        String name = (tableName == null || tableName.isEmpty()) ? DEFAULT_TABLE_NAME : tableName;
        if (!TABLE_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("tableName must consist of identifier characters only: " + name);
        }
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tableName = name;
    }

    /**
     * 고아 락 만료 시간(ms). 0 이하이면 만료 탈취를 하지 않는다(기본 0).
     */
    public void setExpireMillis(long expireMillis) {
        this.expireMillis = expireMillis;
    }

    /**
     * 락 획득을 1회 시도한다(비대기).
     *
     * @param resourceId 자원 식별자(예: 파일 경로, 논리 자원명)
     * @param ownerId    소유자 식별자(예: jobName#executionId, 호스트명)
     * @return 획득 성공 여부
     */
    public boolean tryAcquire(String resourceId, String ownerId) {
        requireText(resourceId, "resourceId");
        requireText(ownerId, "ownerId");

        if (insertLock(resourceId, ownerId)) {
            LOGGER.debug("Resource lock acquired: {} by {}", resourceId, ownerId);
            return true;
        }
        // 같은 소유자의 재진입 — 획득 시각을 갱신하고 성공 처리
        List<String> owners = jdbcTemplate.queryForList(
                "SELECT OWNER_ID FROM " + tableName + " WHERE RESOURCE_ID = ?", String.class, resourceId);
        if (owners.size() == 1 && ownerId.equals(owners.get(0))) {
            int refreshed = jdbcTemplate.update(
                    "UPDATE " + tableName + " SET ACQUIRED_TIME = ? WHERE RESOURCE_ID = ? AND OWNER_ID = ?",
                    now(), resourceId, ownerId);
            if (refreshed > 0) {
                return true;
            }
            // SELECT 와 UPDATE 사이에 소유권이 바뀌었다(만료 탈취·forceRelease). 영향 행 0건을 보유로 보고하면 두 프로세스가
            // 동시에 락을 가졌다고 믿게 된다. 빈 자리면 다시 잡고, 다른 소유자가 잡았으면 아래 만료 탈취 판정으로 넘어간다.
            LOGGER.warn("Resource lock ownership changed during reentrant refresh: {} (owner {})", resourceId, ownerId);
            if (insertLock(resourceId, ownerId)) {
                return true;
            }
        }
        // 고아 락 만료 탈취 시도
        if (expireMillis > 0) {
            Timestamp cutoff = new Timestamp(System.currentTimeMillis() - expireMillis);
            int expired = jdbcTemplate.update(
                    "DELETE FROM " + tableName + " WHERE RESOURCE_ID = ? AND ACQUIRED_TIME < ?", resourceId, cutoff);
            if (expired > 0 && insertLock(resourceId, ownerId)) {
                LOGGER.warn("Resource lock taken over from expired owner: {} by {}", resourceId, ownerId);
                return true;
            }
        }
        return false;
    }

    /**
     * 락을 얻을 때까지 대기하며 시도한다.
     *
     * @param resourceId 자원 식별자
     * @param ownerId    소유자 식별자
     * @param waitMillis 최대 대기 시간(ms)
     * @param pollMillis 재시도 간격(ms, 최소 10)
     * @return 획득 성공 여부(대기 시간 안에 실패하거나 인터럽트되면 false)
     */
    public boolean acquire(String resourceId, String ownerId, long waitMillis, long pollMillis) {
        long deadline = System.currentTimeMillis() + Math.max(waitMillis, 0);
        long interval = Math.max(pollMillis, 10);
        while (true) {
            if (tryAcquire(resourceId, ownerId)) {
                return true;
            }
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 소유한 락을 해제한다.
     *
     * @param resourceId 자원 식별자
     * @param ownerId    소유자 식별자
     * @return 해제되었으면 true(소유자가 아니거나 락이 없으면 false)
     */
    public boolean release(String resourceId, String ownerId) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM " + tableName + " WHERE RESOURCE_ID = ? AND OWNER_ID = ?", resourceId, ownerId);
        if (deleted > 0) {
            LOGGER.debug("Resource lock released: {} by {}", resourceId, ownerId);
        }
        return deleted > 0;
    }

    /**
     * 소유자와 무관하게 락을 강제로 해제한다(운영 조치용).
     *
     * @param resourceId 자원 식별자
     * @return 해제되었으면 true
     */
    public boolean forceRelease(String resourceId) {
        return jdbcTemplate.update("DELETE FROM " + tableName + " WHERE RESOURCE_ID = ?", resourceId) > 0;
    }

    /**
     * 자원이 잠겨 있는지 확인한다.
     *
     * @param resourceId 자원 식별자
     * @return 락 행이 있으면 true
     */
    public boolean isLocked(String resourceId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE RESOURCE_ID = ?", Integer.class, resourceId);
        return count != null && count > 0;
    }

    private boolean insertLock(String resourceId, String ownerId) {
        try {
            jdbcTemplate.update("INSERT INTO " + tableName + " (RESOURCE_ID, OWNER_ID, ACQUIRED_TIME) VALUES (?, ?, ?)",
                    resourceId, ownerId, now());
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    private static void requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }

}
