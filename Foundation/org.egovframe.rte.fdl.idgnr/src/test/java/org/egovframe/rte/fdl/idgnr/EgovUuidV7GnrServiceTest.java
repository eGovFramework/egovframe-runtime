package org.egovframe.rte.fdl.idgnr;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.impl.EgovUuidV7GnrServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UUID 버전 7(RFC 9562) 채번 서비스를 검증한다.
 */
public class EgovUuidV7GnrServiceTest {

    private final EgovUuidV7GnrServiceImpl service = new EgovUuidV7GnrServiceImpl();

    @Test
    public void testVersionAndVariant() throws FdlException {
        UUID uuid = UUID.fromString(service.getNextStringId());

        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant(), "IETF variant(10xx)여야 한다");
    }

    @Test
    public void testLeading48BitsAreCreationTimestamp() throws FdlException {
        long before = System.currentTimeMillis();
        UUID uuid = UUID.fromString(service.getNextStringId());
        long after = System.currentTimeMillis();

        long embedded = uuid.getMostSignificantBits() >>> 16;
        assertTrue(embedded >= before && embedded <= after,
                "타임스탬프 " + embedded + " 는 [" + before + ", " + after + "] 안이어야 한다");
    }

    @Test
    public void testLaterMillisecondSortsAfterEarlier() throws Exception {
        String first = service.getNextStringId();
        Thread.sleep(2);
        String second = service.getNextStringId();

        assertTrue(first.compareTo(second) < 0, first + " < " + second);
    }

    @Test
    public void testBulkGenerationIsUnique() throws FdlException {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 5_000; i++) {
            assertTrue(ids.add(service.getNextStringId()), "중복 발생");
        }
    }

    @Test
    public void testBigDecimalIsUnsigned128BitValueOfTheUuid() throws FdlException {
        BigDecimal id = service.getNextBigDecimalId();

        assertTrue(id.signum() > 0);
        assertTrue(id.toBigInteger().bitLength() <= 128, "128비트 안이어야 한다: " + id);
        // 상위 64비트를 다시 꺼내면 버전 7 이어야 한다
        BigInteger high = id.toBigInteger().shiftRight(64);
        assertEquals(7, high.shiftRight(12).and(BigInteger.valueOf(0xF)).intValue());
    }

    @Test
    public void testUnsupportedTypesThrowWithoutMessageSource() {
        assertThrows(FdlException.class, service::getNextLongId);
        assertThrows(FdlException.class, service::getNextIntegerId);
        assertThrows(FdlException.class, service::getNextShortId);
        assertThrows(FdlException.class, service::getNextByteId);
        assertThrows(FdlException.class, () -> service.getNextStringId("policy"));
        assertThrows(FdlException.class, () -> service.getNextStringId((EgovIdGnrStrategy) null));
    }

    @Test
    public void testStaticGenerateNeedsNoBeanOrConfiguration() {
        UUID uuid = EgovUuidV7GnrServiceImpl.generate();

        assertEquals(7, uuid.version());
        assertEquals(36, uuid.toString().length());
    }
}
