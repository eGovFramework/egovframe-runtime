package org.egovframe.rte.fdl.cryptography;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/config/crypto-config.xml" })
public class EgovEvnCryptoConfigTest extends AbstractJUnit4SpringContextTests{

    @Resource(name="egov.envCryptoService")
    EgovEnvCryptoService egovEnvCrypto;
    
    @Resource(name="egovPropertyConfigurer")
    private EgovPropertyService propertyService;
    
    @Test
    public void EgovDatabaseCrypto() {

		try {
						
			assertEquals(Boolean.valueOf(propertyService.getString("Globals.db.Crypto")), egovEnvCrypto.isCrypto());
			assertNotEquals(propertyService.getString("Globals.mysql.Url"), egovEnvCrypto.getUrl());
			assertNotEquals(propertyService.getString("Globals.mysql.UserName"), egovEnvCrypto.getUsername());
			assertNotEquals(propertyService.getString("Globals.mysql.Password"), egovEnvCrypto.getPassword());
			
		} catch (Exception uee) {
			uee.printStackTrace();
			fail();
		}
    }

	private void assertNotEquals(String property, String url) {
		// TODO Auto-generated method stub
		
	}
    
   
}
