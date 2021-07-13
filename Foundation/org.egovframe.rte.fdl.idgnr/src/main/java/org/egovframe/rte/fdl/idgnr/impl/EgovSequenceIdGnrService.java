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
package org.egovframe.rte.fdl.idgnr.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ID Generation 서비스를 위한 Sequence 구현 클래스
 * 
 * <p><b>NOTE</b>: DBMS 에서 Sequence 를 제공하는 경우, 
 * Sequence 기반의 유일키를 제공 받을 수 있다.</p>
 * 
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @deprecated Use the EgovSequenceIdGnrServiceImpl class.
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.02.01	김태호				최초 생성
 * 2015.10.13	장동한				close 보안 코딩 적용
 * 2017.02.14	장동한				시큐어코딩(ES)-오류 상황 대응 부재[CWE-390, CWE-397]
 * </pre>
 */
@Deprecated
public class EgovSequenceIdGnrService extends AbstractDataIdGnrService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSequenceIdGnrService.class);

	/**
	 * BigDecimal 유형의 ID 제공
	 * @return the next id as a BigDecimal.
	 * @throws FdlException 여타이유에 의해 아이디 생성이 불가능 할때
	 */
	protected BigDecimal getNextBigDecimalIdInner() throws FdlException {
		LOGGER.debug(messageSource.getMessage("debug.idgnr.sequenceid.query", new String[] { query }, Locale.getDefault()));

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getBigDecimal(1);
			} else {
				LOGGER.error(messageSource.getMessage("error.idgnr.sequenceid.notallocate.id", new String[] {}, Locale.getDefault()));
				throw new FdlException(messageSource, "error.idgnr.sequenceid.notallocate.id");
			}
		} catch (SQLException e) {
			LOGGER.error(messageSource.getMessage("error.idgnr.get.connection", new String[] {}, Locale.getDefault()));
			throw new FdlException(messageSource, "error.idgnr.get.connection", e);
		}  finally {
			//2017.02.14 장동한 시큐어코딩(ES)-오류 상황 대응 부재[CWE-390, CWE-397]
			if (rs != null) try { rs.close(); } catch(SQLException ex) {LOGGER.error("[SQLException] ResultSet Next Runing : "+ ex.getMessage());}
			//2017.02.14 장동한 시큐어코딩(ES)-오류 상황 대응 부재[CWE-390, CWE-397]
			if (stmt != null) try { stmt.close(); } catch(SQLException ex) {LOGGER.error("[SQLException] ResultSet Next Runing : "+ ex.getMessage());}
			//2017.02.14 장동한 시큐어코딩(ES)-오류 상황 대응 부재[CWE-390, CWE-397]
			if (conn != null) try { conn.close(); } catch(SQLException ex) {LOGGER.error("[SQLException] ResultSet Next Runing : "+ ex.getMessage());}
		}
	}

	/**
	 * long 유형의 ID 제공
	 * @return the next id as a long.
	 * @throws FdlException 여타이유에 의해 아이디 생성이 불가능 할때
	 */
	protected long getNextLongIdInner() throws FdlException {
		LOGGER.debug(messageSource.getMessage("debug.idgnr.sequenceid.query", new String[] { query }, Locale.getDefault()));

		try {
			Connection conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			try {
				if (rs.next()) {
					return rs.getLong(1);
				} else {
					LOGGER.error(messageSource.getMessage("error.idgnr.sequenceid.notallocate.id", new String[] {}, Locale.getDefault()));
					throw new FdlException(messageSource, "error.idgnr.sequenceid.notallocate.id");
				}
			} finally {
				//2017.02.14 장동한 시큐어코딩(ES)-오류 상황 대응 부재[CWE-390, CWE-397]
				if (rs != null) try { rs.close(); } catch(SQLException ex) {LOGGER.error("[SQLException] ResultSet Next Runing : "+ ex.getMessage());}
				//2017.02.14 장동한 시큐어코딩(ES)-오류 상황 대응 부재[CWE-390, CWE-397]
				if (stmt != null) try { stmt.close(); } catch(SQLException ex) {LOGGER.error("[SQLException] ResultSet Next Runing : "+ ex.getMessage());}
				//2017.02.14 장동한 시큐어코딩(ES)-오류 상황 대응 부재[CWE-390, CWE-397]
				if (conn != null) try { conn.close(); } catch(SQLException ex) {LOGGER.error("[SQLException] ResultSet Next Runing : "+ ex.getMessage());}
			}
		} catch (SQLException e) {
			LOGGER.error(messageSource.getMessage("error.idgnr.get.connection", new String[] {}, Locale.getDefault()));
			throw new FdlException(messageSource, "error.idgnr.get.connection", e);
		}
	}

}
