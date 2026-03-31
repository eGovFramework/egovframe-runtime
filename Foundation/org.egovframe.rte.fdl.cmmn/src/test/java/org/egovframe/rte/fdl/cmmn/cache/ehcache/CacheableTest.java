package org.egovframe.rte.fdl.cmmn.cache.ehcache;

import org.egovframe.rte.fdl.cmmn.cache.Employee;
import org.egovframe.rte.fdl.cmmn.config.EhcacheConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EhcacheConfig.class, CacheableServiceImpl.class})
public class CacheableTest {

    @Autowired
    private CacheableService cacheableService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testGetCache() {
        String employeeNum = "01";

        // 메서드 호출 -> 캐시 저장
        Employee employee = cacheableService.cacheableMethod(employeeNum);

        // 캐시 확인
        Cache cache = cacheManager.getCache("ehcache");
        ValueWrapper value = cache.get(employeeNum);

        assertEquals(employee.getName(), ((Employee) value.get()).getName());
    }

}
