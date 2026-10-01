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
package org.egovframe.rte.psl.reactive.r2dbc.repository;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2DBC Repository 구현 클래스
 *
 * <p>Desc.: R2DBC Repository 구현 클래스</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * 2026.09.30   실행환경 개발팀      Spring Boot 매핑 부품(DatabaseClient·R2dbcDialect·R2dbcConverter)으로 조립하는 생성자 추가
 * </pre>
 * @since 2023.08.31
 */
public class EgovR2dbcRepository<T> extends R2dbcEntityTemplate {

    public EgovR2dbcRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    /**
     * 이미 구성된 데이터베이스 클라이언트·방언·변환기로 조립한다. Spring Boot 가 만든 부품을 넘기면 앱이 등록한
     * 커스텀 변환기와 명명 전략이 이 템플릿의 조회·저장에도 적용된다.
     *
     * @param databaseClient 데이터베이스 클라이언트
     * @param dialect R2DBC 방언
     * @param converter 매핑 변환기(커스텀 변환 포함)
     * @since 5.1
     */
    public EgovR2dbcRepository(DatabaseClient databaseClient, R2dbcDialect dialect, R2dbcConverter converter) {
        super(databaseClient, dialect, converter);
    }

    public Flux<T> selectAllData(Query query, Class<T> entityClass) {
        return select(query, entityClass);
    }

    public Mono<T> selectOneData(Query query, Class<T> entityClass) {
        return selectOne(query, entityClass);
    }

    public Mono<Long> countData(Query query, Class<T> entityClass) {
        return count(query, entityClass);
    }

    public Mono<T> insertData(T entity) {
        return insert(entity);
    }

    public Mono<T> updateData(T entity) {
        return update(entity);
    }

    public Mono<T> deleteData(T entity) {
        return delete(entity);
    }

}
