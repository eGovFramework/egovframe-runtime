package org.egovframe.rte.psl.dataaccess.dao;

import org.apache.ibatis.session.RowBounds;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.egovframe.rte.psl.dataaccess.vo.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

@Repository("empBatchMapper")
public class EmpBatchMapper extends EgovAbstractMapper {

	@Resource(name = "batchSqlSessionTemplate")
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		super.setSqlSessionTemplate(sqlSessionTemplate);
	}

	public void insertEmp(String queryId, EmpVO vo) {
		insert(queryId, vo);
	}

	public int insertEmpUsingSelectKey(String queryId, EmpVO vo) {
		return insert(queryId, vo);
	}

	public Integer batchInsertEmp(String queryId, List<EmpVO> list) {
		Iterator<EmpVO> itr = list.iterator();

		int count = 0;

		while (itr.hasNext()) {
			count += insert(queryId, itr.next());
		}

		return count;
	}

	public EmpVO selectEmp(String queryId, EmpVO vo) {
		return (EmpVO) selectList(queryId, vo);
	}

	public List<EmpVO> selectEmpList(String queryId, EmpVO vo) {
		return selectList(queryId, vo);
	}

	public List<?> selectList(String statementName, int skipResults, int maxResults) {
		RowBounds rowBounds = new RowBounds(skipResults, maxResults);
		return selectList(statementName, "EmpVO", rowBounds);
	}

	public EmpExtendsDeptVO selectEmpExtendsDept(String queryId, EmpVO vo) {
		return (EmpExtendsDeptVO) selectList(queryId, vo);
	}

	public EmpDeptSimpleCompositeVO selectEmpDeptSimpleComposite(String queryId, EmpVO vo) {
		return (EmpDeptSimpleCompositeVO) selectList(queryId, vo);
	}

	public EmpIncludesDeptVO selectEmpDeptComplexProperties(String queryId, EmpVO vo) {
		return (EmpIncludesDeptVO) selectList(queryId, vo);
	}

	public DeptIncludesEmpListVO selectDeptEmpListComplexProperties(String queryId, DeptVO vo) {
		return (DeptIncludesEmpListVO) selectList(queryId, vo);
	}

	public List<DeptIncludesEmpListVO> selectDeptEmpListComplexPropertiesList(String queryId, DeptVO vo) {
		return selectList(queryId, vo);
	}

	public EmpIncludesMgrVO selectEmpMgrHierarchy(String queryId, EmpVO vo) {
		return (EmpIncludesMgrVO) selectList(queryId, vo);
	}

	public EmpIncludesEmpListVO selectEmpIncludesEmpList(String queryId, EmpVO vo) {
		return (EmpIncludesEmpListVO) selectList(queryId, vo);
	}
}
