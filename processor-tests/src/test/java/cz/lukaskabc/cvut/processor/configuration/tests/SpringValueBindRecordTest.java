package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.configuration.YamlPropertySourceFactory;
import cz.lukaskabc.cvut.processor.configuration.tests.properties.InnerRecordConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@PropertySource(value = "classpath:/configurations/value-bind-record-test.yaml", factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties({InnerRecordConfiguration.class})
class SpringValueBindRecordTest {
    @Autowired
    InnerRecordConfiguration configuration;

    @Test
    void configuration_bind_to_simple_record_uninitialized_attribute() {
        var recordAttribute = configuration.getSimpleRecordAttributeName();
        assertNotNull(recordAttribute);
        assertEquals("1value1", recordAttribute.component1());
        assertEquals("1value2", recordAttribute.component2());
    }

    @Test
    void configuration_simple_record_attribute_with_default_values() {
        var recordAttribute = configuration.getAttributeWithRecordDefault();
        assertNotNull(recordAttribute);
        assertEquals("value1", recordAttribute.component1());
        assertEquals("value2", recordAttribute.component2());
    }

    @Test
    void configuration_simple_record_attribute_with_default_values_overwrite() {
        var recordAttribute = configuration.getAttributeWithRecordDefaultOverwrite();
        assertNotNull(recordAttribute);
        assertEquals("2value1", recordAttribute.component1());
        assertEquals("2value2", recordAttribute.component2());
    }

    /**
     * Records cannot be partially updated, in config there is only component2 defined,
     * the record is recreated with defined values and other attributes are set to null,
     * in case they are primitive and so cannot be null,
     * then there are default values used
     * (false for boolean, zero for numbers etc.).
     */
    @Test
    void configuration_simple_record_attribute_with_default_values_partial_overwrite() {
        var recordAttribute = configuration.getAttributeWithRecordDefaultPartialOverwrite();
        assertNotNull(recordAttribute);
        assertNull(recordAttribute.component1());
        assertEquals("3value2", recordAttribute.component2());
    }

    @Test
    void configuration_record_with_default_component_initialized_attribute() {
        var recordAttribute = configuration.getInitializedRecordWithOneDefaultComponent();
        assertNotNull(recordAttribute);
        assertEquals("value1", recordAttribute.component1());
    }

    @Test
    void configuration_record_with_default_component_configured_attribute() {
        var recordAttribute = configuration.getConfiguredRecordWithOneDefaultComponent();
        assertNotNull(recordAttribute);
        assertEquals("4value1", recordAttribute.component1());
    }
}
