package org.egovframe.rte.psl.dataaccess.dao;

import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;

import org.springframework.stereotype.Repository;

@Repository("mapTypeMapper")
public class MapTypeMapper extends EgovAbstractMapper {

	public void insertDept(String queryId, Map<String, Object> map) {
		insert(queryId, map);
	}

	public int updateDept(String queryId, Map<String, Object> map) {
		return update(queryId, map);
	}

	public int deleteDept(String queryId, Map<String, Object> map) {
		return delete(queryId, map);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectDept(String queryId, Map<String, Object> map) {
		return (Map<String, Object>) selectOne(queryId, map);
	}

	public List<Map<?, ?>> selectDeptList(String queryId, Map<?, ?> searchMap) {
		return selectList(queryId, searchMap);
	}
}
