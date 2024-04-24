package cz.lukaskabc.cvut.processor.configuration.tests;


import cz.lukaskabc.cvut.processor.configuration.tests.value.ValueAnnotationCommentOverloading;
import cz.lukaskabc.cvut.processor.configuration.tests.value.ValueAnnotationConfiguration;
import cz.lukaskabc.cvut.processor.configuration.tests.value.ValueInPropertiesAnnotation;
import org.junit.jupiter.api.Test;

class ValueAnnotationTest extends AbstractProcessorTest {
    @Override
    protected String getFolderName() {
        return "value";
    }

    @Test
    void Annotation_on_different_element_kinds_HTML() {
        testFile(ValueAnnotationConfiguration.class.getSimpleName(), "html", 6);
    }

    @Test
    void Annotation_on_different_element_kinds_MD() {
        testFile(ValueAnnotationConfiguration.class.getSimpleName(), "md", 6);
    }

    @Test
    void Comments_on_multiple_elements_with_same_Value_annotations() {
        // expected result: comments chained when differ
        testFile(ValueAnnotationCommentOverloading.class.getSimpleName(), "html", 2);
    }

    @Test
    void Value_annotation_used_inside_ConfigurationProperties_class() {
        testFile(ValueInPropertiesAnnotation.class.getSimpleName(), "html", 4);
    }
}
