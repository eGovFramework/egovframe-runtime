package org.egovframe.rte.fdl.cmmn.aspectj;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, SampleAspect.class}, loader = AnnotationConfigContextLoader.class)
public class AspectjJunitTest {

    @Autowired
    private SampleService sampleService;

    @BeforeAll
    public static void setUp() {
    }

    @AfterAll
    public static void afterTest() {
    }

    @Test
    public void testSampleService() {
        assertTrue(sampleService.getClass().toString().contains("class org.egovframe.rte.fdl.cmmn.aspectj.SampleServiceImpl"));
    }

    @Test
    public void testSampleServiceGetAccountDescription() {
        assertTrue(sampleService.getOrderDescription().contains("Description:"));
    }

    @Test
    public void testSampleServiceGetAccountCode() {
        assertTrue(sampleService.getOrderStringCode().contains("Code:"));
    }

    @Test
    public void testSampleServiceCreateNewOrder() {
        Order newOrder = new Order();
        newOrder.setSecurityCode("XYZ");
        newOrder.setDescription("Description");

        Object createdOrder = sampleService.createOrder(newOrder);
        assertTrue(createdOrder instanceof Order);
        assertNotNull(newOrder.getSecurityCode(), "Security isn't null");
        assertNotNull(newOrder.getDescription(), "Description isn't not null");
        assertNotNull(newOrder, "New Order is not null");
    }

    @Test
    public void testSampleServiceGetOrder() {
        Order existingOrder = sampleService.getOrder(0);
        assertTrue(existingOrder instanceof Order);
        assertNotNull(existingOrder.getSecurityCode(), "Security isn't null");
        assertNotNull(existingOrder.getDescription(), "Description isn't null");
        assertNotNull(existingOrder, "Object is not null");
    }
}
