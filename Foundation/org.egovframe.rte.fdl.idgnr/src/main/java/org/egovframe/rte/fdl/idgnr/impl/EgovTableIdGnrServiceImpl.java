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
package org.egovframe.rte.fdl.idgnr.impl;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * ID Generation 서비스를 위한 Table 구현 클래스
 *
 * <p><b>NOTE</b>: 채번 테이블을 정의하고, 각 관리대상에 대한 현재 최종 Max 번호를
 * 관리하여 Table 기반의 유일키를 제공 받을 수 있다.</p>
 *
 * <pre>
 *   필요한 테이블 생성 스크립트
 *   CREATE TABLE ids (
 *       table_name varchar(16) NOT NULL,
 *       next_id INTEGER NOT NULL,
 *       PRIMARY KEY (table_name)
 *   );
 * </pre>
 *
 * <p><b>주의:</b> 블록 채번의 SELECT에 {@code FOR UPDATE} 행 잠금을 사용해 다중 WAS 인스턴스가 같은
 * DB를 공유하는 배포에서도 next_id 중복 할당을 방지한다. Oracle/Tibero/PostgreSQL/MySQL(InnoDB)/
 * HSQLDB/H2 등 주요 DBMS는 이 구문을 지원하나, SQL Server(T-SQL)는 표준 {@code FOR UPDATE} 구문을
 * 지원하지 않으므로 SQL Server 사용 시 별도 잠금 힌트(예: {@code WITH (UPDLOCK, ROWLOCK)}) 적용이 필요하다.</p>
 *
 * @author 실행환경 개발팀 김태호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.02.01	김태호				최초 생성
 * 2013.03.25	한성곤				필드명 속성 처리, JdbcTemplate 방식으로 변경, 초기 id 값 등록(자동 insert 처리), 반복처리 제외
 * 2013.09.04	한성곤				TransactionTemplate을 통해 transaction 처리 분리
 * 2014.08.18	한성곤				명명규칙 클래스 명 변경
 * 2017.02.28	장동한				시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
 * 2026.09.10	실행환경 개발팀		초기 행 동시 INSERT 경합 시 새 트랜잭션에서 재시도, deprecated queryForObject 시그니처 교체
 * </pre>
 * @since 2009.02.01
 */
