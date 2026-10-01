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
package org.egovframe.rte.psl.reactive.r2dbc.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

/**
 * R2DBC 감사 필드 공통 상위 엔티티 — 등록자/등록일시/수정자/수정일시 자동 기록.
 *
 * <p>JPA 의 {@code org.egovframe.rte.psl.data.jpa.entity.EgovBaseEntity} 와 <b>같은 규약</b>이다 — 컬럼명 {@value #COLUMN_CREATED_BY}·
 * {@value #COLUMN_CREATED_DATE}·{@value #COLUMN_MODIFIED_BY}·{@value #COLUMN_MODIFIED_DATE}, 시각 타입 {@link LocalDateTime}. 두 스택을 함께 쓰는
 * 서비스가 테이블·조회 규약을 하나로 유지하게 하는 것이 목적이다.</p>
 *
 * <p>값은 Spring Data R2DBC 감사가 저장/수정 시점에 채운다. 활성화 조건:</p>
 * <ul>
 *   <li>구성 클래스에 {@code @EnableR2dbcAuditing} 선언</li>
 *   <li>등록자/수정자를 쓰려면 {@code ReactiveAuditorAware<String>} 빈 제공 — 실행환경 기본 구현은
 *       {@code org.egovframe.rte.psl.reactive.r2dbc.audit.EgovContextReactiveAuditorAware}(Reactor Context {@code userId}). 없으면 일시만 기록된다</li>
 *   <li>저장은 컨텍스트가 관리하는 {@code R2dbcEntityTemplate}(또는 그것을 상속한 {@code EgovR2dbcRepository} 빈)로 해야 엔티티 콜백이 적용된다</li>
 * </ul>
 *
 * <p>세터를 두지 않는다 — 감사 값은 감사 콜백이 채우며 업무 코드가 임의 조작할 수 없다. 이 클래스는 감사 활성화를 강제하지 않는다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.30	실행환경 개발팀		최초 생성(JPA EgovBaseEntity 와 컬럼 규약 통일)
 * </pre>
 */
public abstract class EgovReactiveBaseEntity {

    /** 등록자 컬럼명(JPA EgovBaseEntity 와 동일) */
    public static final String COLUMN_CREATED_BY = "created_by";
    /** 등록일시 컬럼명 */
    public static final String COLUMN_CREATED_DATE = "created_date";
    /** 수정자 컬럼명 */
    public static final String COLUMN_MODIFIED_BY = "modified_by";
    /** 수정일시 컬럼명 */
    public static final String COLUMN_MODIFIED_DATE = "modified_date";

    /** 등록자 */
    @CreatedBy
    @Column(COLUMN_CREATED_BY)
    private String createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(COLUMN_CREATED_DATE)
    private LocalDateTime createdDate;

    /** 수정자 */
    @LastModifiedBy
    @Column(COLUMN_MODIFIED_BY)
    private String modifiedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(COLUMN_MODIFIED_DATE)
    private LocalDateTime modifiedDate;

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }
}
