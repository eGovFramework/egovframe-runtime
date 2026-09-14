package org.egovframe.rte.bat.support;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EgovResourceVariablePropertiesTest {

    @Test
    void includesInheritedDefaultResources() {
        EgovResourceVariable variable = new EgovResourceVariable();
        variable.setPros(new Properties(properties("input.resource", "input.csv")));

        assertEquals("input.csv", variable.getVariable("input.resource"));
        assertEquals("input.csv", variable.getVariableString("input.resource"));
        assertEquals("input.csv", variable.getVariableMap().get("input.resource"));
    }

    @Test
    void explicitValuesOverrideDefaults() {
        Properties properties = new Properties(properties("input.resource", "default.csv"));
        properties.setProperty("input.resource", "custom.csv");
        EgovResourceVariable variable = new EgovResourceVariable();
        variable.setPros(properties);

        assertEquals("custom.csv", variable.getVariableString("input.resource"));
    }

    @Test
    void resolvesMultipleLevelsOfDefaults() {
        Properties defaults = properties("input.resource", "input.csv");
        Properties parent = new Properties(defaults);
        parent.setProperty("output.resource", "output.csv");
        EgovResourceVariable variable = new EgovResourceVariable();
        variable.setPros(new Properties(parent));

        assertEquals("input.csv", variable.getVariableString("input.resource"));
        assertEquals("output.csv", variable.getVariableString("output.resource"));
    }

    @Test
    void replacingPropertiesDropsPreviousValuesAndLoadsNewDefaults() {
        EgovResourceVariable variable = new EgovResourceVariable();
        variable.setPros(properties("old.resource", "old.csv"));
        variable.setVariable("temporary", "value");

        variable.setPros(new Properties(properties("input.resource", "new.csv")));

        assertNull(variable.getVariable("old.resource"));
        assertNull(variable.getVariable("temporary"));
        assertEquals("new.csv", variable.getVariableString("input.resource"));
    }

    @Test
    void emptyPropertiesClearExistingValues() {
        EgovResourceVariable variable = new EgovResourceVariable();
        variable.setPros(properties("input.resource", "input.csv"));

        variable.setPros(new Properties());

        assertTrue(variable.getVariableMap().isEmpty());
    }

    private Properties properties(String key, String value) {
        Properties properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}
