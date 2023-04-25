package org.egovframe.rte.psl.dataaccess.dao;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.egovframe.rte.psl.dataaccess.vo.JobHistVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository("jobHistMapper")
public class JobHistMapper extends EgovAbstractMapper {

	@Resource(name = "batchSqlSessionTemplate")
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		super.setSqlSessionTemplate(sqlSessionTemplate);
	}

	public JobHistVO selectJobHist(String queryId, JobHistVO vo) {
		return (JobHistVO) selectList(queryId, vo);
	}

	public List<JobHistVO> selectJobHistList(String queryId, JobHistVO vo) {
		return selectList(queryId, vo);
	}

}
