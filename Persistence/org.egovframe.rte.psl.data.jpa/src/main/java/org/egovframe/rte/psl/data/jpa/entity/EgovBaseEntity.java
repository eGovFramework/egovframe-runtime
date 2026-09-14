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
package org.egovframe.rte.psl.data.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 감사 필드 공통 상위 엔티티 — 등록자·등록일시·수정자·수정일시를 자동으로 기록한다.
 *
 * <p>Spring Data JPA Auditing 이 저장·수정 시점에 값을 채운다. 활성화 조건:</p>
 * <ul>
 *   <li>구성 클래스에 {@code @EnableJpaAuditing} 선언. 이 기능은 {@code spring-aspects} 가 클래스패스에 있어야 하므로
 *       애플리케이션 의존에 추가한다(이 모듈은 optional 로만 선언한다)</li>
 *   <li>등록자·수정자를 쓰려면 {@code AuditorAware<String>} 빈 제공(예: 인증 사용자 ID 반환). 없으면 일시만 기록된다</li>
 * </ul>
 *
 * <p>setter 를 두지 않는다. 감사 값은 {@link AuditingEntityListener} 가 채우며 업무 코드가 임의로 바꿀 수 없다.
 * 컬럼명은 사이트 표준에 맞게 하위 엔티티에서 {@code @AttributeOverride} 로 재정의할 수 있다.</p>
 *
 * <pre>
 * &#64;Entity
 * public class Employee extends EgovBaseEntity {
 *     &#64;Id &#64;GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private Integer id;
 *     ...
 * }
 * </pre>
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
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EgovBaseEntity {

    /** 등록자 */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    /** 등록일시 */
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    /** 수정자 */
    @LastModifiedBy
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    /** 수정일시 */
    @LastModifiedDate
    @Column(name = "modified_date")
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
