package org.egovframe.rte.fdl.property.db;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DbPropertySourceDelegateTest {

    @Test
    public void initPropertiesLoadsLowerCaseColumnLabels() {
        EmbeddedDatabase database = createDatabase();
        try {
            DbPropertySourceDelegate delegate = new DbPropertySourceDelegate(database,
                    "SELECT PKEY AS \"pkey\", PVALUE AS \"pvalue\" FROM PROPERTY");

            assertEquals("sample01", delegate.getProperty("egov.test.sample01"));
            assertEquals("sample02", delegate.getProperty("egov.test.sample02"));
        } finally {
            database.shutdown();
        }
    }

    @Test
    public void initPropertiesLoadsUpperCaseColumnLabels() {
        EmbeddedDatabase database = createDatabase();
        try {
            DbPropertySourceDelegate delegate = new DbPropertySourceDelegate(database,
                    "SELECT PKEY, PVALUE FROM PROPERTY");

            assertEquals("sample01", delegate.getProperty("egov.test.sample01"));
            assertEquals("sample02", delegate.getProperty("egov.test.sample02"));
        } finally {
            database.shutdown();
        }
    }

    @Test
    public void initPropertiesSkipsRowsWithoutKeyColumn() {
        EmbeddedDatabase database = createDatabase();
        try {
            DbPropertySourceDelegate delegate = new DbPropertySourceDelegate(database,
                    "SELECT PVALUE FROM PROPERTY");

            assertNull(delegate.getProperty(null));
        } finally {
            database.shutdown();
        }
    }

    private EmbeddedDatabase createDatabase() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:/META-INF/testdata/testdb.sql")
                .build();
    }

}
