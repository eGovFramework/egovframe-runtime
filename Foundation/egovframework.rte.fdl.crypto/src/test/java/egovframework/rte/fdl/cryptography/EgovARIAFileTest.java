package egovframework.rte.fdl.cryptography;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import egovframework.rte.fdl.cryptography.impl.EgovARIACryptoServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/context-crypto.xml" })
public class EgovARIAFileTest {
	
	@Resource(name = "ARIACryptoService")
	EgovCryptoService cryptoService;
	
	String sAlgorithmKey = "egovframe";

	
    @Test
    public void EgovARIAFilVerify() throws Exception{
    	
    	String sPath = EgovARIAFileTest.class.getResource("").getPath();
    	cryptoService.encrypt(new File(sPath+"/test/test.txt"), sAlgorithmKey, new File(sPath+"/test/test2.txt"));
    	cryptoService.decrypt(new File(sPath+"/test/test2.txt"), sAlgorithmKey, new File(sPath+"/test/test3.txt"));
		
    	List<String> list = new ArrayList<String>();
    	List<String> list3 = new ArrayList<String>();

    	
        //파일 객체 생성
        File file = new File(sPath+"/test/test.txt");
        File file3 = new File(sPath+"/test/test3.txt");
        
        //입력 스트림 생성
        FileReader filereader = new FileReader(file);
        FileReader filereader3 = new FileReader(file3);
        //입력 버퍼 생성
        BufferedReader bufReader = new BufferedReader(filereader);
        BufferedReader bufReader3 = new BufferedReader(filereader3);
        String line = "";
        while((line = bufReader.readLine()) != null){
            //System.out.println(line);
        	list.add(line);
        }
        while((line = bufReader3.readLine()) != null){
            //System.out.println(line);
        	list3.add(line);
        }
        //.readLine()은 끝에 개행문자를 읽지 않는다.            
        bufReader.close();
        
        //System.out.print(list);


    	//assertEquals(list, list3);
        assertEquals(list, list3);
    }

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIAFileTest.class);
	
	public static void main(String[] args) {
		
		EgovARIAFileTest egovARIAFileTest = new EgovARIAFileTest();
	
		//String sPath = EgovARIAFileTest.class.getResource("").getPath();
		String sPath = "";
		
		LOGGER.info("------------------------------------------------------");		
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:/META-INF/spring/context-crypto.xml"});
		EgovARIACryptoService cryptoService = context.getBean(EgovARIACryptoServiceImpl.class);
		LOGGER.info("------------------------------------------------------");
		
			try {
				//System.out.println(EgovARIAFileTest.class.getResource("").getPath() );
				sPath = EgovARIAFileTest.class.getResource("").getPath();
				cryptoService.encrypt(new File(sPath+"/test/test.txt"), egovARIAFileTest.sAlgorithmKey, new File(sPath+"/test/test2.txt"));
				cryptoService.decrypt(new File(sPath+"/test/test2.txt"), egovARIAFileTest.sAlgorithmKey, new File(sPath+"/test/test3.txt"));

				System.out.println("------------------------------------------------------");
				//assertEquals(str, new String(decrypted, "UTF-8"));
				
			} catch (Exception uee) {
				uee.printStackTrace();

			}
	}

}
