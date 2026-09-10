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
package org.egovframe.rte.psl.data.jpa.repository;

import org.egovframe.rte.psl.data.jpa.entity.EgovSoftDeletable;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;

/**
 * 소프트 삭제를 지원하는 공통 CRUD 리포지토리.
 *
 * <p>{@link EgovJpaRepository} 에 논리 삭제 연산을 더한다. 엔티티가 {@link EgovSoftDeletable}
 * (예: {@code EgovSoftDeletableEntity} 상속)을 구현하면 물리 삭제 대신 삭제일시 기록으로 삭제를 표현할 수 있다.
 * 활성 행 조회는 파생 쿼리({@code findByDeletedDateIsNull()} 등)로 선언한다.</p>
 *
 * <pre>
 * public interface EmployeeRepository extends EgovCrudRepository&lt;Employee, Integer&gt; {
 *     List&lt;Employee&gt; findByDeletedDateIsNull();   // 활성 행 목록
 * }
 * </pre>
 *
 * <p>여기서 던지는 {@code IllegalArgumentException} 은 리포지토리 프록시를 통해 호출될 때 Spring 예외 변환에 의해
 * {@code InvalidDataAccessApiUsageException} 으로 감싸져 전파된다.</p>
 *
 * @param <T>  엔티티 타입
 * @param <ID> 식별자 타입
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
@NoRepositoryBean
public interface EgovCrudRepository<T, ID> extends EgovJpaRepository<T, ID> {

    /**
     * 엔티티를 논리 삭제한다(삭제일시 기록 후 저장).
     *
     * @param entity 삭제할 엔티티
     * @return 저장된 엔티티
     * @throws IllegalArgumentException 엔티티가 {@link EgovSoftDeletable} 이 아닌 경우
     */
    default <S extends T> S softDelete(S entity) {
        requireSoftDeletable(entity).setDeletedDate(LocalDateTime.now());
        return save(entity);
    }

    /**
     * 식별자로 조회해 논리 삭제한다.
     *
     * @param id 식별자
     * @return 저장된 엔티티
     * @throws IllegalArgumentException 대상이 없거나 {@link EgovSoftDeletable} 이 아닌 경우
     */
    default T softDeleteById(ID id) {
        T entity = findById(id).orElseThrow(
                () -> new IllegalArgumentException("Entity to soft-delete not found: id=" + id));
        return softDelete(entity);
    }

    /**
     * 논리 삭제된 엔티티를 복구한다(삭제일시 제거 후 저장).
     *
     * @param entity 복구할 엔티티
     * @return 저장된 엔티티
     * @throws IllegalArgumentException 엔티티가 {@link EgovSoftDeletable} 이 아닌 경우
     */
    default <S extends T> S restore(S entity) {
        requireSoftDeletable(entity).setDeletedDate(null);
        return save(entity);
    }

    private static EgovSoftDeletable requireSoftDeletable(Object entity) {
        if (entity instanceof EgovSoftDeletable deletable) {
            return deletable;
        }
        throw new IllegalArgumentException("Entity does not implement EgovSoftDeletable: "
                + (entity == null ? "null" : entity.getClass().getName()));
    }

}
