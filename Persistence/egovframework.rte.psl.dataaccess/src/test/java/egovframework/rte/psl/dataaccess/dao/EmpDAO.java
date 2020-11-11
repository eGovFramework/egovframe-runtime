package egovframework.rte.psl.dataaccess.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import egovframework.rte.psl.dataaccess.EgovAbstractDAO;
import egovframework.rte.psl.dataaccess.vo.DeptIncludesEmpListVO;
import egovframework.rte.psl.dataaccess.vo.DeptVO;
import egovframework.rte.psl.dataaccess.vo.EmpDeptSimpleCompositeVO;
import egovframework.rte.psl.dataaccess.vo.EmpExtendsDeptVO;
import egovframework.rte.psl.dataaccess.vo.EmpIncludesDeptVO;
import egovframework.rte.psl.dataaccess.vo.EmpIncludesEmpListVO;
import egovframework.rte.psl.dataaccess.vo.EmpIncludesMgrVO;
import egovframework.rte.psl.dataaccess.vo.EmpVO;

import egovframework.rte.psl.orm.ibatis.SqlMapClientCallback;
import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapExecutor;

@SuppressWarnings("deprecation")
@Repository("empDAO")
public class EmpDAO extends EgovAbstractDAO {

	public void insertEmp(String queryId, EmpVO vo) {
		getSqlMapClientTemplate().insert(queryId, vo);
	}

	public BigDecimal insertEmpUsingSelectKey(String queryId, EmpVO vo) {
		return (BigDecimal) getSqlMapClientTemplate().insert(queryId, vo);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Integer batchInsertEmp(final String queryId, final List<EmpVO> list) {
		return (Integer) getSqlMapClientTemplate().execute(new SqlMapClientCallback() {
			public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {
				Iterator<EmpVO> itr = list.iterator();

				executor.startBatch();
				while (itr.hasNext()) {
					executor.insert(queryId, itr.next());
				}
				// autoboxing
				return executor.executeBatch();
			}
		});
	}

	public EmpVO selectEmp(String queryId, EmpVO vo) {
		return (EmpVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

	@SuppressWarnings("unchecked")
	public List<EmpVO> selectEmpList(String queryId, EmpVO vo) {
		return getSqlMapClientTemplate().queryForList(queryId, vo);
	}

	public EmpExtendsDeptVO selectEmpExtendsDept(String queryId, EmpVO vo) {
		return (EmpExtendsDeptVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

	public EmpDeptSimpleCompositeVO selectEmpDeptSimpleComposite(String queryId, EmpVO vo) {
		return (EmpDeptSimpleCompositeVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

	public EmpIncludesDeptVO selectEmpDeptComplexProperties(String queryId, EmpVO vo) {
		return (EmpIncludesDeptVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

	public DeptIncludesEmpListVO selectDeptEmpListComplexProperties(String queryId, DeptVO vo) {
		return (DeptIncludesEmpListVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

	@SuppressWarnings("unchecked")
	public List<DeptIncludesEmpListVO> selectDeptEmpListComplexPropertiesList(String queryId, DeptVO vo) {
		return getSqlMapClientTemplate().queryForList(queryId, vo);
	}

	public EmpIncludesMgrVO selectEmpMgrHierarchy(String queryId, EmpVO vo) {
		return (EmpIncludesMgrVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

	public EmpIncludesEmpListVO selectEmpIncludesEmpList(String queryId, EmpVO vo) {
		return (EmpIncludesEmpListVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}
}
