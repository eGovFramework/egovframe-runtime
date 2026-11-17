package org.egovframe.rte.bat.core.item.database;

import org.egovframe.rte.bat.domain.CustomerCredit;
import org.egovframe.rte.bat.support.EgovResourceVariable;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * EgovMyBatisBatchItemWriter 공유 변수 병합 JUnit Test 클래스 (PR #490 반영)
 */
public class EgovMyBatisBatchItemWriterTest {

    @Test
    public void mergeSharedVariables_mergesSharedVariableIntoMapItemEntries() throws Exception {
        EgovMyBatisBatchItemWriter<Map<String, Object>> writer = new EgovMyBatisBatchItemWriter<Map<String, Object>>();
        EgovResourceVariable resourceVariable = new EgovResourceVariable();
        resourceVariable.setVariable("batchDate", "20260717");
        writer.setResourceVariable(resourceVariable);

        Map<String, Object> item = new HashMap<String, Object>();
        item.put("isin", "ISIN001");
        item.put("qty", 100);

        Map<String, Object> map = writer.mergeSharedVariables(item);

        assertEquals("20260717", map.get("batchDate"));
        assertEquals("ISIN001", map.get("isin"));
        assertEquals(100, map.get("qty"));
    }

    @Test
    public void mergeSharedVariables_mapItemEntryTakesPriorityOverSharedVariableOnKeyCollision() throws Exception {
        EgovMyBatisBatchItemWriter<Map<String, Object>> writer = new EgovMyBatisBatchItemWriter<Map<String, Object>>();
        EgovResourceVariable resourceVariable = new EgovResourceVariable();
        resourceVariable.setVariable("customer", "shared-customer");
        writer.setResourceVariable(resourceVariable);

        Map<String, Object> item = new HashMap<String, Object>();
        item.put("customer", "item-customer");

        assertEquals("item-customer", writer.mergeSharedVariables(item).get("customer"));
    }

    @Test
    public void mergeSharedVariables_doesNotAddBeanPropertiesOfMapItem() throws Exception {
        EgovMyBatisBatchItemWriter<Map<String, Object>> writer = new EgovMyBatisBatchItemWriter<Map<String, Object>>();
        EgovResourceVariable resourceVariable = new EgovResourceVariable();
        resourceVariable.setVariable("batchDate", "20260717");
        writer.setResourceVariable(resourceVariable);

        Map<String, Object> item = new HashMap<String, Object>();
        item.put("isin", "ISIN001");

        Map<String, Object> map = writer.mergeSharedVariables(item);

        assertFalse(map.containsKey("class"));
        assertFalse(map.containsKey("empty"));
    }

    @Test
    public void mergeSharedVariables_keepsMapItemEntries_whenNoSharedVariableConfigured() throws Exception {
        // 4.3.1 은 공유 변수가 없어도 항상 Map 으로 변환하므로, 이 경우에도 item 엔트리가 유지되어야 한다
        EgovMyBatisBatchItemWriter<Map<String, Object>> writer = new EgovMyBatisBatchItemWriter<Map<String, Object>>();
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("isin", "ISIN001");

        Map<String, Object> map = writer.mergeSharedVariables(item);

        assertEquals(item, map);
    }

    @Test
    public void mergeSharedVariables_ignoresNonStringKeysOfMapItem() throws Exception {
        EgovMyBatisBatchItemWriter<Map<Object, Object>> writer = new EgovMyBatisBatchItemWriter<Map<Object, Object>>();
        Map<Object, Object> item = new HashMap<Object, Object>();
        item.put("isin", "ISIN001");
        item.put(1, "numeric-key");

        Map<String, Object> map = writer.mergeSharedVariables(item);

        assertEquals(1, map.size());
        assertEquals("ISIN001", map.get("isin"));
    }

    @Test
    public void mergeSharedVariables_beanItemPropertiesTakePriorityOverSharedVariable() throws Exception {
        EgovMyBatisBatchItemWriter<CustomerCredit> writer = new EgovMyBatisBatchItemWriter<CustomerCredit>();
        EgovResourceVariable resourceVariable = new EgovResourceVariable();
        resourceVariable.setVariable("batchDate", "20260717");
        resourceVariable.setVariable("name", "shared-name");
        writer.setResourceVariable(resourceVariable);

        Map<String, Object> map = writer.mergeSharedVariables(new CustomerCredit(1, "customer1", new BigDecimal("100")));

        assertEquals("20260717", map.get("batchDate"));
        assertEquals("customer1", map.get("name"));
        assertEquals(new BigDecimal("100"), map.get("credit"));
        assertEquals(1, map.get("id"));
    }

}
