package org.egovframe.rte.fdl.crypto;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.crypto.config.EgovCryptoConfig;
import org.egovframe.rte.fdl.crypto.config.EgovCryptoTestConfig;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * egov-crypto 설정 및 EnvCrypto 테스트
 * properties 설정 파일(egov-crypto-config.properties) 기반으로 빈 로드 검증
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { EgovCryptoTestConfig.class })
public class EgovEvnCryptoConfigTest {

    @Resource(name = "cryptoConfig")
    private EgovCryptoConfig cryptoConfig;

    @Resource(name = "egov.envCryptoService")
    private EgovEnvCryptoService egovEnvCrypto;

    @Resource(name = "egovPropertyConfigurer")
    private EgovPropertyService propertyService;

    /**
     * properties 설정 파일 로드 테스트
     */
    @Test
    public void testPropertiesConfigurationLoading() {
        assertNotNull(cryptoConfig, "EgovCryptoConfig 빈이 로드되어야 함");
        assertEquals("egovCryptoConfig", cryptoConfig.getId());
        assertTrue(cryptoConfig.isInitial());
        assertTrue(cryptoConfig.isCrypto());
        assertEquals("SHA-256", cryptoConfig.getAlgorithm());
        assertEquals("egovframe", cryptoConfig.getAlgorithmKey());
        assertNotNull(cryptoConfig.getAlgorithmKeyHash());
        assertEquals(1024, cryptoConfig.getCryptoBlockSize());
        assertTrue(cryptoConfig.getCryptoPropertyLocation().contains("jdbc.properties"));
        assertFalse(cryptoConfig.isPlainDigest());
    }

    @Test
    public void EgovDatabaseCrypto() throws Exception {
        assertEquals(Boolean.valueOf(propertyService.getString("Globals.Crypto")), egovEnvCrypto.isCrypto());

        // globals: encrypted values, envCrypto: decrypted -> must not be equal
        String encDriver = propertyService.getString("Globals.mysql.DriverClassName");
        String decDriver = egovEnvCrypto.getDriverClassName();
        assertNotNull(encDriver, "Globals.mysql.DriverClassName");
        assertNotNull(decDriver, "envCrypto.getDriverClassName()");
        assertNotEquals(encDriver, decDriver);

        String encUrl = propertyService.getString("Globals.mysql.Url");
        String decUrl = egovEnvCrypto.getUrl();
        assertNotNull(encUrl, "Globals.mysql.Url");
        assertNotNull(decUrl, "envCrypto.getUrl()");
        assertNotEquals(encUrl, decUrl);

        String encUser = propertyService.getString("Globals.mysql.UserName");
        String decUser = egovEnvCrypto.getUsername();
        assertNotNull(encUser, "Globals.mysql.UserName");
        assertNotNull(decUser, "envCrypto.getUsername()");
        assertNotEquals(encUser, decUser);

        String encPassword = propertyService.getString("Globals.mysql.Password");
        String decPassword = egovEnvCrypto.getPassword();
        assertNotNull(encPassword, "Globals.mysql.Password (property)");
        assertNotNull(decPassword, "envCrypto.getPassword()");
        // When password is empty, encrypted and decrypted may both be "" -> allow equal in that case
        if (encPassword != null && !encPassword.isEmpty()) {
            assertNotEquals(encPassword, decPassword);
        }
    }

}
