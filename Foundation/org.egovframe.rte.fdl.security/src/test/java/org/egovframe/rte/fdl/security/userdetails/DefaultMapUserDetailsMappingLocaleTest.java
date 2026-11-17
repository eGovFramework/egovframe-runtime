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
 * <p>터키어 로케일 : "USER_ID".toLowerCase() → "user_ıd"('ı' U+0131) 로 컬럼 조회 실패·속성 유실(CWE-176).
 * Locale.ROOT 고정으로 로케일 무관 동일 매핑</p>
 */
class DefaultMapUserDetailsMappingLocaleTest {

    private static final String QUERY =
            "select user_id, password, enabled from users where user_id = ?";

    @Test
    void mapRow_lowercasesColumnNamesLocaleIndependently() throws Exception {
        Locale original = Locale.getDefault();
        try {
            // 터키어 로케일 재현 : 'I' -> 'ı'(dotless i) 로 소문자화 깨짐
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("honggildong", mapUserIdUnderCurrentLocale(),
                    "터키어 로케일에서도 컬럼값(user_id)이 유실 없이 매핑되어야 한다");

            // 대조군 : 영어 로케일도 동일 결과 (로케일 독립성)
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

        // 실제 DB(Oracle/HSQLDB 등)는 메타데이터 컬럼명을 대문자로 반환
        EasyMock.expect(md.getColumnCount()).andReturn(1).anyTimes();
        EasyMock.expect(md.getColumnName(1)).andReturn("USER_ID").anyTimes();
        // ASCII 소문자 컬럼명으로만 값 조회
        // (버그 시 "user_ıd" 로 조회 → nice mock 은 null 반환)
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
