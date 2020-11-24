package org.egovframe.rte.itl.webservice.data.dao.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Wrapped version of DBUnits DatabaseDataSourceConnection to enable Spring Transaction support. 
 */
public class SpringDatabaseDataSourceConnection extends DatabaseDataSourceConnection {

    private DataSource dataSource;

    /**
     * @param dataSource
     * @throws SQLException
     */
    public SpringDatabaseDataSourceConnection(DataSource dataSource) throws SQLException {
        super(dataSource);
        this.dataSource = dataSource;
    }

    /**
     * @see org.dbunit.database.IDatabaseConnection#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }
}
