/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.rte.psl.dataaccess;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import egovframework.rte.psl.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Spring 의 iBatis 연동 지원을 Annotation 형식으로 쉽게 처리하기 위한 공통 parent DAO 클래스
 * <p>
 * <b>NOTE</b>: Spring 에서 iBatis 연동을 지원하는 org.springframework.orm.ibatis.support.SqlMapClientDaoSupport을
 * extends 하고 있으며 CRUD 와 관련한 대표적인 method 를 간단하게 호출할 수 있도록 Wrapping 하고 있어 사용자 DAO에서
 * iBatis sqlMapClient 호출을 쉽게 하며 Bean 생성 시 Annotation 기반으로 sqlMapClient 을 쉽게 injection 할 수 있는
 * 공통 로직을 포함하고 있다.
 * </p>
 * @author 실행환경 개발팀 우병훈
 * @author Vincent Han
 *
 * @since 1.0
 *
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      	수정자           수정내용
 *  ------------   --------    ---------------------------
 *   2009.02.25		우병훈		최초 생성
 *   2013.11.21		유지보수	listWithPaging 오류 수정
 *   2014.07.11 	이기하  	selectByPk Deprecated 및 getSqlMapClientTemplate 메소드 추가
 *
 * </pre>
 */
@SuppressWarnings("deprecation")
public abstract class EgovAbstractDAO extends SqlMapClientDaoSupport {

	/**
	 * EgovAbstractDAO 는 base class 로만 사용되며 해당 인스턴스를 직접 생성할 수 없도록 protected constructor 로 선언하였음.
	 */
	protected EgovAbstractDAO() {
		// PMD abstract Rule
		// - If the class is intended to be used as a base class only (not to be instantiated directly)
		// a protected constructor can be provided prevent direct instantiation
	}

	/**
	 * Annotation 형식으로 sqlMapClient 를 받아와 이를 super(SqlMapClientDaoSupport) 의 setSqlMapClient 메서드를 호출하여 설정해 준다.
	 *
	 * @param sqlMapClient - ibatis 의 SQL Map 과의 상호작용을 위한 기본 클래스로
	 *               mapped statements(select, insert, update, delete 등)의 실행을 지원함.
	 */
	@Resource(name = "sqlMapClient")
	public void setSuperSqlMapClient(SqlMapClient sqlMapClient) {
		super.setSqlMapClient(sqlMapClient);
	}

	/**
	 * 입력 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 입력 처리 SQL mapping 쿼리 ID
	 * @return 입력 시 selectKey 를 사용하여 key 를 딴 경우 해당 key
	 */
	public Object insert(String queryId) {
		return getSqlMapClientTemplate().insert(queryId);
	}

	/**
	 * 입력 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 입력 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 입력 처리 SQL mapping 입력 데이터를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @return 입력 시 selectKey 를 사용하여 key 를 딴 경우 해당 key
	 */
	public Object insert(String queryId, Object parameterObject) {
		return getSqlMapClientTemplate().insert(queryId, parameterObject);
	}

	/**
	 * 수정 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 수정 처리 SQL mapping 쿼리 ID
	 * @return DBMS가 지원하는 경우 update 적용 결과 count
	 */
	public int update(String queryId) {
		return getSqlMapClientTemplate().update(queryId);
	}

	/**
	 * 수정 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 수정 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 수정 처리 SQL mapping 입력 데이터(key 조건 및 변경 데이터)를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @return DBMS가 지원하는 경우 update 적용 결과 count
	 */
	public int update(String queryId, Object parameterObject) {
		return getSqlMapClientTemplate().update(queryId, parameterObject);
	}

	/**
	 * 수정 처리 SQL mapping 을 실행한다.
	 * 반환값이 없는 대신 예상 결과행 수와 맞지 않으면 예외 오류 발생한다.
	 *
	 * @param queryId - 수정 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 수정 처리 SQL mapping 입력 데이터(key 조건 및 변경 데이터)를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @param requiredRowsAffected - 수정할 row 수
	 */
	public void update(String queryId, Object parameterObject, int requiredRowsAffected) {
		getSqlMapClientTemplate().update(queryId, parameterObject, requiredRowsAffected);
	}

	/**
	 * 삭제 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 삭제 처리 SQL mapping 쿼리 ID
	 * @return DBMS가 지원하는 경우 delete 적용 결과 count
	 */
	public int delete(String queryId) {
		return getSqlMapClientTemplate().delete(queryId);
	}

	/**
	 * 삭제 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 삭제 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 삭제 처리 SQL mapping 입력 데이터(일반적으로 key 조건)를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @return DBMS가 지원하는 경우 delete 적용 결과 count
	 */
	public int delete(String queryId, Object parameterObject) {
		return getSqlMapClientTemplate().delete(queryId, parameterObject);
	}

	/**
	 * 삭제 처리 SQL mapping 을 실행한다.
	 * 반환값이 없는 대신 예상 결과행 수와 맞지 않으면 예외 오류 발생한다.
	 *
	 * @param queryId - 삭제 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 삭제 처리 SQL mapping 입력 데이터(일반적으로 key 조건)를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @param requiredRowsAffected - 삭제할 row 수
	 * @return DBMS가 지원하는 경우 delete 적용 결과 count
	 */
	public void delete(String queryId, Object parameterObject, int requiredRowsAffected) {
		getSqlMapClientTemplate().delete(queryId, parameterObject, requiredRowsAffected);
	}

