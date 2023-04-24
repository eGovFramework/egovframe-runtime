package org.egovframe.rte.fdl.cryptography;

import org.egovframe.rte.fdl.cryptography.impl.EgovEnvCryptoServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/config/crypto-config.xml" })
public class EgovARIATest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIATest.class);
	
	@Resource(name = "egov.envCryptoService")
	EgovEnvCryptoService cryptoService;
	
	String sAlgorithmKey = "egovframe";
	
    @Test
    public void EgovARIARandomVerify() throws Exception {
    	boolean bResult = Boolean.TRUE;

		String sOrgStr = "";
		for (int i=1; i < 1000; i++ ) {
			sOrgStr = createRndStringNew(i);
			if( cryptoService.decrypt(cryptoService.encrypt(sOrgStr)).equals(sOrgStr) ) {
				 //bResult = Boolean.TRUE;
			}else {
				 bResult = Boolean.FALSE;
			}
		}

    	assertEquals(Boolean.TRUE, bResult);
    }
	
	public static void main(String[] args) {
		
		String[] arrCryptoString = { 
                "ckimage/2018/12ckimage/2018/12ckimage/2018/121123ckimage/2018/12ckimage/2018/12ckimage/2018/121123"
              };

		
		LOGGER.info("------------------------------------------------------");		
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:/META-INF/spring/config/crypto-config.xml"});
		EgovEnvCryptoService cryptoService = context.getBean(EgovEnvCryptoServiceImpl.class);
		LOGGER.info("------------------------------------------------------");
		
		String label = "";
		String result = "통과 !!!";
		try {
			String orgStr = "";
			for (int i=1; i < 1000; i++ ) {
				orgStr = createRndStringNew(i);
				LOGGER.debug("===>>>["+i+"] -----------");
				LOGGER.info(label+" 원본(orignal):" + orgStr);
				LOGGER.info(label+" 인코딩(encrypted):" + cryptoService.encrypt(orgStr) );
				LOGGER.info(label+" 디코딩(decrypted):" + cryptoService.decrypt(cryptoService.encrypt(orgStr)) );
				if( cryptoService.decrypt(cryptoService.encrypt(orgStr)).equals(orgStr) ) {
					LOGGER.info(label+" 통과 !!!");
				}else {
					result = "실패 !!!";
					LOGGER.info(label+" 실패 !!!");
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
		System.out.println("------------------------------------------------------");
		System.out.println("최종결과 > "+result);
		System.out.println("최종결과 > "+result);
		System.out.println("최종결과 > "+result);		
	
	}

	
	private static String createRndStringNew(int length) {

		StringBuffer result = new StringBuffer();
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
		    	result.append( (char) ((Math.random() * 11172) + 0xAC00) );
		        break;
		    }
		}

		return result.toString();
	}
	
	
	private static String createRndStringOld(int length) {
		
		StringBuffer result = new StringBuffer();
		Random rnd = new Random();
		for (int i = 0; i < length; i++) {
		    int rIndex = rnd.nextInt(3);
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
		    }
		}

		return result.toString();
	}
}
