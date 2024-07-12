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
package org.egovframe.rte.psl.reactive.redis.repository;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Redis Repository 구현 클래스
 *
 * <p>Desc.: Redis Repository 구현 클래스</p>
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
public class EgovRedisRepository<T> extends ReactiveRedisTemplate {

    public EgovRedisRepository(ReactiveRedisConnectionFactory connectionFactory, RedisSerializationContext serializationContext) {
        super(connectionFactory, serializationContext);
    }

    public Flux<T> selectData(String redisKey) {
        return opsForList().range(redisKey, 0, -1);
    }

    public Mono<Long> findIndex(String redisKey, T entity) {
        return opsForList().indexOf(redisKey, entity);
    }

    public Mono<Long> countData(String redisKey) {
        return opsForList().size(redisKey);
    }

    public Mono<T> insertData(String redisKey, T entity) {
        return opsForList().leftPush(redisKey, entity);
    }

    public Mono<T> updateData(String redisKey, long idx, T entity) {
        return opsForList().set(redisKey, idx, entity);
    }

    public Mono<Boolean> deleteAllData(String redisKey) {
        return opsForList().delete(redisKey);
    }

    public Mono<Boolean> deleteData(String redisKey, T entity) {
        return opsForList().remove(redisKey, 0, entity);
    }

}
