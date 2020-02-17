package egovframework.rte.fdl.idgnr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import egovframework.rte.fdl.cmmn.exception.FdlException;
import egovframework.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * UUId Generation Service Test 클래스
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.01  김태호          최초 생성
 * 
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/spring/context-uuid.xml" })
public class EgovUUIdGnrServiceTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovUUIdGnrServiceTest.class);

    @Resource(name = "UUIdGenerationService")
    private EgovIdGnrService uUidGenerationService;

    @Resource(name = "UUIdGenerationServiceWithoutAddress")
    private EgovIdGnrService uUIdGenerationServiceWithoutAddress;

    @Resource(name = "UUIdGenerationServiceWithIP")
    private EgovIdGnrService uUIdGenerationServiceWithIP;

    /**
     * Mac Address 세팅 테스트
     * @throws Exception
     *         fail to test
     */
    @Test
    public void testUUIdGeneration() throws Exception {

        // 1. get next String id
    	String uuid = null;
        for (int i = 0; i < 10; i++) {
            assertNotNull(uuid = uUidGenerationService.getNextStringId());
            LOGGER.info("UUID : {} (version = {})", uuid, UUID.fromString(uuid).version());
        }
        // 2. get next BigDecimal id
        BigDecimal decimal;
        for (int i = 0; i < 10; i++) {
            assertNotNull(decimal = uUidGenerationService.getNextBigDecimalId());
            LOGGER.info("UUID (BigDecimal) : {}", decimal);
        }
    }

    /**
     * Mac Address 세팅없이 테스트
     * @throws Exception
     *         fail to test
     */
    @Test
    public void testUUIdGenerationNoAddress() throws Exception {

        // 1. get next String id
    	String uuid = null;
        for (int i = 0; i < 10; i++) {
            assertNotNull(uuid = uUIdGenerationServiceWithoutAddress.getNextStringId());
            LOGGER.info("UUID : {} (version = {})", uuid, UUID.fromString(uuid).version());
        }
        // 2. get next BigDecimal id
        BigDecimal decimal;
        for (int i = 0; i < 10; i++) {
            assertNotNull(decimal = uUIdGenerationServiceWithoutAddress.getNextBigDecimalId());
            LOGGER.info("UUID (BigDecimal) : {}", decimal);
        }
    }

    /**
     * IP 세팅 테스트
     * @throws Exception
     *         fail to test
     */
    @Test
    public void testUUIdGenerationIP() throws Exception {

        // 1. get next String id
    	String uuid = null;
        for (int i = 0; i < 10; i++) {
            assertNotNull(uuid = uUIdGenerationServiceWithIP.getNextStringId());
            LOGGER.info("UUID : {} (version = {})", uuid, UUID.fromString(uuid).version());
        }
        // 2. get next BigDecimal id
        BigDecimal decimal;
        for (int i = 0; i < 10; i++) {
            assertNotNull(decimal = uUIdGenerationServiceWithIP.getNextBigDecimalId());
            LOGGER.info("UUID (BigDecimal) : {}", decimal);
        }
    }

    /**
     * UUID Generation Service는
     * getNextStringId,getNextBigDecimalId 만 제공.
     * @throws Exception fail to test
     */
    @Test
    public void testNotSupported() throws Exception {

        // 1. get next byte id
        try {
            uUidGenerationService.getNextByteId();
        } catch (Exception e) {
            assertTrue(e instanceof FdlException);
        }

        // 2. get next integer id
        try {
            uUidGenerationService.getNextIntegerId();
        } catch (Exception e) {
            assertTrue(e instanceof FdlException);
        }

        // 3. get next long id
        try {
            uUidGenerationService.getNextLongId();
        } catch (Exception e) {
            assertTrue(e instanceof FdlException);
        }

        // 4. get next short id
        try {
            uUidGenerationService.getNextShortId();
        } catch (Exception e) {
            assertTrue(e instanceof FdlException);
        }

        // 5. get next string id with a specific
        // strategy
        try {
            uUidGenerationService.getNextStringId("mixPrefix");
        } catch (Exception e) {
            assertTrue(e instanceof FdlException);
        }

        // 6. get next string id with a specific
        // strategy
        try {
            uUidGenerationService.getNextStringId(new EgovIdGnrStrategyImpl());
        } catch (Exception e) {
            assertTrue(e instanceof FdlException);
        }
    }
}
