/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).

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
package egovframework.rte.fdl.property.db;

import javax.sql.DataSource;

import org.springframework.core.env.PropertySource;

/**
 * DB기반의 PropertySource를 관리하는 클래스
 * @author yjLee
 *
 */
public class DbPropertySource extends PropertySource<DbPropertySourceDelegate> {
		
	public DbPropertySource(String name, DataSource dataSource, String sql) {
		super(name, new DbPropertySourceDelegate(dataSource, sql));
	}
	
	@Override
	public Object getProperty(String key) {
		return this.source.getProperty(key);
	}

}
