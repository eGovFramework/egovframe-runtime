package org.egovframe.rte.fdl.cryptography.impl;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

/**
 * 파일 암복호화 블록 크기가 0 이하이면 거부하는지 검증한다(무한 루프 방지, CWE-835).
 */
public class EgovCryptoBlockSizeSecurityTest {

    @Test
    public void ARIA_블록_크기는_양수여야_한다() {
        EgovARIACryptoServiceImpl service = new EgovARIACryptoServiceImpl();
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(0));
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(-16));
        service.setBlockSize(1000);
    }

    @Test
    public void 일반_암호화_블록_크기는_양수여야_한다() {
        EgovGeneralCryptoServiceImpl service = new EgovGeneralCryptoServiceImpl();
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(0));
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(-1));
        service.setBlockSize(1024);
    }

}
