package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.configuration.tests.properties.ConstructorBindingWithDefaultConfiguration;
import cz.lukaskabc.cvut.processor.configuration.tests.properties.DefaultValueConfiguration;
import cz.lukaskabc.cvut.processor.configuration.tests.properties.DefaultValueExternalStaticConfiguration;
import cz.lukaskabc.cvut.processor.configuration.tests.properties.JavadocDefaultValueTagConfiguration;
import org.junit.jupiter.api.Test;

class DefaultValuesCollectorTest extends AbstractProcessorTest {
    @Override
    protected String getFolderName() {
        return "properties";
    }

    @Test
    void default_value_variants() {
        testFile(DefaultValueConfiguration.class.getSimpleName(), "md", 103);
    }

    @Test
    void defaultValueAnnotation_ConstructorBinding() {
        testFile(ConstructorBindingWithDefaultConfiguration.class.getSimpleName(), "md", 3);
    }

    @Test
    void default_values_from_external_class_constants() {
        testFile(DefaultValueExternalStaticConfiguration.class.getSimpleName(), "md", 13);
    }

    @Test
    void javadoc_default_value_tag() {
        testFile(JavadocDefaultValueTagConfiguration.class.getSimpleName(), "md", 3);
    }
}
