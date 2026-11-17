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
package org.egovframe.boot.security.userdetails;

import org.egovframe.boot.security.userdetails.jdbc.EgovUsersByUsernameMapping;
import org.egovframe.boot.security.userdetails.util.CamelCaseUtil;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DefaultMapUserDetailsMapping extends EgovUsersByUsernameMapping {

	public DefaultMapUserDetailsMapping(DataSource ds, String usersByUsernameQuery) {
		super(ds, usersByUsernameQuery);
	}

	@Override
	protected EgovUserDetails mapRow(ResultSet rs, int rownum) throws SQLException {
		String userid = rs.getString("user_id");
		String password = rs.getString("password");
		boolean enabled = rs.getBoolean("enabled");

		Map<String, String> map = new HashMap<String, String>();
		ResultSetMetaData md = rs.getMetaData();
		int cnt = md.getColumnCount();
		for (int i = 1; i <= cnt; i++) {
			String column = md.getColumnName(i).toLowerCase();
			String value = rs.getString(column);
			map.put(CamelCaseUtil.convert2CamelCase(column), value);
		}

		return new EgovUserDetails(userid, password, enabled, map);
	}

}
