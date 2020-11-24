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
package org.egovframe.rte.fdl.security.securedobject.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.fdl.security.config.SecuredObjectConfig;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

/**
 * DB기반의 보호된 자원 관리를 구현한 DAO 클래스
 * 
 * <p><b>NOTE:</b> DB 기반의 Secured Object 정보를 제공하기 위한 DAO 로
 * default 쿼리를 제공하며 사용자 DB 에 맞는 각 유형의 DB 쿼리는 재설정 가능하다. 
 * namedParameterJdbcTemplate 를 사용하여 DB 조회를 처리한다.</p>
 * 
 * @author ByungHun Woo
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01   윤성종         		최초 생성
 * 2014.01.22   한성곤		        Spring Security 3.2.X 업그레이드 적용, 설정 간소화 처리 관련 변경
 * 2014.07.03   한성곤		        RegexRequestMatcher의 equals 미적용에 따른 Map 검색 문제 해결 (RegexRequestMatcher -> SelfRegexRequestMatcher 변경)
 * </pre>
 */
public class SecuredObjectDAO implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecuredObjectDAO.class);

    /**
     * url 형식인 보호자원 - Role 맵핑정보를 조회하는 default 쿼리이다.
     */
    public static final String DEF_ROLES_AND_URL_QUERY =
        "SELECT a.resource_pattern url, b.authority authority "
            + "FROM SECURED_RESOURCES a, SECURED_RESOURCES_ROLE b "
            + "WHERE a.resource_id = b.resource_id "
            + "AND a.resource_type = 'url' ORDER BY a.sort_order ";

    /**
     * method 형식인 보호자원 - Role 맵핑정보를 조회하는 default 쿼리이다.
     */
    public static final String DEF_ROLES_AND_METHOD_QUERY =
        "SELECT a.resource_pattern method, b.authority authority "
            + "FROM SECURED_RESOURCES a, SECURED_RESOURCES_ROLE b "
            + "WHERE a.resource_id = b.resource_id "
            + "AND a.resource_type = 'method' ORDER BY a.sort_order ";

    /**
     * pointcut 형식인 보호자원 - Role 맵핑정보를 조회하는 default 쿼리이다.
     */
    public static final String DEF_ROLES_AND_POINTCUT_QUERY =
        "SELECT a.resource_pattern pointcut, b.authority authority "
            + "FROM SECURED_RESOURCES a, SECURED_RESOURCES_ROLE b "
            + "WHERE a.resource_id = b.resource_id "
            + "AND a.resource_type = 'pointcut' ORDER BY a.sort_order ";

    /**
     * 매 request 마다 best matching url 보호자원 - Role 맵핑정보를 얻기위한 default 쿼리이다.
     * (Oracle 10g specific)
     */
    public static final String DEF_REGEX_MATCHED_REQUEST_MAPPING_QUERY_ORACLE10G =
        "SELECT a.resource_pattern uri, b.authority authority "
            + "FROM secured_resources a, secured_resources_role b "
            + "WHERE a.resource_id = b.resource_id "
            + "AND a.resource_id =  "
            + " ( SELECT resource_id FROM "
            + "    ( SELECT resource_id, ROW_NUMBER() OVER (ORDER BY sort_order) resource_order FROM secured_resources c "
            + "      WHERE REGEXP_LIKE ( :url, c.resource_pattern ) "
            + "      AND c.resource_type = 'url' "
            + "      ORDER BY c.sort_order ) "
            + "   WHERE resource_order = 1 ) ";

    /**
     * Role 의 계층(Hierarchy) 관계를 조회하는 default 쿼리이다.
     */
    public static final String DEF_HIERARCHICAL_ROLES_QUERY =
        "SELECT a.child_role child, a.parent_role parent "
            + "FROM ROLES_HIERARCHY a LEFT JOIN ROLES_HIERARCHY b on (a.child_role = b.parent_role) ";

    private String sqlRolesAndUrl;

    private String sqlRolesAndMethod;

    private String sqlRolesAndPointcut;

    private String sqlRegexMatchedRequestMapping;

    private String sqlHierarchicalRoles;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ApplicationContext context;

    public SecuredObjectDAO() {
		this.sqlRolesAndUrl = DEF_ROLES_AND_URL_QUERY;
		this.sqlRolesAndMethod = DEF_ROLES_AND_METHOD_QUERY;
		this.sqlRolesAndPointcut = DEF_ROLES_AND_POINTCUT_QUERY;
		this.sqlRegexMatchedRequestMapping = DEF_REGEX_MATCHED_REQUEST_MAPPING_QUERY_ORACLE10G;
		this.sqlHierarchicalRoles = DEF_HIERARCHICAL_ROLES_QUERY;
    }

    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@PostConstruct
	public void securedObjectConfigInit() {
		if (context.getBeanNamesForType(SecuredObjectConfig.class).length > 0) {
			SecuredObjectConfig config = context.getBean(SecuredObjectConfig.class);

			if (StringUtils.hasText(config.getSqlRolesAndUrl())) {
				sqlRolesAndUrl = config.getSqlRolesAndUrl();
			}

			if (StringUtils.hasText(config.getSqlRolesAndMethod())) {
				sqlRolesAndMethod = config.getSqlRolesAndMethod();
			}

			if (StringUtils.hasText(config.getSqlRolesAndPointcut())) {
				sqlRolesAndPointcut = config.getSqlRolesAndPointcut();
			}

			if (StringUtils.hasText(config.getSqlRegexMatchedRequestMapping())) {
				sqlRegexMatchedRequestMapping = config.getSqlRegexMatchedRequestMapping();
			}

			if (StringUtils.hasText(config.getSqlHierarchicalRoles())) {
				sqlHierarchicalRoles = config.getSqlHierarchicalRoles();
			}
		}
	}

    /**
     * 롤에 대한 URL 정보를 가져오는 SQL을 얻어온다.
     * @return
     */
    public String getSqlRolesAndUrl() {
        return sqlRolesAndUrl;
    }

    /**
     * 롤에대한 URL 정보를 가져오는 SQL을 설정한다.
     * @param sqlRolesAndUrl
     */
    public void setSqlRolesAndUrl(String sqlRolesAndUrl) {
        this.sqlRolesAndUrl = sqlRolesAndUrl;
    }

    /**
     *  롤에 대한 메소드 정보를 가져오는 SQL을 얻어온다.
     * @return
     */
    public String getSqlRolesAndMethod() {
        return sqlRolesAndMethod;
    }

    /**
     * 롤에 대한 메소드 정보를 가져오는 SQL을 설정한다.
     * @param sqlRolesAndMethod
     */
    public void setSqlRolesAndMethod(String sqlRolesAndMethod) {
        this.sqlRolesAndMethod = sqlRolesAndMethod;
    }

    /**
     * 롤에 대한 포인트컷 정보를 가져오는 SQL을 얻어온다.
     * @return
     */
    public String getSqlRolesAndPointcut() {
        return sqlRolesAndPointcut;
    }

    /**
     * 롤에 대한 포인트컷 정보를 가져오는 SQL을 설정한다.
     * @param sqlRolesAndPointcut
     */
    public void setSqlRolesAndPointcut(String sqlRolesAndPointcut) {
        this.sqlRolesAndPointcut = sqlRolesAndPointcut;
    }

    /**
     *  매핑 정보를 가져오는 SQL을 얻어온다.
     * @return
     */
    public String getSqlRegexMatchedRequestMapping() {
        return sqlRegexMatchedRequestMapping;
    }

    /**
     * 매핑 정보를 가져오는 SQL을 설정한다.
     * @param sqlRegexMatchedRequestMapping
     */
    public void setSqlRegexMatchedRequestMapping(String sqlRegexMatchedRequestMapping) {
        this.sqlRegexMatchedRequestMapping = sqlRegexMatchedRequestMapping;
    }

    /**
     * 롤 계층구조 정보를 가져오는 SQL을 얻어온다.
     * @return
     */
    public String getSqlHierarchicalRoles() {
        return sqlHierarchicalRoles;
    }

    /**
     * 롤 계층구조 정보를 가져오는 SQL을 설정한다.
     * @param sqlHierarchicalRoles
     */
    public void setSqlHierarchicalRoles(String sqlHierarchicalRoles) {
        this.sqlHierarchicalRoles = sqlHierarchicalRoles;
    }

    /**
     * 리소스 유형에 대한 할당된 롤 정보를 가져온다.
     *
     * @param resourceType
     * @return
     * @throws Exception
     */
    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndResources(String resourceType, String requestMatcherType) throws Exception {

        LinkedHashMap<Object, List<ConfigAttribute>> resourcesMap = new LinkedHashMap<Object, List<ConfigAttribute>>();

        String sqlRolesAndResources;
        boolean isResourcesUrl = true;

        if ("method".equals(resourceType)) {
            sqlRolesAndResources = getSqlRolesAndMethod();
            isResourcesUrl = false;
        } else if ("pointcut".equals(resourceType)) {
            sqlRolesAndResources = getSqlRolesAndPointcut();
            isResourcesUrl = false;
        } else {
            sqlRolesAndResources = getSqlRolesAndUrl();
        }

        List<Map<String, Object>> resultList = this.namedParameterJdbcTemplate.queryForList(sqlRolesAndResources, new HashMap<String, String>());

        Iterator<Map<String, Object>> itr = resultList.iterator();
        Map<String, Object> tempMap;
        String preResource = null;
        String presentResourceStr;
        Object presentResource;
        while (itr.hasNext()) {
            tempMap = itr.next();
            presentResourceStr = (String) tempMap.get(resourceType);
            // url 인 경우 RequestKey 형식의 key를 Map에 담아야 함
            if (isResourcesUrl) {
            	if (requestMatcherType.equalsIgnoreCase("regex")) {
            		presentResource = new SelfRegexRequestMatcher(presentResourceStr, null);
            	} else if (requestMatcherType.equalsIgnoreCase("ciRegex")) {
            		presentResource = new SelfRegexRequestMatcher(presentResourceStr, null, true);
            	} else {
            		presentResource = new AntPathRequestMatcher(presentResourceStr);
            	}
            } else {
            	presentResource = presentResourceStr;
            }

            List<ConfigAttribute> configList = new LinkedList<ConfigAttribute>();

            // 이미 requestMap 에 해당 Resource 에 대한 Role 이 하나 이상 맵핑되어 있었던 경우,
            // sort_order 는 resource(Resource) 에 대해 매겨지므로 같은 Resource 에 대한 Role 맵핑은 연속으로 조회됨.
            // 해당 맵핑 Role List (SecurityConfig) 의 데이터를 재활용하여 새롭게 데이터 구축
            if (preResource != null && presentResourceStr.equals(preResource)) {
                List<ConfigAttribute> preAuthList = resourcesMap.get(presentResource);
                Iterator<ConfigAttribute> preAuthItr = preAuthList.iterator();
                while (preAuthItr.hasNext()) {
                    SecurityConfig tempConfig = (SecurityConfig) preAuthItr.next();
                    configList.add(tempConfig);
                }
            }

            configList.add(new SecurityConfig((String) tempMap.get("authority")));

            // 만약 동일한 Resource 에 대해 한개 이상의 Role 맵핑 추가인 경우
            // 이전 resourceKey 에 현재 새로 계산된 Role 맵핑 리스트로 덮어쓰게 됨.
            resourcesMap.put(presentResource, configList);

            // 이전 resource 비교위해 저장
            preResource = presentResourceStr;
        }

        return resourcesMap;
    }

    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndUrl(String requestMatcherType) throws Exception {
        return getRolesAndResources("url", requestMatcherType);
    }

    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndMethod() throws Exception {
        return getRolesAndResources("method", null);
    }

    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndPointcut() throws Exception {
        return getRolesAndResources("pointcut", null);
    }

    public List<ConfigAttribute> getRegexMatchedRequestMapping(String url) throws Exception {
        // best regex matching - best 매칭된 Uri 에 따른 Role List 조회,
    	// DB 차원의 정규식 지원이 있는 경우 사용 (ex. hsqldb custom function, Oracle 10g regexp_like 등)
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("url", url);
        List<Map<String, Object>> resultList = this.namedParameterJdbcTemplate.queryForList(getSqlRegexMatchedRequestMapping(), paramMap);

        Iterator<Map<String, Object>> itr = resultList.iterator();
        Map<String, Object> tempMap;
        List<ConfigAttribute> configList = new LinkedList<ConfigAttribute>();
        // 같은 Uri 에 대한 Role 맵핑이므로 무조건 configList 에 add 함
        while (itr.hasNext()) {
            tempMap = itr.next();
			configList.add(new SecurityConfig((String) tempMap.get("authority")));
        }

        if (configList.size() > 0) {
        	LOGGER.debug("Request Uri : {}, matched Uri : {}, mapping Roles : {}", url, resultList.get(0).get("uri"), configList);
        }

        return configList;
    }

    public String getHierarchicalRoles() throws Exception {
    	List<Map<String, Object>> resultList = this.namedParameterJdbcTemplate.queryForList(getSqlHierarchicalRoles(), new HashMap<String, String>());

        Iterator<Map<String, Object>> itr = resultList.iterator();
        StringBuffer concatedRoles = new StringBuffer();
        Map<String, Object> tempMap;
        while (itr.hasNext()) {
            tempMap = itr.next();
            concatedRoles.append(tempMap.get("child"));
            concatedRoles.append(" > ");
            concatedRoles.append(tempMap.get("parent"));
            concatedRoles.append("\n");
        }

        return concatedRoles.toString();
    }

}
