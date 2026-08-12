package org.egovframe.rte.fdl.security.userdetails;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DefaultMapUserDetailsMapping 의 컬럼명 소문자화가 JVM 기본 로케일과 무관함을 검증한다.
 *
 * <p>터키어 로케일에서는 "USER_ID".toLowerCase() 가 점 없는 'ı'(U+0131) 때문에 "user_ıd" 가 되어,
 * 그 이름으로 ResultSet 을 조회하면 실제 컬럼(user_id)을 찾지 못하고 사용자 속성이 유실된다(CWE-176).
 * Locale.ROOT 고정으로 어떤 기본 로케일에서도 동일하게 매핑되어야 한다.</p>
 */
class DefaultMapUserDetailsMappingLocaleTest {

    private static final String QUERY =
            "select user_id, password, enabled from users where user_id = ?";

    @Test
    void mapRow_lowercasesColumnNamesLocaleIndependently() throws Exception {
        Locale original = Locale.getDefault();
        try {
            // 터키어 로케일에서 재현: 'I' -> 'ı'(dotless i) 로 매핑되어 컬럼명 소문자화가 깨진다.
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("honggildong", mapUserIdUnderCurrentLocale(),
                    "터키어 로케일에서도 컬럼값(user_id)이 유실 없이 매핑되어야 한다");

            // 대조군: 영어 로케일에서도 동일 결과여야 한다(로케일 독립성).
            Locale.setDefault(Locale.ENGLISH);
            assertEquals("honggildong", mapUserIdUnderCurrentLocale(),
                    "영어 로케일에서도 동일하게 매핑되어야 한다");
        } finally {
            Locale.setDefault(original);
        }
    }

    /**
     * USER_ID 컬럼 하나를 보고하는 ResultSet 을 mapRow 로 매핑한 뒤,
     * 카멜케이스 키(userId)에 담긴 값을 반환한다.
     */
    private String mapUserIdUnderCurrentLocale() throws Exception {
        DataSource ds = EasyMock.createNiceMock(DataSource.class);
        ResultSet rs = EasyMock.createNiceMock(ResultSet.class);
        ResultSetMetaData md = EasyMock.createNiceMock(ResultSetMetaData.class);

        // 실제 DB(예: Oracle/HSQLDB)는 메타데이터 컬럼명을 대문자로 돌려준다.
        EasyMock.expect(md.getColumnCount()).andReturn(1).anyTimes();
        EasyMock.expect(md.getColumnName(1)).andReturn("USER_ID").anyTimes();
        // ASCII 소문자 컬럼명으로만 값이 조회되도록 한다.
        // (버그가 있으면 코드는 "user_ıd" 로 조회하고, nice mock 은 null 을 돌려준다.)
        EasyMock.expect(rs.getString("user_id")).andReturn("honggildong").anyTimes();
        EasyMock.expect(rs.getString("password")).andReturn("secret").anyTimes();
        EasyMock.expect(rs.getBoolean("enabled")).andReturn(true).anyTimes();
        EasyMock.expect(rs.getMetaData()).andReturn(md).anyTimes();
        EasyMock.replay(ds, rs, md);

        DefaultMapUserDetailsMapping mapping = new DefaultMapUserDetailsMapping(ds, QUERY);
        EgovUserDetails details = mapping.mapRow(rs, 1);

        @SuppressWarnings("unchecked")
        Map<String, String> attributes = (Map<String, String>) details.getEgovUserVO();
        return attributes.get("userId");
    }
}
