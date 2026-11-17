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
package org.egovframe.boot.security.secureobject.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.*;

@Slf4j
public class SecuredObjectDAO {

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

    /**
     * 롤에 대한 URL 정보
     */
    @Setter
    @Getter
    private String sqlRolesAndUrl;

    /**
     * 롤에 대한 메소드 정보
     */
    @Setter
    @Getter
    private String sqlRolesAndMethod;

    /**
     * 롤에 대한 포인트컷 정보
     */
    @Setter
    @Getter
    private String sqlRolesAndPointcut;

    /**
     * 매핑 정보
     */
    @Setter
    @Getter
    private String sqlRegexMatchedRequestMapping;

    /**
     * 롤 계층구조 정보
     */
    @Setter
    @Getter
    private String sqlHierarchicalRoles;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private EgovSecurityProperties properties;

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

	@PostConstruct
	public void securedObjectConfigInit() {
        if (properties == null) {
            throw new NoSuchBeanDefinitionException("### SecuredObjectDAO EgovSecurityProperties is not defined");
        }

        if (StringUtils.hasText(properties.getSqlRolesAndUrl())) {
            sqlRolesAndUrl = properties.getSqlRolesAndUrl();
        }

        if (StringUtils.hasText(properties.getSqlRolesAndMethod())) {
            sqlRolesAndMethod = properties.getSqlRolesAndMethod();
        }

        if (StringUtils.hasText(properties.getSqlRolesAndPointcut())) {
            sqlRolesAndPointcut = properties.getSqlRolesAndPointcut();
        }

        if (StringUtils.hasText(properties.getSqlRegexMatchedRequestMapping())) {
            sqlRegexMatchedRequestMapping = properties.getSqlRegexMatchedRequestMapping();
        }

        if (StringUtils.hasText(properties.getSqlHierarchicalRoles())) {
            sqlHierarchicalRoles = properties.getSqlHierarchicalRoles();
        }
	}

    /**
     * 리소스 유형에 대한 할당된 롤 정보를 가져온다.
     */
    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndResources(String resourceType, String requestMatcherType) {

        LinkedHashMap<Object, List<ConfigAttribute>> resourcesMap = new LinkedHashMap<>();

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

            List<ConfigAttribute> configList = new LinkedList<>();

            // 이미 requestMap 에 해당 Resource 에 대한 Role 이 하나 이상 맵핑되어 있었던 경우,
            // sort_order 는 resource(Resource) 에 대해 매겨지므로 같은 Resource 에 대한 Role 맵핑은 연속으로 조회됨.
            // 해당 맵핑 Role List (SecurityConfig) 의 데이터를 재활용하여 새롭게 데이터 구축
            if (!ObjectUtils.isEmpty(preResource) && presentResourceStr.equals(preResource)) {
                List<ConfigAttribute> preAuthList = resourcesMap.get(presentResource);
                for (ConfigAttribute configAttribute : preAuthList) {
                    SecurityConfig tempConfig = (SecurityConfig) configAttribute;
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

    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndUrl(String requestMatcherType) {
        return getRolesAndResources("url", requestMatcherType);
    }

    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndMethod() {
        return getRolesAndResources("method", null);
    }

    public LinkedHashMap<Object, List<ConfigAttribute>> getRolesAndPointcut() {
        return getRolesAndResources("pointcut", null);
    }

    public List<ConfigAttribute> getRegexMatchedRequestMapping(String url) {
        // best regex matching - best 매칭된 Uri 에 따른 Role List 조회,
    	// DB 차원의 정규식 지원이 있는 경우 사용 (ex. hsqldb custom function, Oracle 10g regexp_like 등)
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("url", url);
        List<Map<String, Object>> resultList = this.namedParameterJdbcTemplate.queryForList(getSqlRegexMatchedRequestMapping(), paramMap);

        Iterator<Map<String, Object>> itr = resultList.iterator();
        Map<String, Object> tempMap;
        List<ConfigAttribute> configList = new LinkedList<>();

        // 같은 Uri 에 대한 Role 맵핑이므로 무조건 configList 에 add 함
        while (itr.hasNext()) {
            tempMap = itr.next();
			configList.add(new SecurityConfig((String) tempMap.get("authority")));
        }

        return configList;
    }

    public String getHierarchicalRoles() {
    	List<Map<String, Object>> resultList = this.namedParameterJdbcTemplate.queryForList(getSqlHierarchicalRoles(), new HashMap<String, String>());

        Iterator<Map<String, Object>> itr = resultList.iterator();
        StringBuilder concatedRoles = new StringBuilder();
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
