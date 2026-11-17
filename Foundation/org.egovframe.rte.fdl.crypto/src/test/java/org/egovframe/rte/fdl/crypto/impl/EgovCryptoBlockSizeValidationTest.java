package org.egovframe.rte.fdl.crypto.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link EgovARIACryptoServiceImpl}·{@link EgovGeneralCryptoServiceImpl}의 setBlockSize가
 * 0/음수를 거부하는지 검증한다.
 *
 * <p>0 이면 encrypt(File) read 무한 루프(CWE-835), 음수면 버퍼 할당 예외 → 양수만 허용.</p>
 */
class EgovCryptoBlockSizeValidationTest {

    @Test
    @DisplayName("ARIA: blockSize 0·음수는 IllegalArgumentException으로 거부한다")
    void ariaRejectsNonPositiveBlockSize() {
        EgovARIACryptoServiceImpl service = new EgovARIACryptoServiceImpl();
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(0));
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(-16));
        // 양수는 정상(내부적으로 BLOCKSIZE_MODULAR 배수로 정규화)
        assertDoesNotThrow(() -> service.setBlockSize(512));
    }

    @Test
    @DisplayName("General: blockSize 0·음수는 IllegalArgumentException으로 거부한다")
    void generalRejectsNonPositiveBlockSize() {
        EgovGeneralCryptoServiceImpl service = new EgovGeneralCryptoServiceImpl();
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(0));
        assertThrows(IllegalArgumentException.class, () -> service.setBlockSize(-1));
        assertDoesNotThrow(() -> service.setBlockSize(1024));
    }
}
