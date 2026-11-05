package org.egovframe.rte.fdl.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * hash 설정값을 switch 키로 정규화할 때 기본 로케일에 휘둘리지 않는지 검증한다.
 *
 * <p>터키어·아제르바이잔어 로케일에서 "PLAINTEXT".toLowerCase()는 점 없는 ı가 섞인
 * "plaıntext"가 되어 case "plaintext"에 걸리지 않는다. 같은 파일의 xframeOptions
 * 분기(:488)는 이미 Locale.ROOT로 정규화한다.</p>
 */
public class EgovSecurityConfigurationHashLocaleTest {

    private PasswordEncoder encoderFor(String hash) {
        EgovSecurityConfig config = new EgovSecurityConfig();
        config.setHash(hash);
        return new EgovSecurityConfiguration().passwordEncoder(config);
    }

    @Test
    @DisplayName("hash=PLAINTEXT는 로케일과 무관하게 NoOpPasswordEncoder로 해석된다")
    public void plaintextHashResolvesUnderTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertInstanceOf(NoOpPasswordEncoder.class, encoderFor("PLAINTEXT"),
                    "터키어 로케일에서도 PLAINTEXT는 NoOp으로 해석되어야 한다");

            Locale.setDefault(Locale.ENGLISH);
            assertInstanceOf(NoOpPasswordEncoder.class, encoderFor("PLAINTEXT"),
                    "영어 로케일에서도 동일해야 한다");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    @DisplayName("hash 미지정(null/빈 값/공백)이면 기본 인코더는 BCrypt 이다")
    public void defaultHashResolvesToBCrypt() {
        assertInstanceOf(BCryptPasswordEncoder.class, encoderFor(null), "hash=null 이면 BCrypt");
        assertInstanceOf(BCryptPasswordEncoder.class, encoderFor(""), "hash='' 이면 BCrypt");
        assertInstanceOf(BCryptPasswordEncoder.class, encoderFor("   "), "hash=공백 이면 BCrypt");

        PasswordEncoder encoder = encoderFor(null);
        String encoded = encoder.encode("egov1234");
        assertTrue(encoded.startsWith("$2"), "BCrypt 형식 해시여야 한다 : " + encoded);
        assertTrue(encoder.matches("egov1234", encoded));
    }

    @Test
    @DisplayName("hash=BCRYPT 는 로케일과 무관하게 BCrypt, 명시한 SHA-256 은 그대로 유지된다")
    public void explicitHashValuesUnderTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertInstanceOf(BCryptPasswordEncoder.class, encoderFor(" BCRYPT "), "BCRYPT(대문자·공백) → BCrypt");
            assertInstanceOf(MessageDigestPasswordEncoder.class, encoderFor("SHA-256"), "명시한 SHA-256 은 유지");
        } finally {
            Locale.setDefault(original);
        }
    }
}
