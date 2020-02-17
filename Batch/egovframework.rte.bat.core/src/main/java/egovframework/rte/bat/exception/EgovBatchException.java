package egovframework.rte.bat.exception;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;  
import egovframework.rte.fdl.cmmn.exception.BaseRuntimeException;

/**
 * EgovBatchException 클래스
 * <Notice>
 * 	    표준프레임워크 베치에서 Database 기반에서 Exception Code 
 *      기반으로 에러 메세지 기반으로 Exception을 처리하는 class    
 *      
 * <Disclaimer>
 *		N/A
 *
 * @author 장동한
 * @since 2017.12.01
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2017.12.01  장동한           최초 생성
 *   2017.12.17  장동한           sql 설정 기능 추가(생성자 파라미터 방식)
 * </pre>
 */
public class EgovBatchException extends BaseRuntimeException {
	
	/** serial id */
	private static final long serialVersionUID = -7642174304081691230L;
	
	/** sql */
	private String sEXCEPTION_MESSAGE_SQL = "SELECT EX_MESSAGE FROM BATCH_EXCEPTION_MESSAGE WHERE EX_KEY = ?";
	
	
	/**
	 * 에러코드 코드 SQL Getter
	 * 
	 * @return String EXCEPTION MESSAGE SQL
	 */
	public String getExceptionMessageSql() {
		return sEXCEPTION_MESSAGE_SQL;
	}

	/**
	 * 에러코드 코드 SQL Setter
	 * 
	 * @param sEXCEPTION_MESSAGE_SQL EXCEPTION MESSAGE SQL
	 */
	public void setExceptionMessageSql(String sEXCEPTION_MESSAGE_SQL) {
		this.sEXCEPTION_MESSAGE_SQL = sEXCEPTION_MESSAGE_SQL;
	}

	/**
	 * EgovBatchException 생성자.
	 * 
	 * @param dataSource 데이터소스
	 * @param messageKey 메세지키
	 */
	public EgovBatchException(DataSource dataSource, String messageKey) {
		this.messageKey = messageKey;
		this.message = getExceptionMessageSelect(dataSource);
		this.messageParameters = null;
		this.wrappedException = null;	
	}

	/**
	 * EgovBatchException 생성자.
	 * 
	 * @param dataSource 데이터소스
	 * @param messageKey 메세지키
	 * @param messageSql 사용자 정의 SQL
	 */
	public EgovBatchException(DataSource dataSource, String messageKey, String messageSql) {
		this.messageKey = messageKey;
		this.sEXCEPTION_MESSAGE_SQL = messageSql;
		this.message = getExceptionMessageSelect(dataSource);
		this.messageParameters = null;
		this.wrappedException = null;	
	}

	/**
	 * EgovBatchException 생성자.
	 * 
	 * @param dataSource 데이터소스
	 * @param messageKey 메세지키
	 * @param wrappedException 에러객체
	 */
	public EgovBatchException(DataSource dataSource, String messageKey, Exception wrappedException) {
		this.messageKey = messageKey;
		this.message = getExceptionMessageSelect(dataSource);
		this.wrappedException = wrappedException;
		this.messageParameters = null;
		this.wrappedException = null;
	}
	
	
	/**
	 * 데이터베이스에서 코드에대한 메세지를 가져온다.
	 * 
	 * @param dataSource 데이터소스
	 * @return String 에러메세지
	 */
	private String getExceptionMessageSelect(DataSource dataSource){
		return (String)(new JdbcTemplate(dataSource)).queryForObject(sEXCEPTION_MESSAGE_SQL, new Object[]{messageKey}, String.class);
	}
	
	
}
