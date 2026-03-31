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
package org.egovframe.rte.psl.orm.ibatis;

import com.ibatis.sqlmap.client.event.RowHandler;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

/**
 * Interface that specifies a basic set of iBATIS SqlMapClient operations,
 * implemented by {@link SqlMapClientTemplate}. Not often used, but a useful
 * option to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Defines SqlMapClientTemplate's convenience methods that mirror
 * the iBATIS {@link com.ibatis.sqlmap.client.SqlMapExecutor}'s execution
 * methods. Users are strongly encouraged to read the iBATIS javadocs
 * for details on the semantics of those methods.
 *
 * @author Juergen Hoeller
 * @see SqlMapClientTemplate
 * @see com.ibatis.sqlmap.client.SqlMapClient
 * @see com.ibatis.sqlmap.client.SqlMapExecutor
 * @since 24.02.2004
 * @deprecated as of Spring 3.2, in favor of the native Spring support
 * in the Mybatis follow-up project (http://code.google.com/p/mybatis/)
 */
@Deprecated
public interface SqlMapClientOperations {

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForObject(String)
     */
    Object queryForObject(String statementName) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForObject(String, Object)
     */
    Object queryForObject(String statementName, Object parameterObject) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForObject(String, Object, Object)
     */
    Object queryForObject(String statementName, Object parameterObject, Object resultObject) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForList(String)
     */
    List queryForList(String statementName) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForList(String, Object)
     */
    List queryForList(String statementName, Object parameterObject) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForList(String, int, int)
     */
    List queryForList(String statementName, int skipResults, int maxResults) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForList(String, Object, int, int)
     */
    List queryForList(String statementName, Object parameterObject, int skipResults, int maxResults) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryWithRowHandler(String, RowHandler)
     */
    void queryWithRowHandler(String statementName, RowHandler rowHandler) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryWithRowHandler(String, Object, RowHandler)
     */
    void queryWithRowHandler(String statementName, Object parameterObject, RowHandler rowHandler) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForMap(String, Object, String)
     */
    Map queryForMap(String statementName, Object parameterObject, String keyProperty) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForMap(String, Object, String, String)
     */
    Map queryForMap(String statementName, Object parameterObject, String keyProperty, String valueProperty)  throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#insert(String)
     */
    Object insert(String statementName) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#insert(String, Object)
     */
    Object insert(String statementName, Object parameterObject) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#update(String)
     */
    int update(String statementName) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#update(String, Object)
     */
    int update(String statementName, Object parameterObject) throws DataAccessException;

    /**
     * Convenience method provided by Spring: execute an update operation
     * with an automatic check that the update affected the given required
     * number of rows.
     *
     * @param statementName        the name of the mapped statement
     * @param parameterObject      the parameter object
     * @param requiredRowsAffected the number of rows that the update is
     *                             required to affect
     * @throws org.springframework.dao.DataAccessException in case of errors
     */
    void update(String statementName, Object parameterObject, int requiredRowsAffected) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#delete(String)
     */
    int delete(String statementName) throws DataAccessException;

    /**
     * @throws org.springframework.dao.DataAccessException in case of errors
     * @see com.ibatis.sqlmap.client.SqlMapExecutor#delete(String, Object)
     */
    int delete(String statementName, Object parameterObject) throws DataAccessException;

    /**
     * Convenience method provided by Spring: execute a delete operation
     * with an automatic check that the delete affected the given required
     * number of rows.
     *
     * @param statementName        the name of the mapped statement
     * @param parameterObject      the parameter object
     * @param requiredRowsAffected the number of rows that the delete is
     *                             required to affect
     * @throws org.springframework.dao.DataAccessException in case of errors
     */
    void delete(String statementName, Object parameterObject, int requiredRowsAffected) throws DataAccessException;

}
