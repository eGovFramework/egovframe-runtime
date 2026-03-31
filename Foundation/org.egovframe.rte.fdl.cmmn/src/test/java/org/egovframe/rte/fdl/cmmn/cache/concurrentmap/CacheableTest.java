package org.egovframe.rte.fdl.cmmn.cache.concurrentmap;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.cache.Employee;
import org.egovframe.rte.fdl.cmmn.config.ConcurrentMapCacheConfig;
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
@ContextConfiguration(classes = ConcurrentMapCacheConfig.class)
public class CacheableTest {

    @Resource(name = "concurrentmapCacheableService")
    CacheableService cacheableService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testGetCache() {
        String employeeNum = "01";
        Employee employee = cacheableService.cacheableMethod(employeeNum);

        Cache cache = cacheManager.getCache("concurrentMap");
        ValueWrapper value = cache.get(employeeNum);

        assertEquals(employee.getName(), ((Employee) value.get()).getName());
    }

}
