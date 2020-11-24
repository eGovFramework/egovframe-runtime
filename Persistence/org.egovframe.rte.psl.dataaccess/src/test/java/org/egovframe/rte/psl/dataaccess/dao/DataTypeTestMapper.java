package org.egovframe.rte.psl.dataaccess.dao;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.egovframe.rte.psl.dataaccess.vo.TypeTestVO;

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
