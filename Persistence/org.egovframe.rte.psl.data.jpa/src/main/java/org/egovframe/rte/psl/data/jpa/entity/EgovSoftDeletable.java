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

import java.time.LocalDateTime;

/**
 * 소프트 삭제(논리 삭제) 지원 엔티티 계약.
 *
 * <p>삭제일시가 null 이면 활성, 값이 있으면 삭제된 행으로 본다.
 * {@code EgovCrudRepository.softDelete*}·{@code restore} 가 이 계약으로 동작한다.</p>
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
public interface EgovSoftDeletable {

    /**
     * 삭제일시(null 이면 활성)를 돌려준다.
     *
     * @return 삭제일시
     */
    LocalDateTime getDeletedDate();

    /**
     * 삭제일시를 설정한다(null 로 설정하면 복구).
     *
     * @param deletedDate 삭제일시
     */
    void setDeletedDate(LocalDateTime deletedDate);

    /**
     * 삭제 여부를 돌려준다.
     *
     * @return 삭제일시가 있으면 true
     */
    default boolean isDeleted() {
        return getDeletedDate() != null;
    }

}
