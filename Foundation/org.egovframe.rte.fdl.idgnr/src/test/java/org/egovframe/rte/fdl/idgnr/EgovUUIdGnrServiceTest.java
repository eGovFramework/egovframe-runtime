package org.egovframe.rte.fdl.idgnr;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.config.IdgnrTestConfig;
import org.egovframe.rte.fdl.idgnr.config.UUIdGenerationConfig;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

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
