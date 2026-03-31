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
package org.egovframe.rte.psl.reactive.mongodb.repository;

import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * MongoDB Reactive Repository 구현 클래스
 *
 * <p>Desc.: MongoDB Reactive Repository 구현 클래스</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * </pre>
 * @since 2023.08.31
 */
public class EgovReactiveMongoDbRepository<T> extends ReactiveMongoTemplate {

    public EgovReactiveMongoDbRepository(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        super(reactiveMongoDatabaseFactory);
    }

    public Flux<T> selectAllData(Query query, Class<T> entityClass) {
        return find(query, entityClass);
    }

    public Mono<T> selectOneData(Query query, Class<T> entityClass) {
        return findOne(query, entityClass);
    }

    public Mono<Long> countData(Query query, Class<T> entityClass) {
        return count(query, entityClass);
    }

    public Mono<T> insertData(T objectToSave) {
        return insert(objectToSave);
    }

    public Mono<T> updateData(Query query, Update update, Class<T> entityClass) {
        return findAndModify(query, update, entityClass);
    }

    public Mono<T> deleteData(Query query, Class<T> entityClass) {
        return findAndRemove(query, entityClass);
    }

}
