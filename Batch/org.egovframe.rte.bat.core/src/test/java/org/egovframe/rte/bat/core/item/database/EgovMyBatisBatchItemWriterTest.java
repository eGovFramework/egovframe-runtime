package org.egovframe.rte.bat.core.item.database;

import org.egovframe.rte.bat.sample.domain.trade.Trade;
import org.egovframe.rte.bat.support.EgovJobVariableListener;
import org.egovframe.rte.bat.support.EgovResourceVariable;
import org.egovframe.rte.bat.support.EgovStepVariableListener;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * EgovMyBatisBatchItemWriter JUnit Test 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2026.07.17  배치실행개발팀   최초 생성
 * </pre>
 * @since 2026.07.17
 */
class EgovMyBatisBatchItemWriterTest {

    @Test
    void mergeSharedVariables_returnsItemUnchanged_whenNoSharedVariableConfigured() {
        EgovMyBatisBatchItemWriter<Trade> writer = new EgovMyBatisBatchItemWriter<>();
        Trade item = new Trade("ISIN001", 100L, new BigDecimal("10.5"), "customer1");

        Object parameter = writer.mergeSharedVariables(item);

        assertSame(item, parameter);
    }

    @Test
    void mergeSharedVariables_mergesResourceVariableIntoItemProperties() {
        EgovMyBatisBatchItemWriter<Trade> writer = new EgovMyBatisBatchItemWriter<>();
        EgovResourceVariable resourceVariable = new EgovResourceVariable();
        resourceVariable.setVariable("batchDate", "20260717");
        writer.setResourceVariable(resourceVariable);

        Trade item = new Trade("ISIN001", 100L, new BigDecimal("10.5"), "customer1");
        item.setId(1L);

        Object parameter = writer.mergeSharedVariables(item);

        assertInstanceOf(Map.class, parameter);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) parameter;
        assertEquals("20260717", map.get("batchDate"));
        assertEquals("customer1", map.get("customer"));
        assertEquals("ISIN001", map.get("isin"));
    }

    @Test
    void mergeSharedVariables_itemPropertyTakesPriorityOverSharedVariableOnKeyCollision() {
        EgovMyBatisBatchItemWriter<Trade> writer = new EgovMyBatisBatchItemWriter<>();
        EgovResourceVariable resourceVariable = new EgovResourceVariable();
        resourceVariable.setVariable("customer", "shared-customer");
        writer.setResourceVariable(resourceVariable);

        Trade item = new Trade("ISIN001", 100L, new BigDecimal("10.5"), "item-customer");
        item.setId(1L);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) writer.mergeSharedVariables(item);

        assertEquals("item-customer", map.get("customer"));
    }

    @Test
    void mergeSharedVariables_stepVariableOverridesJobVariableOverridesResourceVariable() {
        EgovMyBatisBatchItemWriter<Trade> writer = new EgovMyBatisBatchItemWriter<>();

        EgovResourceVariable resourceVariable = new EgovResourceVariable();
        resourceVariable.setVariable("region", "from-resource");
        writer.setResourceVariable(resourceVariable);

        Properties jobPros = new Properties();
        jobPros.setProperty("region", "from-job");
        EgovJobVariableListener jobVariable = new EgovJobVariableListener();
        jobVariable.setPros(jobPros);
        writer.setJobVariable(jobVariable);

        Properties stepPros = new Properties();
        stepPros.setProperty("region", "from-step");
        EgovStepVariableListener stepVariable = new EgovStepVariableListener();
        stepVariable.setPros(stepPros);
        writer.setStepVariable(stepVariable);

        Trade item = new Trade("ISIN001", 100L, new BigDecimal("10.5"), "customer1");
        item.setId(1L);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) writer.mergeSharedVariables(item);

        assertEquals("from-step", map.get("region"));
    }

}
