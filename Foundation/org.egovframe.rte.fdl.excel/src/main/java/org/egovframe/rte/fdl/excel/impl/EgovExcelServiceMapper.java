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
package org.egovframe.rte.fdl.excel.impl;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * 엑셀서비스을 처리하는 Mapper 클래스.
 *
 * <p>업로드 실행 방식은 호출 문맥에 따라 다르다.</p>
 * <ul>
 *   <li><b>Spring 트랜잭션 밖</b>: MyBatis {@link ExecutorType#BATCH} 세션을 열어 JDBC 배치로 실행하고
 *       {@code batchInsert} 호출(=커밋 단위 청크)마다 flush·commit 한다. 반환값은 JDBC 배치 결과의 영향 행 수다.
 *       MyBatis 예외는 {@link SqlSessionTemplate} 과 같은 방식으로 Spring {@link DataAccessException} 으로 변환한다.</li>
 *   <li><b>Spring 트랜잭션 안</b>: 기존과 같이 주입된 {@link SqlSessionTemplate} 으로 행별 실행해 둘러싼 트랜잭션에
 *       참여한다. 같은 트랜잭션에서 SIMPLE/BATCH executor 를 혼용할 수 없다는 MyBatis 제약 때문이다.</li>
 * </ul>
 * <p>
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.05.07	이기하				최초 생성
 * 2026.09.10	실행환경 개발팀		트랜잭션 밖 업로드를 BATCH executor 세션의 JDBC 배치 실행으로 변경, 트랜잭션 안은 행별 실행 유지
 */
public class EgovExcelServiceMapper extends EgovAbstractMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovExcelServiceMapper.class);

    private final SqlSessionTemplate sqlSessionTemplate;

    public EgovExcelServiceMapper(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        super.setSqlSessionTemplate(sqlSessionTemplate);
    }

    /**
     * 엑셀서비스의 배치업로드를 실행한다.
     */
    public Integer batchInsert(String queryId, List<Object> list) {
        return batchInsert(queryId, list, 0);
    }

    /**
     * 엑셀서비스의 배치업로드를 실행한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     * Spring 트랜잭션 밖에서는 JDBC 배치로 실행한 뒤 커밋하고, 트랜잭션 안에서는 행별로 실행해 둘러싼 트랜잭션에 참여한다.
     */
    public Integer batchInsert(final String queryId, final List<Object> list, final int start) {
        if (list == null || start >= list.size()) {
            return 0;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return rowByRowInsert(queryId, list, start);
        }
        return batchExecutorInsert(queryId, list, start);
    }

    /**
     * 트랜잭션 밖 경로 — BATCH executor 세션으로 JDBC 배치 실행 후 커밋한다.
     */
    private Integer batchExecutorInsert(String queryId, List<Object> list, int start) {
        try (SqlSession batchSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH)) {
            for (int i = start; i < list.size(); i++) {
                batchSession.insert(queryId, list.get(i));
            }
            List<BatchResult> results = batchSession.flushStatements();
            batchSession.commit();
            int affected = countAffected(results);
            LOGGER.debug("Excel batch insert executed with BATCH executor: {} rows requested, {} affected", list.size() - start, affected);
            return affected;
        } catch (PersistenceException e) {
            throw translate(e);
        }
    }

    /**
     * 트랜잭션 안 경로 — 기존과 같은 행별 실행.
     */
    private Integer rowByRowInsert(String queryId, List<Object> list, int start) {
        int count = 0;
        int size = list.size();
        for (int i = start; i < size; i++) {
            count += insert(queryId, list.get(i));
        }
        return count;
    }

    /**
     * JDBC 배치 결과에서 영향 행 수를 집계한다. 드라이버가 건수를 보고하지 않으면(SUCCESS_NO_INFO) 문장당 1행으로 센다.
     */
    private static int countAffected(List<BatchResult> results) {
        int affected = 0;
        for (BatchResult result : results) {
            for (int updateCount : result.getUpdateCounts()) {
                affected += (updateCount >= 0) ? updateCount : 1;
            }
        }
        return affected;
    }

    /**
     * SqlSessionTemplate 과 같은 방식으로 MyBatis 예외를 Spring DataAccessException 으로 변환한다. 변환할 수 없으면 그대로 돌려준다.
     */
    private RuntimeException translate(PersistenceException e) {
        PersistenceExceptionTranslator translator = sqlSessionTemplate.getPersistenceExceptionTranslator();
        DataAccessException translated = (translator != null) ? translator.translateExceptionIfPossible(e) : null;
        return (translated != null) ? translated : e;
    }

}
