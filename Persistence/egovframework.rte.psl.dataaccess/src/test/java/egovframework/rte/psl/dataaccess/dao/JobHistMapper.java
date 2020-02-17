package egovframework.rte.psl.dataaccess.dao;

import java.util.List;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;
import egovframework.rte.psl.dataaccess.vo.JobHistVO;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

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
