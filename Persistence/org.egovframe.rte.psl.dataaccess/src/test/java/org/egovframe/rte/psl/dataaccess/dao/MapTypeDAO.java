package org.egovframe.rte.psl.dataaccess.dao;

import org.egovframe.rte.psl.dataaccess.EgovAbstractDAO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("mapTypeDAO")
@SuppressWarnings("deprecation")
public class MapTypeDAO extends EgovAbstractDAO {

	public void insertDept(String queryId, Map<String, Object> map) {
		getSqlMapClientTemplate().insert(queryId, map);
	}

	public int updateDept(String queryId, Map<String, Object> map) {
		return getSqlMapClientTemplate().update(queryId, map);
	}

	public int deleteDept(String queryId, Map<String, Object> map) {
		return getSqlMapClientTemplate().delete(queryId, map);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectDept(String queryId, Map<String, Object> map) {
		return (Map<String, Object>) getSqlMapClientTemplate().queryForObject(queryId, map);
	}

	@SuppressWarnings("unchecked")
	public List<Map<?, ?>> selectDeptList(String queryId, Map<?, ?> searchMap) {
		return getSqlMapClientTemplate().queryForList(queryId, searchMap);
	}
}
