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
package org.egovframe.boot.security.userdetails.jdbc;

import org.egovframe.boot.security.userdetails.EgovUserDetails;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 사용자 조회 결과를 EgovUserDetails 로 매핑하는 추상 클래스.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
public abstract class EgovUsersByUsernameMapping extends MappingSqlQuery<EgovUserDetails> {

    /** 사용자정보를 테이블에서 조회하여 사용자객체에 매핑한다. */
    public EgovUsersByUsernameMapping(DataSource ds, String usersByUsernameQuery) {
        super(ds, usersByUsernameQuery);
        declareParameter(new SqlParameter(Types.VARCHAR));
        compile();
    }

    @Override
    protected abstract EgovUserDetails mapRow(ResultSet rs, int rownum) throws SQLException;

}