public class EgovTableIdGnrServiceImpl extends AbstractDataBlockIdGnrService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovTableIdGnrServiceImpl.class);

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * 초기 행 INSERT 가 중복 키로 실패한 경합 신호. 현재 트랜잭션을 되돌리고 새 트랜잭션에서 한 번 더 시도하게 한다.
     */
    private static final class InitialRowRaceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private InitialRowRaceException(DataIntegrityViolationException cause) {
            super(cause);
        }
    }

    /**
     * ID생성을 위한 테이블 정보 디폴트는 ids임.
     */
    private String table = "ids";

    /**
     * 테이블 정보에 기록되는 대상 키정보 대개의 경우는 아이디로 생성되는 테이블명을 기재함
     */
    private String tableName = "id";

    /**
     * 테이블명(구분값)에 대한 테이블 필드명 지정
     */
    private String tableNameFieldName = "table_name";

    /**
     * Next Id 정보를 보관하는 필드명 지정
     */
    private String nextIdFieldName = "next_id";

    /**
     * Jdbc template
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * TransactionTemplate
     */
    private TransactionTemplate transactionTemplate;

    /**
     * 생성자
     */
    public EgovTableIdGnrServiceImpl() {
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        this.transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
    }

    /**
     * tableName에 대한 초기 값이 없는 경우 초기 id 값 등록 (blockSize 처리)
     *
     * @param useBigDecimals
     * @param blockSize
     */
    private Object insertInitId(final boolean useBigDecimals, final int blockSize) {
        LOGGER.debug(messageSource.getMessage("debug.idgnr.init.idblock", new Object[]{tableName}, Locale.getDefault()));
        Object initId = null;
        String insertQuery = "INSERT INTO " + table + "(" + tableNameFieldName + ", " + nextIdFieldName + ") " + "values('" + tableName + "', ?)";
        LOGGER.debug("Insert Query : {}", insertQuery);
        if (useBigDecimals) {
            initId = new BigDecimal(blockSize);
        } else {
            initId = Long.valueOf(blockSize);
        }
        jdbcTemplate.update(insertQuery, initId);
        return initId;
    }

    /**
     * blockSize 대로 ID 지정
     *
     * @param blockSize      지정되는 blockSize
     * @param useBigDecimals BigDecimal 사용 여부
     * @return BigDecimal을 사용하면 BigDecimal 아니면 long 리턴
     * @throws FdlException ID생성을 위한 블럭 할당이 불가능할때
     */
    private Object allocateIdBlock(final int blockSize, final boolean useBigDecimals) throws FdlException {
        return allocateIdBlock(blockSize, useBigDecimals, true);
    }

    /**
     * blockSize 대로 ID 지정. 초기 행 INSERT 가 다른 인스턴스와 경합해 실패하면 새 트랜잭션에서 한 번 더 시도한다.
     *
     * @param blockSize      지정되는 blockSize
     * @param useBigDecimals BigDecimal 사용 여부
     * @param retryOnRace    초기 행 경합 시 재시도 여부(재시도 호출에서는 false)
     * @return BigDecimal을 사용하면 BigDecimal 아니면 long 리턴
     * @throws FdlException ID생성을 위한 블럭 할당이 불가능할때
     */
    private Object allocateIdBlock(final int blockSize, final boolean useBigDecimals, final boolean retryOnRace) throws FdlException {
        LOGGER.debug(messageSource.getMessage("debug.idgnr.allocate.idblock", new Object[]{Integer.valueOf(blockSize), tableName}, Locale.getDefault()));
        try {
            return transactionTemplate.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus status) {
                    Object nextId;
                    Object newNextId;
                    try {
                        // mSemaphore(AbstractIdGnrService)는 같은 JVM 내 동시성만 막는다. 여러 WAS
                        // 인스턴스가 같은 DB를 공유하는 배포에서는 서로 다른 JVM의 트랜잭션이 이 SELECT를
                        // 동시에 실행해 같은 next_id를 읽어갈 수 있으므로, FOR UPDATE로 행 잠금을 걸어
                        // 다른 트랜잭션이 UPDATE 커밋 전까지 대기하도록 한다(ID 블록 중복 할당 방지).
                        String selectQuery = "SELECT " + nextIdFieldName + " FROM " + table + " WHERE " + tableNameFieldName + " = ? FOR UPDATE";
                        LOGGER.debug("Select Query : {}", selectQuery);
                        if (useBigDecimals) {
                            try {
                                nextId = jdbcTemplate.queryForObject(selectQuery, BigDecimal.class, tableName);
                            } catch (EmptyResultDataAccessException erdae) {
                                nextId = null;
                            }

                            if (nextId == null) { // no row
                                insertInitIdOrSignalRace(status, useBigDecimals, blockSize);
                                return new BigDecimal(0);
                            }
                        } else {
                            try {
                                nextId = jdbcTemplate.queryForObject(selectQuery, Long.class, tableName);
                            } catch (EmptyResultDataAccessException erdae) {
                                nextId = -1L;
                            }

                            if ((Long) nextId == -1L) { // no row
                                insertInitIdOrSignalRace(status, useBigDecimals, blockSize);
                                return Long.valueOf(0);
                            }
                        }
                    } catch (DataAccessException dae) {
                        //2017.02.28 장동한 시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
                        status.setRollbackOnly();
                        throw new RuntimeException(new FdlException(messageSource, "error.idgnr.select.idblock", new String[]{tableName}, null));
                    }

                    try {
                        String updateQuery = "UPDATE " + table + " SET " + nextIdFieldName + " = ?" + " WHERE " + tableNameFieldName + " = ?";
                        LOGGER.debug("Update Query : {}", updateQuery);

                        if (useBigDecimals) {
                            newNextId = ((BigDecimal) nextId).add(new BigDecimal(blockSize));
                        } else {
                            newNextId = ((Long) nextId).longValue() + blockSize;
                        }

                        jdbcTemplate.update(updateQuery, newNextId, tableName);
                        return nextId;
                    } catch (DataAccessException dae) {
                        status.setRollbackOnly();
                        throw new RuntimeException(new FdlException(messageSource, "error.idgnr.update.idblock", new String[]{tableName}, null));
                    }
                }
            });
        } catch (InitialRowRaceException race) {
            if (!retryOnRace) {
                throw new FdlException(messageSource, "error.idgnr.select.idblock", new String[]{tableName}, race.getCause());
            }
            // 같은 DB를 쓰는 다른 인스턴스가 같은 순간 초기 행을 만들었다. 실패한 트랜잭션은 되돌아갔으므로
            // 새 트랜잭션에서 이제 존재하는 행을 잠그고(FOR UPDATE) 정상 경로로 다음 블록을 받는다.
            LOGGER.debug("Initial row for [{}] was created concurrently - retrying in a new transaction", tableName);
            return allocateIdBlock(blockSize, useBigDecimals, false);
        } catch (RuntimeException re) {
            if (re.getCause() instanceof FdlException) {
                throw (FdlException) re.getCause();
            } else {
                throw re;
            }
        }
    }

    /**
     * 초기 행을 INSERT 한다. 중복 키 등 무결성 제약으로 실패하면 다른 인스턴스가 먼저 만든 경합이므로
     * 트랜잭션을 되돌리고 {@link InitialRowRaceException} 으로 재시도를 요청한다.
     */
    private void insertInitIdOrSignalRace(TransactionStatus status, boolean useBigDecimals, int blockSize) {
        try {
            insertInitId(useBigDecimals, blockSize);
        } catch (DataIntegrityViolationException dive) {
            status.setRollbackOnly();
            throw new InitialRowRaceException(dive);
        }
    }

    /**
     * blockSize 대로 ID 지정(BigDecimal)
     *
     * @param blockSize 지정되는 blockSize
     * @return 할당된 블럭의 첫번째 아이디
     * @throws FdlException ID생성을 위한 블럭 할당이 불가능할때
     */
    protected BigDecimal allocateBigDecimalIdBlock(int blockSize) throws FdlException {
        return (BigDecimal) allocateIdBlock(blockSize, true);
    }

    /**
     * blockSize 대로 ID 지정(long)
     *
     * @param blockSize 지정되는 blockSize
     * @return 할당된 블럭의 첫번째 아이디
     * @throws FdlException ID생성을 위한 블럭 할당이 불가능할때
     */
    protected long allocateLongIdBlock(int blockSize) throws FdlException {
        Long id = (Long) allocateIdBlock(blockSize, false);
        return id.longValue();
    }

    /**
     * SQL 식별자(테이블명/컬럼명) 유효성 검사 - SQL 삽입 방지
     *
     * @param name   식별자 값
     * @param fieldName setter/필드 이름 (에러 메시지용)
     * @throws IllegalArgumentException 영문, 숫자, 언더스코어 외 문자가 포함된 경우
     */
    private static void validateIdentifier(String name, String fieldName) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be null or empty");
        }
        if (!SAFE_IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException(
                fieldName + " contains invalid characters; only [a-zA-Z0-9_] allowed for SQL identifier");
        }
    }

    /**
     * ID생성을 위한 테이블 정보 Injection
     *
     * @param table config로 지정되는 정보
     */
    public void setTable(String table) {
        validateIdentifier(table, "table");
        this.table = table;
    }

    /**
     * ID 생성을 위한 테이블의 키정보 ( 대개의경우는 대상 테이블명을 기재함 )
     *
     * @param tableName config로 지정되는 정보
     */
    public void setTableName(String tableName) {
        validateIdentifier(tableName, "tableName");
        this.tableName = tableName;
    }

    /**
     * 테이블명(구분값)에 대한 테이블 필드명 정보 지정
     *
     * @param tableNameFieldName
     */
    public void setTableNameFieldName(String tableNameFieldName) {
        validateIdentifier(tableNameFieldName, "tableNameFieldName");
        this.tableNameFieldName = tableNameFieldName;
    }

    /**
     * Next Id 정보를 보관하는 필드명 정보 지정
     *
     * @param nextIdFieldName
     */
    public void setNextIdFieldName(String nextIdFieldName) {
        validateIdentifier(nextIdFieldName, "nextIdFieldName");
        this.nextIdFieldName = nextIdFieldName;
    }

}
