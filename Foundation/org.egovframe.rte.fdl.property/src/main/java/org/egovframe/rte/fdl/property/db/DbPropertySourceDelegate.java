/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.property.db;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DB기반의 PropertySource를 저장하는 클래스
 */
public class DbPropertySourceDelegate {

    public static final String PROPERTY_SOURCE_KEY = "PKEY";
    public static final String PROPERTY_SOURCE_VALUE = "PVALUE";

    private final Map<String, Object> properties = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;

    private final String sql;

    public DbPropertySourceDelegate(DataSource dataSource, String sql) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sql = sql;
        initProperties();
    }

    public void initProperties() {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        if (ObjectUtils.isNotEmpty(result)) {
            for (Map<String, Object> property : result) {
                if (property != null) {
                    Iterator<String> iterator = property.keySet().iterator();
                    String pKey = null;
                    String pValue = null;
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        String data = (String) property.get(key);
                        if (PROPERTY_SOURCE_KEY.equals(key)) {
                            pKey = data;
                        } else if (PROPERTY_SOURCE_VALUE.equals(key)) {
                            pValue = data;
                        }
                    }
                    properties.put(pKey, pValue);
                }
            }
        }
    }

    public Object getProperty(String key) {
        return this.properties.get((String) key);
    }

}
