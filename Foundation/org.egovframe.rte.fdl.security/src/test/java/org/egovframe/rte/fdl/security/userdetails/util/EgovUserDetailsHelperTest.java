package org.egovframe.rte.fdl.security.userdetails.util;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.egovframe.rte.fdl.security.config.EgovSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * getHashedPassword 가 만든 해시를 프레임워크가 주입하는 인코더로 검증할 수 있는지 확인한다.
 *
 * <p>hash 미지정 시 passwordEncoder() 는 BCrypt (가입 시 저장한 값으로 로그인 가능해야 함)</p>
 */
public class EgovUserDetailsHelperTest {

    private static final String RAW_PASSWORD = "egov1234";

    @Test
    public void testGetHashedPasswordMatchesDefaultConfiguredEncoder() {
        String stored = EgovUserDetailsHelper.getHashedPassword(RAW_PASSWORD);

        PasswordEncoder configured =
                new EgovSecurityConfiguration().passwordEncoder(new EgovSecurityConfig());

        assertInstanceOf(BCryptPasswordEncoder.class, configured, "기본 설정 인코더는 BCrypt");
        assertTrue(stored.startsWith("$2"), "BCrypt 형식 해시여야 한다 : " + stored);
        assertTrue(configured.matches(RAW_PASSWORD, stored),
                "기본 설정 인코더로 검증되어야 한다 : " + stored);
        assertFalse(configured.matches("wrong", stored));
    }
}
