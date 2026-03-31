package org.egovframe.rte.fdl.crypto;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.crypto.config.EgovCryptoTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EgovCryptoTestConfig.class)
public class EgovDigestServiceTest {

    @Resource(name = "egov.digestService")
    private EgovDigestService digestService;

    @Test
    public void testDigest() {
        String data = "egovframe";
        byte[] digested = digestService.digest(data.getBytes());
        assertTrue(digestService.matches(data.getBytes(), digested));
    }

}
