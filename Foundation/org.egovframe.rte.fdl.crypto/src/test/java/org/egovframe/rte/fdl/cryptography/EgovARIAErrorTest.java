package org.egovframe.rte.fdl.cryptography;

import org.egovframe.rte.fdl.cryptography.impl.EgovEnvCryptoServiceImpl;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EgovARIAErrorTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIAErrorTest.class);
	
	public static void main(String[] args) {

		String[] arrCryptoString = {
				"ckimage/2018/12",
				"https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.2"
		};

		LOGGER.info("------------------------------------------------------");

		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/META-INF/spring/config/crypto-config.xml");
		EgovEnvCryptoService cryptoService = context.getBean(EgovEnvCryptoServiceImpl.class);

		LOGGER.info("------------------------------------------------------");

		try {
            for (String s : arrCryptoString) {
                LOGGER.info(" 원본(orignal) : {}", s);
                LOGGER.info(" 인코딩(encrypted) : {}", cryptoService.encrypt(s));
                LOGGER.info(" 디코딩(decrypted) : {}", cryptoService.decrypt(cryptoService.encrypt(s)));

                if (cryptoService.decrypt(cryptoService.encrypt(s)).equals(s)) {
                    LOGGER.info( " 통과 !!!");
                } else {
					LOGGER.error(" 실패 !!!");
					return;
				}

				LOGGER.info("------------------------------------------------------");

				LOGGER.info(" 인코딩(encrypted None) : {}", cryptoService.encryptNone(s));
				LOGGER.info(" 디코딩(decrypted None) : {}", cryptoService.decryptNone(cryptoService.encrypt(s)));

				if (cryptoService.decryptNone(cryptoService.encryptNone(s)).equals(s)) {
					LOGGER.info( " 통과 !!!");
				} else {
					LOGGER.error(" 실패 !!!");
					return;
				}

                LOGGER.info("------------------------------------------------------");
            }
		} catch (IllegalArgumentException e) {
            LOGGER.error("[{}] IllegalArgumentException : {}", e.getClass(), e.getMessage());
		} catch (EncryptionOperationNotPossibleException e) {
            LOGGER.error("[{}] EncryptionOperationNotPossibleException : {}", e.getClass(), e.getMessage());
		}

    }

}
