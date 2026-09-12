package org.egovframe.rte.fdl.property.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DbPropertySourceDelegateReloadTest {

    private EmbeddedDatabase database;
    private JdbcTemplate jdbc;
    private DbPropertySourceDelegate delegate;

    @BeforeEach
    void setUp() {
        database = new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL).build();
        jdbc = new JdbcTemplate(database);
        createTable("VARCHAR(40)");
        jdbc.update("INSERT INTO REVIEW_PROPERTY VALUES (?, ?)", "remaining", "before");
        jdbc.update("INSERT INTO REVIEW_PROPERTY VALUES (?, ?)", "removed", "old");
        delegate = new DbPropertySourceDelegate(database, "SELECT PKEY, PVALUE FROM REVIEW_PROPERTY");
    }

    @AfterEach
    void tearDown() {
        database.shutdown();
    }

    @Test
    void reloadReflectsDeletedUpdatedAndAddedProperties() {
        jdbc.update("DELETE FROM REVIEW_PROPERTY WHERE PKEY = ?", "removed");
        jdbc.update("UPDATE REVIEW_PROPERTY SET PVALUE = ? WHERE PKEY = ?", "after", "remaining");
        jdbc.update("INSERT INTO REVIEW_PROPERTY VALUES (?, ?)", "added", "new");

        delegate.initProperties();

        assertNull(delegate.getProperty("removed"));
        assertEquals("after", delegate.getProperty("remaining"));
        assertEquals("new", delegate.getProperty("added"));
    }

    @Test
    void emptyQueryResultClearsAllProperties() {
        jdbc.update("DELETE FROM REVIEW_PROPERTY");

        delegate.initProperties();

        assertNull(delegate.getProperty("remaining"));
        assertNull(delegate.getProperty("removed"));
    }

    @Test
    void queryFailurePreservesPreviousValuesAndAllowsLaterReload() {
        jdbc.execute("DROP TABLE REVIEW_PROPERTY");

        assertThrows(DataAccessException.class, delegate::initProperties);
        assertEquals("before", delegate.getProperty("remaining"));
        assertEquals("old", delegate.getProperty("removed"));

        createTable("VARCHAR(40)");
        jdbc.update("INSERT INTO REVIEW_PROPERTY VALUES (?, ?)", "recovered", "new");
        delegate.initProperties();

        assertNull(delegate.getProperty("remaining"));
        assertNull(delegate.getProperty("removed"));
        assertEquals("new", delegate.getProperty("recovered"));
    }

    @Test
    void invalidColumnTypePreservesPreviousValues() {
        jdbc.execute("DROP TABLE REVIEW_PROPERTY");
        createTable("INTEGER");
        jdbc.update("INSERT INTO REVIEW_PROPERTY VALUES (?, ?)", "remaining", 123);

        assertThrows(ClassCastException.class, delegate::initProperties);

        assertEquals("before", delegate.getProperty("remaining"));
        assertEquals("old", delegate.getProperty("removed"));
    }

    @Test
    void reloadStillAllowsNullPropertyValues() {
        jdbc.update("UPDATE REVIEW_PROPERTY SET PVALUE = NULL WHERE PKEY = ?", "remaining");

        delegate.initProperties();

        assertNull(delegate.getProperty("remaining"));
        assertEquals("old", delegate.getProperty("removed"));
    }

    private void createTable(String valueType) {
        jdbc.execute("CREATE TABLE REVIEW_PROPERTY (PKEY VARCHAR(40), PVALUE " + valueType + ")");
    }
}
