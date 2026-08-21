package org.egovframe.rte.fdl.idgnr;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.config.IdgnrTestConfig;
import org.egovframe.rte.fdl.idgnr.config.UUIdGenerationConfig;
import org.egovframe.rte.fdl.idgnr.impl.EgovUUIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UUId Generation Service Test 클래스
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2009.02.01  김태호          최초 생성
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IdgnrTestConfig.class, UUIdGenerationConfig.class})
public class EgovUUIdGnrServiceTest {

    @Resource(name = "UUIdGenerationService")
    private EgovIdGnrService uUidGenerationService;

    @Resource(name = "UUIdGenerationServiceWithoutAddress")
    private EgovIdGnrService uUIdGenerationServiceWithoutAddress;

    @Resource(name = "UUIdGenerationServiceWithIP")
    private EgovIdGnrService uUIdGenerationServiceWithIP;

    /**
     * Mac Address 세팅 테스트
     */
    @Test
    public void testUUIdGeneration() throws FdlException {
        // 1. get next String id
        String uuid = null;
        for (int i = 0; i < 10; i++) {
            assertNotNull(uuid = uUidGenerationService.getNextStringId());
        }

        // 2. get next BigDecimal id
        BigDecimal decimal;
        for (int i = 0; i < 10; i++) {
            assertNotNull(decimal = uUidGenerationService.getNextBigDecimalId());
        }
    }

    /**
     * Mac Address 세팅없이 테스트
     */
    @Test
    public void testUUIdGenerationNoAddress() throws FdlException {
        // 1. get next String id
        String uuid = null;
        for (int i = 0; i < 10; i++) {
            assertNotNull(uuid = uUIdGenerationServiceWithoutAddress.getNextStringId());
        }

        // 2. get next BigDecimal id
        BigDecimal decimal;
        for (int i = 0; i < 10; i++) {
            assertNotNull(decimal = uUIdGenerationServiceWithoutAddress.getNextBigDecimalId());
        }
    }

    /**
     * IP 세팅 테스트
     */
    @Test
    public void testUUIdGenerationIP() throws FdlException {
        // 1. get next String id
        String uuid = null;
        for (int i = 0; i < 10; i++) {
            assertNotNull(uuid = uUIdGenerationServiceWithIP.getNextStringId());
        }

        // 2. get next BigDecimal id
        BigDecimal decimal;
        for (int i = 0; i < 10; i++) {
            assertNotNull(decimal = uUIdGenerationServiceWithIP.getNextBigDecimalId());
        }
    }

    /**
     * IP 세팅시 octet 은 10진수로 해석되어야 함 테스트
     */
    @Test
    public void testUUIdGenerationIPOctetRadix() throws FdlException {
        // 100.128.120.107 의 octet 은 0x64, 0x80, 0x78, 0x6B (앞 2 byte 는 0xFF 고정)
        assertEquals(0x0000FFFF6480786BL, hostId(uUIdGenerationServiceWithIP.getNextStringId()));
    }

    /**
     * 서로 다른 IP 가 같은 host id 로 접히지 않는지 테스트
     */
    @Test
    public void testUUIdGenerationIPHostIdCollision() throws Exception {
        EgovUUIdGnrServiceImpl generator = new EgovUUIdGnrServiceImpl();
        generator.setAddress("192.168.156.1");
        EgovUUIdGnrServiceImpl other = new EgovUUIdGnrServiceImpl();
        other.setAddress("192.168.56.1");

        assertEquals(0x0000FFFFC0A89C01L, hostId(generator.getNextStringId()));
        assertEquals(0x0000FFFFC0A83801L, hostId(other.getNextStringId()));
    }

    private long hostId(String uuid) {
        return UUID.fromString(uuid).getLeastSignificantBits() & 0xFFFFFFFFFFFFL;
    }

    /**
     * UUID Generation Service 는 getNextStringId, getNextBigDecimalId 만 제공.
     */
    @Test
    public void testNotSupported() throws Exception {
        // 1. get next byte id
        try {
            uUidGenerationService.getNextByteId();
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }

        // 2. get next integer id
        try {
            uUidGenerationService.getNextIntegerId();
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }

        // 3. get next long id
        try {
            uUidGenerationService.getNextLongId();
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }

        // 4. get next short id
        try {
            uUidGenerationService.getNextShortId();
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }

        // 5. get next string id with a specific
        // strategy
        try {
            uUidGenerationService.getNextStringId("mixPrefix");
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }

        // 6. get next string id with a specific
        // strategy
        try {
            uUidGenerationService.getNextStringId(new EgovIdGnrStrategyImpl());
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

}
