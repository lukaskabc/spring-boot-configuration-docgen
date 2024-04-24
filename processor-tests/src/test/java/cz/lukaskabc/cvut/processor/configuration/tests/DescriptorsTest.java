package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.ConfigurationDocProcessor;
import cz.lukaskabc.cvut.processor.ProcessorConfiguration;
import cz.lukaskabc.cvut.processor.configuration.tests.descriptors.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

class DescriptorsTest extends AbstractProcessorTest {
    @Override
    protected String getFolderName() {
        return "descriptors";
    }

    private static boolean arePropertyGettersRequired() {
        return ProcessorConfiguration.REQUIRE_GETTER_FOR_PROPERTIES;
    }

    @Test
    @EnabledIf("arePropertyGettersRequired")
    void different_accessors_with_getters_required() {
        testFile(AccessorsConfiguration.class.getSimpleName(), "md", 2);
    }

    @Test
    @DisabledIf("arePropertyGettersRequired")
    void different_accessors_with_getters_not_required() {
        documentFile(AccessorsConfiguration.class.getSimpleName(), "md", 4);
        validateFiles(AccessorsConfiguration.class.getSimpleName() + "-no-getters", "md");
    }

    @Test
    void multiple_constructors_one_annotated() {
        testFile(AnnotatedConstructorConfiguration.class.getSimpleName(), "md", 2);
    }

    @Test
    void one_single_constructor() {
        testFile(SingleConstructorConfiguration.class.getSimpleName(), "md", 2);
    }

    @Test
    void nonAttribute_parameter_in_constructor_binding() {
        testFile(ConstructorBindingWithNonAttributeParameterConfiguration.class.getSimpleName(), "md", 2);
    }

    @Test
    void Collection_type_attributes() {
        testFile(CollectionConfiguration.class.getSimpleName(), "md", 6);
    }
}
