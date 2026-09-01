package org.egovframe.rte.fdl.security.userdetails.util;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.egovframe.rte.fdl.security.config.EgovSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * getHashedPassword 가 만든 해시를 프레임워크가 주입하는 인코더로 검증할 수 있는지 확인한다.
 *
 * <p>hash 를 지정하지 않으면 EgovSecurityConfiguration.passwordEncoder() 는 SHA-256 인코더를 돌려준다.
 * 가입 시 이 메서드로 저장한 값은 그 인코더로 로그인할 수 있어야 한다.</p>
 */
public class EgovUserDetailsHelperTest {

    private static final String RAW_PASSWORD = "egov1234";

    @Test
    public void testGetHashedPasswordMatchesDefaultConfiguredEncoder() {
        String stored = EgovUserDetailsHelper.getHashedPassword(RAW_PASSWORD);

        PasswordEncoder configured =
                new EgovSecurityConfiguration().passwordEncoder(new EgovSecurityConfig());

        assertTrue(configured.matches(RAW_PASSWORD, stored),
                "기본 설정 인코더로 검증되어야 한다 : " + stored);
    }
}
