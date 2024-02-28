/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.access.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * DB기반의 보호된 자원 관리를 구현한 DAO 클래스
 *
 * <p>Desc.: DB기반의 보호된 자원 관리를 구현한 DAO 클래스</p>
 *
 * @author ESFC
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.10.01	ESFC            최초 생성
 * </pre>
 */
public class EgovAccessDao implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovAccessDao.class);

    private ApplicationContext context;
    private JdbcTemplate jdbcTemplate;
    private String authorityUserQuery;
    private String roleAndUrlQuery;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String getAuthorityUserQuery() {
        return authorityUserQuery;
    }

    public void setAuthorityUserQuery(String authorityUserQuery) {
        this.authorityUserQuery = authorityUserQuery;
    }

    public String getRoleAndUrlQuery() {
        return roleAndUrlQuery;
    }

    public void setRoleAndUrlQuery(String roleAndUrlQuery) {
        this.roleAndUrlQuery = roleAndUrlQuery;
    }

    public List<Map<String, Object>> getAuthorityUser() {
        LOGGER.debug("##### EgovAccessDao getAuthorityUser >>> {} ", getAuthorityUserQuery());
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList(getAuthorityUserQuery());
        return list;
    }

    public List<Map<String, Object>> getRoleAndUrl() {
        LOGGER.debug("##### EgovAccessDao getRoleAndUrl >>> {} ", getRoleAndUrlQuery());
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList(getRoleAndUrlQuery());
        return list;
    }

}
