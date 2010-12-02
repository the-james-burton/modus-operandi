package com.modusoperandi.properties;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.modusoperandi.monitor.PropertiesLoader;

public class PropertiesLoaderTest {
    PropertiesLoader propertiesLoader;

    @Before
    public void setUp() {
        propertiesLoader = new PropertiesLoader();
        // propertiesLoader.setInputDirectory("\\\\lonms03418\\cruise\\builds\\finesse-analysis\\reports\\config\\properties");
    }

    @Test
    public void testLoad() throws Exception {
        propertiesLoader.run();
        Map<String, String> props = propertiesLoader.getProperties("UAT", "rates2", true);
        assertNotNull(props);
        propertiesLoader.run();
        props = propertiesLoader.getProperties("UAT", "rates3");
    }
}
