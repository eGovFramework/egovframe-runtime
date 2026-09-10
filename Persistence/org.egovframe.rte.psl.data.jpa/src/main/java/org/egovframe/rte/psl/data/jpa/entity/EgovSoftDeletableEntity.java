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
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

/**
 * 감사 필드 + 소프트 삭제 공통 상위 엔티티.
 *
 * <p>{@link EgovBaseEntity} 의 감사 필드에 삭제일시({@code deleted_date}) 컬럼을 더한 형태다.
 * 활성 행 조회는 리포지토리 파생 쿼리({@code findByDeletedDateIsNull()} 등)로 선언한다.</p>
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
public abstract class EgovSoftDeletableEntity extends EgovBaseEntity implements EgovSoftDeletable {

    /** 삭제일시(null 이면 활성) */
    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @Override
    public LocalDateTime getDeletedDate() {
        return deletedDate;
    }

    @Override
    public void setDeletedDate(LocalDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }

}
