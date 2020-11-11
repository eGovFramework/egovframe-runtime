package egovframework.rte.fdl.cryptography;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import egovframework.rte.fdl.cryptography.impl.EgovEnvCryptoServiceImpl;
import egovframework.rte.fdl.cryptography.EgovEnvCryptoService;

/**
 * @Class Name : EgovUrlIdPasswordCryptoTest.java
 * @Description : 데이터베이스 접속정보를 암호화하기 위한 클래스 
 * @author 장동한
 * @since 2018.08.11
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2018.08.11  장동한           최초 생성
 *   
 * </pre>
 */

public class EgovEnvCryptoUserTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovEnvCryptoUserTest.class);
    
	public static void main(String[] args) {
		
		String[] arrCryptoString = { 
                "userId",       //데이터베이스 접속 계정
                "userPassword", //데이터베이스 접속 패드워드
                "url"           //데이터베이스 접속 주소
              };

		
		LOGGER.info("------------------------------------------------------");		
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:/META-INF/spring/config/crypto-config.xml"});
		EgovEnvCryptoService cryptoService = context.getBean(EgovEnvCryptoServiceImpl.class);
		LOGGER.info("------------------------------------------------------");
		
		String label = "";
		try {
			for(int i=0; i < arrCryptoString.length; i++) {		
				if(i==0)label = "사용자 아이디";
				if(i==1)label = "사용자 비밀번호";
				if(i==2)label = "접속 주소";
				LOGGER.info(label+" 원본(orignal):" + arrCryptoString[i]);
				LOGGER.info(label+" 인코딩(encrypted):" + cryptoService.encrypt(arrCryptoString[i]));
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
