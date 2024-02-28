/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2DBC Repository 구현 클래스
 *
 * <p>Desc.: R2DBC Repository 구현 클래스</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
public class EgovR2dbcRepository<T> extends R2dbcEntityTemplate {

    public EgovR2dbcRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
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
