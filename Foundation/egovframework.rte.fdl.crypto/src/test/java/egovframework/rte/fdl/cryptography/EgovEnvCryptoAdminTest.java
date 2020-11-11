package egovframework.rte.fdl.cryptography;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import egovframework.rte.fdl.cryptography.EgovCryptoService;
import egovframework.rte.fdl.cryptography.EgovPasswordEncoder;
import egovframework.rte.fdl.cryptography.impl.EgovARIACryptoServiceImpl;

/**
 * @Class Name : EgovUrlIdPasswordCryptoAdminTest.java
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

public class EgovEnvCryptoAdminTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovEnvCryptoAdminTest.class);
	
	//계정 아이디
	String[] arrCryptoString = { 
            "publisher",       //데이터베이스 접속 계정
            "publisher1024!@#", //데이터베이스 접속 패드워드
            "jdbc:log4jdbc:mysql://127.0.0.1:3306/com?zeroDateTimeBehavior=convertToNull&"           //데이터베이스 접속 주소
          };
	
	//계정암호화키 키
	public String algorithmKey = "egovframe";

	//계정암호화 알고리즘(MD5, SHA-1, SHA-256)
	public String algorithm = "SHA-256";
	
	//계정암호화키 블럭사이즈
	public int algorithmBlockSize = 1024;
	    
	public static void main(String[] args) {
		EgovEnvCryptoAdminTest cryptoTest = new EgovEnvCryptoAdminTest();
		
		EgovPasswordEncoder egovPasswordEncoder = new EgovPasswordEncoder();
		egovPasswordEncoder.setAlgorithm(cryptoTest.algorithm);
		
		LOGGER.info("------------------------------------------------------");
		LOGGER.info("알고리즘(algorithm):"+cryptoTest.algorithm);
		LOGGER.info("알고리즘 키(algorithmKey):"+cryptoTest.algorithmKey);
		LOGGER.info("알고리즘 키 Hash(algorithmKeyHash):"+egovPasswordEncoder.encryptPassword(cryptoTest.algorithmKey));
		LOGGER.info("알고리즘 블럭사이즈(algorithmBlockSize):"+cryptoTest.algorithmBlockSize);
		
		egovPasswordEncoder.setHashedPassword(egovPasswordEncoder.encryptPassword(cryptoTest.algorithmKey));
		
		EgovCryptoService cryptoService = new EgovARIACryptoServiceImpl();
		cryptoService.setPasswordEncoder(egovPasswordEncoder);
		cryptoService.setBlockSize(cryptoTest.algorithmBlockSize);
		
		Base64 base64 = new Base64();
	
		String label = "";
	
		LOGGER.info("------------------------------------------------------");
		try {
			for(int i=0; i < cryptoTest.arrCryptoString.length; i++) {
				
				if(i==0)label = "사용자 아이디";
				if(i==1)label = "사용자 비밀번호";
				if(i==2)label = "접속 주소";
				
				byte[] encrypted = cryptoService.encrypt(cryptoTest.arrCryptoString[i].getBytes("UTF-8"), cryptoTest.algorithmKey);
				
				String encryptedResult = URLEncoder.encode(new String(base64.encode(encrypted), "UTF-8"), "UTF-8");
				
				byte[] decrypted = cryptoService.decrypt(base64.decode(URLDecoder.decode(encryptedResult, "UTF-8").getBytes("UTF-8")), cryptoTest.algorithmKey);
				
				LOGGER.info(label+"원본(orignal):" + cryptoTest.arrCryptoString[i]);
				LOGGER.info(label+"인코딩(encrypted):" + encryptedResult);
				LOGGER.info(label+"디코딩(decrypted):" + new String(decrypted, "UTF-8"));
				LOGGER.info("------------------------------------------------------");
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("["+e.getClass()+"] UnsupportedEncodingException : " + e.getMessage());
			//e.printStackTrace();
		} catch (Exception e) {
			LOGGER.error("["+e.getClass()+"] Exception : " + e.getMessage());
			//e.printStackTrace();
		}
	
	}

}
