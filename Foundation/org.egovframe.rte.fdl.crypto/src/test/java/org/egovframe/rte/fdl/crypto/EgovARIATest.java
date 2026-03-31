package org.egovframe.rte.fdl.crypto;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.crypto.config.EgovCryptoTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EgovCryptoTestConfig.class)
public class EgovARIATest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIATest.class);

    @Resource(name = "egov.envCryptoService")
    private EgovEnvCryptoService cryptoService;

    /**
     * properties 설정 파일의 algorithmKey 값을 주입받는 password
     */
    @Resource(name = "password")
    private String password;

    private static String createRndString(int length) {
        StringBuilder result = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            int rIndex = rnd.nextInt(4);
            switch (rIndex) {
                case 0:
                    // a-z
                    result.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    // A-Z
                    result.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    // 0-9
                    result.append((rnd.nextInt(10)));
                    break;
                case 3:
                    // 한글
                    result.append((char) ((Math.random() * 11172) + 0xAC00));
                    break;
            }
        }
        return result.toString();
    }

    @Test
    public void egovARIAVerify() {
        String label = "";
        String result = "통과 !!!";

        try {
            String orgStr = "";
            for (int i = 1; i < 1000; i++) {
                orgStr = createRndString(i);
                LOGGER.debug("### [" + i + "] ########## ");
                LOGGER.debug(label + " 원본(orignal):" + orgStr);
                LOGGER.debug(label + " 인코딩(encrypted):" + cryptoService.encrypt(orgStr));
                LOGGER.debug(label + " 디코딩(decrypted):" + cryptoService.decrypt(cryptoService.encrypt(orgStr)));
                if (cryptoService.decrypt(cryptoService.encrypt(orgStr)).equals(orgStr)) {
                    LOGGER.debug(label + " 통과 !!!");
                } else {
                    result = "실패 !!!";
                    LOGGER.debug(label + " 실패 !!!");
                }

                LOGGER.debug("------------------------------------------------------");
            }
        } catch (IllegalArgumentException e) {
            LOGGER.debug("[{}] EgovARIATest egovARIAVerify() : {}", e.getClass().getName(), e.getMessage());
        }

        LOGGER.debug("------------------------------------------------------");
        LOGGER.debug("최종결과 > " + result);
        LOGGER.debug("------------------------------------------------------");
    }

    @Test
    public void EgovARIARandomVerify() {
        for (int i = 1; i < 1000; i++) {
            String sOrgStr = createRndString(i);
            assertEquals(
                    sOrgStr,
                    cryptoService.decrypt(cryptoService.encrypt(sOrgStr)),
                    "round-trip failed at length " + i);
        }
    }

}
