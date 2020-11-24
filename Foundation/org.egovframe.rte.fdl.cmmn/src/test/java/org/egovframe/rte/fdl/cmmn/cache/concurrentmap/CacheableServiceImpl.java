package org.egovframe.rte.fdl.cmmn.cache.concurrentmap;

import org.egovframe.rte.fdl.cmmn.cache.Employee;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service(value="concurrentmapCacheableService")
public class CacheableServiceImpl implements CacheableService{

	@Cacheable("concurrentMap")
	public Employee cacheableMethod(String num) {
		Employee employee = new Employee();

		employee.setNum(num);
		employee.setName("anonymous"+num);
		return employee;
	}
}
