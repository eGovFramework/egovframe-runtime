package egovframework.rte.psl.dataaccess.dao;

import egovframework.rte.psl.dataaccess.EgovAbstractDAO;
import egovframework.rte.psl.dataaccess.vo.TypeTestVO;

import org.springframework.stereotype.Repository;

@Repository("typeTestDAO")
@SuppressWarnings("deprecation")
public class TypeTestDAO extends EgovAbstractDAO {

	public void insertTypeTest(String queryId, TypeTestVO vo) {
		getSqlMapClientTemplate().insert(queryId, vo);
	}

	public TypeTestVO selectTypeTest(String queryId, TypeTestVO vo) {
		return (TypeTestVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

}
