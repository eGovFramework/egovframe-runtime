package org.egovframe.rte.psl.orm.ibatis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
public class SqlMapClientFactoryBeanTest {

    /**
     * 배열 setter null : NPE 없이 '미설정'으로 처리 (setConfigLocation(null)과 동일).
     */
    @Test
    public void testSetLocationsNull() {
        SqlMapClientFactoryBean factoryBean = new SqlMapClientFactoryBean();
        factoryBean.setConfigLocations(null);
        factoryBean.setMappingLocations(null);

        assertThrows(IllegalArgumentException.class, factoryBean::afterPropertiesSet);
    }

}
