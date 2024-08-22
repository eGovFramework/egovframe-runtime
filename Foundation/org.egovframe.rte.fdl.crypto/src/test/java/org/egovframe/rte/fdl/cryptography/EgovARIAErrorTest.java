package org.egovframe.rte.fdl.cryptography;

import org.egovframe.rte.fdl.cryptography.impl.EgovEnvCryptoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.URLEncoder;

public class EgovARIAErrorTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIAErrorTest.class);
	
	public static void main(String[] args) {


		String[] givenStrs = {
				"ckimage/2018/12",
				"FILE_000000000000001",
				"FILE_000000000000002",
				"FILE_000000000000003",
				"FILE_000000000000004",
				"FILE_000000000000005",
				"FILE_000000000000006",
				"FILE_000000000000007",
				"FILE_000000000000008",
				"FILE_000000000000009",
				"FILE_000000000000010",
		};

		
		LOGGER.info("------------------------------------------------------");		
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:/META-INF/spring/config/crypto-config.xml"});
		EgovEnvCryptoService cryptoService = context.getBean(EgovEnvCryptoServiceImpl.class);
		LOGGER.info("------------------------------------------------------");
		
		
		String label = "";
		try {
            for (String str : givenStrs) {

                String encrypt = cryptoService.encrypt(str);
                LOGGER.info(label + " 원본(orignal):" + str);
                LOGGER.info(label + " 인코딩(encrypted):" + encrypt);
                LOGGER.info(label + " 디코딩(decrypted):" + cryptoService.decrypt(encrypt));
                LOGGER.info(label + " URL인코딩(encrypted+url):" + URLEncoder.encode(encrypt, "UTF-8"));

                if (!encrypt.equals(URLEncoder.encode(encrypt, "UTF-8"))) {
                    LOGGER.error(label + " 실패 !!!(URL SAFE 실패)");
                    return;
                } else if (cryptoService.decrypt(encrypt).equals(str)) {
                    LOGGER.info(label + " 통과 !!!");
                } else {
                    LOGGER.error(label + " 실패 !!!");
                    return;
                }
                LOGGER.info("------------------------------------------------------");
            }
		} catch (IllegalArgumentException e) {
			LOGGER.error("["+e.getClass()+"] IllegalArgumentException : " + e.getMessage());
			//e.printStackTrace();
		} catch (Exception e) {
			LOGGER.error("["+e.getClass()+"] Exception : " + e.getMessage());
			//e.printStackTrace();
		}
	
	}

}
