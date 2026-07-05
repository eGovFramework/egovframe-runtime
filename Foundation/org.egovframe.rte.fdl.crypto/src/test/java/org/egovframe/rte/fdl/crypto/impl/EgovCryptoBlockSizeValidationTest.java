package org.egovframe.rte.fdl.crypto.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link EgovARIACryptoServiceImpl}·{@link EgovGeneralCryptoServiceImpl}의 setBlockSize가
 * 0/음수를 거부하는지 검증한다.
 *
 * <p>blockSize가 0이면 encrypt(File)의 read 루프가 종료되지 않아 무한 루프(CWE-835)에 빠지고,
 * 음수이면 버퍼 할당에서 예외가 발생한다. setBlockSize에서 양수만 허용하도록 입력을 검증한다.</p>
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
