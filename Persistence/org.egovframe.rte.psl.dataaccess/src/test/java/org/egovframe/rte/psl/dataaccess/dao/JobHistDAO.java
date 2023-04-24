package org.egovframe.rte.psl.dataaccess.dao;

import org.egovframe.rte.psl.dataaccess.EgovAbstractDAO;
import org.egovframe.rte.psl.dataaccess.vo.JobHistVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("jobHistDAO")
public class JobHistDAO extends EgovAbstractDAO {

	@SuppressWarnings("deprecation")
	public JobHistVO selectJobHist(String queryId, JobHistVO vo) {
		return (JobHistVO) getSqlMapClientTemplate().queryForObject(queryId, vo);
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<JobHistVO> selectJobHistList(String queryId, JobHistVO vo) {
		return getSqlMapClientTemplate().queryForList(queryId, vo);
	}

}
