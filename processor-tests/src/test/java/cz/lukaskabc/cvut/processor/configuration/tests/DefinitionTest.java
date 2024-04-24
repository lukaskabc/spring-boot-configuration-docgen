package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.ConfigurationDocProcessor;
import cz.lukaskabc.cvut.processor.ProcessorConfiguration;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.SupportedOptions;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefinitionTest {
    public ConfigurationDocProcessor processor = new ConfigurationDocProcessor();

    /**
     * Validates that all {@link SupportedOptions @SupportedOptions} matches the {@link ConfigurationDocProcessor#getSupportedOptions()}.
     */
    @Test
    void check_options_annotation_definition() {
        var prefix = ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX;

        var annotationValuesArray = processor.getClass().getAnnotation(SupportedOptions.class).value();
        List<String> annotationValues = Arrays.asList(annotationValuesArray);

        var anIT = annotationValues.iterator();
        var supported = processor.getSupportedOptions();

        while(anIT.hasNext()) {
            var annotationValue = anIT.next();

            assertTrue(annotationValue.startsWith(prefix));

            assertTrue(supported.contains(annotationValue));
        }

        assertEquals(annotationValues.size(), processor.getSupportedOptions().size());
        assertFalse(anIT.hasNext());
    }
}
