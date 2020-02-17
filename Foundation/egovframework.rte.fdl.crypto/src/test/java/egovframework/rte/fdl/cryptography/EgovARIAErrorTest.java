package egovframework.rte.fdl.cryptography;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import egovframework.rte.fdl.cryptography.impl.EgovEnvCryptoServiceImpl;

public class EgovARIAErrorTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIAErrorTest.class);
	
	public static void main(String[] args) {
		
		
		String[] arrCryptoString = { 
                "ckimage/2018/12"
              };

		
		LOGGER.info("------------------------------------------------------");		
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:/META-INF/spring/config/crypto-config.xml"});
		EgovEnvCryptoService cryptoService = context.getBean(EgovEnvCryptoServiceImpl.class);
		LOGGER.info("------------------------------------------------------");
		
		
		String label = "";
		try {
			for(int i=0; i < arrCryptoString.length; i++) {		

				LOGGER.info(label+" 원본(orignal):" + arrCryptoString[i]);
				LOGGER.info(label+" 인코딩(encrypted):" + cryptoService.encrypt(arrCryptoString[i]) );
				LOGGER.info(label+" 디코딩(decrypted):" + cryptoService.decrypt(cryptoService.encrypt(arrCryptoString[i])) );
				if( cryptoService.decrypt(cryptoService.encrypt(arrCryptoString[i])).equals(arrCryptoString[i]) ) {
					LOGGER.info(label+" 통과 !!!");
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
