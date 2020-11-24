package org.egovframe.rte.fdl.cmmn.cache.concurrentmap;

import static org.junit.Assert.assertEquals;

import org.egovframe.rte.fdl.cmmn.cache.Employee;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/context-concurrentmap.xml" })
public class CacheableTest {

	@Autowired
	private CacheManager cacheManager;

	@Resource(name="concurrentmapCacheableService")
	CacheableService cacheableService;

	@Test
	public void testGetCache() {
		String employeeNum = "01";
		Employee employee = cacheableService.cacheableMethod(employeeNum);

		Cache cache= cacheManager.getCache("concurrentMap");
		ValueWrapper value = cache.get(employeeNum);

		assertEquals(((Employee)value.get()).getName(), employee.getName());
	}



}
