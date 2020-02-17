package egovframework.rte.fdl.cryptography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/*.xml" })
public class EgovGeneralCryptoServiceTest {
    @Resource(name="generalCryptoService")
    EgovCryptoService cryptoService;
    
    @Resource(name="password")
    String password;
    
    private byte[] getHashValue(File file) throws Exception {
    	MessageDigest sha = MessageDigest.getInstance("SHA-256");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DigestOutputStream dout = new DigestOutputStream(baos, sha);

		byte[] data = new byte[1024];

		FileInputStream fis = null;
		BufferedInputStream is = null;
		
		try {
			fis = new FileInputStream(file);
			is = new BufferedInputStream(fis);
			while (true) {
				int bytesRead = is.read(data);
				if (bytesRead < 0)
					break;
				dout.write(data, 0, bytesRead);
			}
			dout.flush();
			byte[] result = dout.getMessageDigest().digest();
	
			return result;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ignore) {
					// no-op
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception ignore) {
						// no-op
				}
			}
		}
    }
    
	private boolean checkFileWithHashFunction(File srcFile, File trgtFile) throws Exception {

		byte[] srcByte = getHashValue(srcFile);

		byte[] trgtByte = getHashValue(trgtFile);

		if (srcByte.length != trgtByte.length) {
			System.out.println("=====> length not same!!");
			return false;
		}

		for (int i = 0; i < srcByte.length; i++) {
			if (srcByte[i] != trgtByte[i]) {
				System.out.println("=====> byte not same in " + i);
				return false;
			}
		}

		return true;
    }

    @Test
    public void testString() {
		String[] testString = {
			"This is a testing...\nHello!",
			"한글 테스트입니다...",
			"!@#$%^&*()_+|~{}:\"<>?-=\\`[];',./"
		};
	
		try {
			for (String str : testString) {
				byte[] encrypted = cryptoService.encrypt(str.getBytes("UTF-8"), password);

				byte[] decrypted = cryptoService.decrypt(encrypted, password);

				assertEquals(str, new String(decrypted, "UTF-8"));
			}
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			fail();
		}
    }
    
    @Test
	public void testBigDecimal() {
		BigDecimal big = new BigDecimal(123456);

		BigDecimal encrypted = cryptoService.encrypt(big, password);

		BigDecimal decrypted = cryptoService.decrypt(encrypted, password);

		assertEquals(big, decrypted);
	}
    
    @Test
    public void testFile() {
		String filePath = "/META-INF/spring/file/test.hwp";

		File srcFile = new File(this.getClass().getResource(filePath).getFile());

		File trgtFile;
		File decryptedFile;
		try {
			trgtFile = File.createTempFile("tmp", "encrypted");
			trgtFile.deleteOnExit();
			//trgtFile = new File("C:/test.enc");

			//System.out.println("Temp file : " + trgtFile.toString());

			cryptoService.encrypt(srcFile, password, trgtFile);

			decryptedFile = File.createTempFile("tmp", "decrypted");
			decryptedFile.deleteOnExit();
			//decryptedFile = new File("C:/test.dec.hwp");

			cryptoService.decrypt(trgtFile, password, decryptedFile);

			assertTrue("Decrypted file not same!!", checkFileWithHashFunction(srcFile, decryptedFile));

		} catch (IOException ioe) {
			ioe.printStackTrace();
			fail(ioe.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
    }
}
