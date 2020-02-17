package egovframework.rte.psl.dataaccess.dao;

import java.util.List;

import egovframework.rte.psl.dataaccess.EgovAbstractDAO;
import egovframework.rte.psl.dataaccess.vo.DeptVO;

import org.springframework.stereotype.Repository;

@Repository("deptDAO")
public class DeptDAO extends EgovAbstractDAO {

	public void insertDept(String queryId, DeptVO vo) {
		insert(queryId, vo);
	}

	public int updateDept(String queryId, DeptVO vo) {
		return update(queryId, vo);
	}

	public int deleteDept(String queryId, DeptVO vo) {
		return delete(queryId, vo);
	}

	public DeptVO selectDept(String queryId, DeptVO vo) {
		return (DeptVO) select(queryId, vo);
	}

	@SuppressWarnings("unchecked")
	public List<DeptVO> selectDeptList(String queryId, DeptVO searchVO) {
		return (List<DeptVO>) list(queryId, searchVO);
	}

	@SuppressWarnings("unchecked")
	public List<DeptVO> selectDeptListWithPaging(String queryId, DeptVO searchVO, int pageIndex, int pageSize) {
		return (List<DeptVO>) listWithPaging(queryId, searchVO, pageIndex, pageSize);
	}
}
