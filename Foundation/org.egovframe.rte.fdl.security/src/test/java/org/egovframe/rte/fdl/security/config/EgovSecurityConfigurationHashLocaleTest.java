package org.egovframe.rte.fdl.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
}
