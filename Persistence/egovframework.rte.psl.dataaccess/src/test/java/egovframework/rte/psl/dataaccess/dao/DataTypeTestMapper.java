package egovframework.rte.psl.dataaccess.dao;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;
import egovframework.rte.psl.dataaccess.vo.TypeTestVO;

import org.springframework.stereotype.Repository;

@Repository("dataTypeTestMapper")
public class DataTypeTestMapper extends EgovAbstractMapper {

	public void insertTypeTest(String queryId, TypeTestVO vo) {
		insert(queryId, vo);
	}

	public TypeTestVO selectTypeTest(String queryId, TypeTestVO vo) {
		return (TypeTestVO) selectOne(queryId, vo);
	}

}
