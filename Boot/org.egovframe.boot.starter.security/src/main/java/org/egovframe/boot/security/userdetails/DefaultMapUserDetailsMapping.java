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
package org.egovframe.boot.security.userdetails;

import org.egovframe.boot.security.userdetails.jdbc.EgovUsersByUsernameMapping;
import org.egovframe.boot.security.userdetails.util.CamelCaseUtil;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 사용자 조회 결과의 모든 컬럼을 camelCase Map 으로 매핑하는 기본 매핑 클래스.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  컬럼명 소문자 변환에 Locale.ROOT 사용
 * </pre>
 * @since 2025.06.01
 */
public class DefaultMapUserDetailsMapping extends EgovUsersByUsernameMapping {

    public DefaultMapUserDetailsMapping(DataSource ds, String usersByUsernameQuery) {
        super(ds, usersByUsernameQuery);
    }

    @Override
    protected EgovUserDetails mapRow(ResultSet rs, int rownum) throws SQLException {
        String userid = rs.getString("user_id");
        String password = rs.getString("password");
        boolean enabled = rs.getBoolean("enabled");

        Map<String, String> map = new HashMap<String, String>();
        ResultSetMetaData md = rs.getMetaData();
        int cnt = md.getColumnCount();
        for (int i = 1; i <= cnt; i++) {
            // 기본 Locale(tr 등)에 영향받지 않도록 Locale.ROOT
            String column = md.getColumnName(i).toLowerCase(Locale.ROOT);
            String value = rs.getString(column);
            map.put(CamelCaseUtil.convert2CamelCase(column), value);
        }

        return new EgovUserDetails(userid, password, enabled, map);
    }

}
