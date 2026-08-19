package org.egovframe.rte.psl.orm.ibatis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
public class SqlMapClientFactoryBeanTest {

    /**
     * setConfigLocation(null)과 buildSqlMapClient()가 이미 null을 '미설정'으로 다루는데
     * 배열 setter 두 개만 null에서 NPE를 내던 것을, 같은 미설정 상태로 받도록 가드를 추가했다.
     */
    @Test
    public void testSetLocationsNull() {
        SqlMapClientFactoryBean factoryBean = new SqlMapClientFactoryBean();
        factoryBean.setConfigLocations(null);
        factoryBean.setMappingLocations(null);

        assertThrows(IllegalArgumentException.class, factoryBean::afterPropertiesSet);
    }

}