	//CHECKSTYLE:OFF
	/**
	 * 명명규칙에 맞춰 select()로 변경한다.
	 * @deprecated select() 메소드로 대체
	 * 
	 * @see EgovAbstractDAO.select
	 */
	//CHECKSTYLE:ON
	@Deprecated
	public Object selectByPk(String queryId, Object parameterObject) {
		return getSqlMapClientTemplate().queryForObject(queryId, parameterObject);
	}

	/**
	 * 단건조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 단건 조회 처리 SQL mapping 쿼리 ID
	 * @return 결과 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 단일 결과 객체(보통 VO 또는 Map)
	 */
	public Object select(String queryId) {
		return getSqlMapClientTemplate().queryForObject(queryId);
	}

	/**
	 * 단건조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 단건 조회 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 단건 조회 처리 SQL mapping 입력 데이터(key)를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @return 결과 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 단일 결과 객체(보통 VO 또는 Map)
	 */
	public Object select(String queryId, Object parameterObject) {
		return getSqlMapClientTemplate().queryForObject(queryId, parameterObject);
	}

	/**
	 * 단건조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 단건 조회 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 단건 조회 처리 SQL mapping 입력 데이터(key)를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @param resultObject - 특정 오브젝트로 결과값을 반환할 경우
	 * @return 결과 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 단일 결과 객체(보통 VO 또는 Map)
	 */
	public Object select(String queryId, Object parameterObject, final Object resultObject) {
		return getSqlMapClientTemplate().queryForObject(queryId, parameterObject, resultObject);
	}

	/**
	 * 리스트 조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 리스트 조회 처리 SQL mapping 쿼리 ID
	 * @return 결과 List 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 결과 객체(보통 VO 또는 Map)의 List
	 */
	public List<?> list(String queryId) {
		return getSqlMapClientTemplate().queryForList(queryId);
	}

	/**
	 * 리스트 조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 리스트 조회 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 리스트 조회 처리 SQL mapping 입력 데이터(조회 조건)를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @return 결과 List 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 결과 객체(보통 VO 또는 Map)의 List
	 */
	public List<?> list(String queryId, Object parameterObject) {
		return getSqlMapClientTemplate().queryForList(queryId, parameterObject);
	}

	/**
	 * 리스트 조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 리스트 조회 처리 SQL mapping 쿼리 ID
	 * @param skipResults - 쿼리를 시작하는 행
	 * @param maxResults - 최대 결과를 나타내는 행
	 * @return 결과 List 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 결과 객체(보통 VO 또는 Map)의 List
	 */
	public List<?> list(String queryId, int skipResults, int maxResults) {
		return getSqlMapClientTemplate().queryForList(queryId, skipResults, maxResults);
	}

	/**
	 * 리스트 조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 리스트 조회 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 입력 처리 SQL mapping 입력 데이터를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @param skipResults - 쿼리를 시작하는 행
	 * @param maxResults - 최대 결과를 나타내는 행
	 * @return 결과 List 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 결과 객체(보통 VO 또는 Map)의 List
	 */
	public List<?> list(String queryId, final Object parameterObject, int skipResults, int maxResults) {
		return getSqlMapClientTemplate().queryForList(queryId, parameterObject, skipResults, maxResults);
	}

	/**
	 * 부분 범위 리스트 조회 처리 SQL mapping 을 실행한다.
	 * (부분 범위 - pageIndex 와 pageSize 기반으로 현재 부분 범위 조회를 위한 skipResults, maxResults 를 계산하여 ibatis 호출)
	 *
	 * @param queryId - 리스트 조회 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 리스트 조회 처리 SQL mapping 입력 데이터(조회 조건)를  세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @param pageIndex - 현재 페이지 번호
	 * @param pageSize - 한 페이지 조회 수(pageSize)
	 * @return 부분 범위 결과 List 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 부분 범위 결과 객체(보통 VO 또는 Map) List
	 */
	public List<?> listWithPaging(String queryId, Object parameterObject, int pageIndex, int pageSize) {
		int skipResults = pageIndex * pageSize;
		///int maxResults = (pageIndex * pageSize) + pageSize;
		int maxResults = pageSize;

		return getSqlMapClientTemplate().queryForList(queryId, parameterObject, skipResults, maxResults);
	}

	/**
	 * 리스트 조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 리스트 조회 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 입력 처리 SQL mapping 입력 데이터를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @param keyProperty - key값이 될 field
	 * @return 결과 List 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 결과 객체(보통 VO 또는 Map)의 Map
	 */
	public Map<?, ?> map(final String queryId, final Object parameterObject, final String keyProperty) {
		return getSqlMapClientTemplate().queryForMap(queryId, parameterObject, keyProperty);
	}

	/**
	 * 리스트 조회 처리 SQL mapping 을 실행한다.
	 *
	 * @param queryId - 리스트 조회 처리 SQL mapping 쿼리 ID
	 * @param parameterObject - 입력 처리 SQL mapping 입력 데이터를 세팅한 파라메터 객체(보통 VO 또는 Map)
	 * @param keyProperty - key값이 될 field
	 * @param valueProperty - map에 저장할 특정 field
	 * @return 결과 List 객체 - SQL mapping 파일에서 지정한 resultClass/resultMap 에 의한 결과 객체(보통 VO 또는 Map)의 Map
	 */
	public Map<?, ?> map(final String queryId, final Object parameterObject, final String keyProperty, final String valueProperty) {
		return getSqlMapClientTemplate().queryForMap(queryId, parameterObject, keyProperty, valueProperty);
	}

}
