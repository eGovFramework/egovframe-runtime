package egovframework.rte.fdl.cryptography;

import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/*.xml" })
public class EgovDigestServiceTest {
    @Resource(name="digestService")
    EgovDigestService digestService;
    
    @Test
    public void testDigest() {
		String data = "egovframe";

		byte[] digested = digestService.digest(data.getBytes());

		assertTrue(digestService.matches(data.getBytes(), digested));
    }
}
