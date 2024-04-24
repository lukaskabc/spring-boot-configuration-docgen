package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.configuration.tests.properties.*;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConfigurationPropertiesAnnotationTest extends AbstractProcessorTest {
    @Override
    protected String getFolderName() {
        return "properties";
    }

    @Test
    void Class_with_one_inner_class_HTML() {
        testFile(InnerClassConfiguration.class.getSimpleName(), "html", 2);
    }

    @Test
    void Class_with_one_inner_class_MD() {
        testFile(InnerClassConfiguration.class.getSimpleName(), "md", 2);
    }

    @Test
    void Class_with_third_level_inner_class() {
        testFile(ThirdInnerClassConfiguration.class.getSimpleName(), "html", 3);
    }

    @Test
    void Simple_record_defined_inside_configuration_class() {
        testFile(InnerRecordConfiguration.class.getSimpleName(), "html", 10);
    }

    @Test
    void Spring_nested_configuration_ConfigurationProperties_annotation_on_inner_class() {
        testFile(SpringNestedConfiguration.class.getSimpleName(), "html", 2);
    }

    @Test
    void Common_data_types_are_recognized_as_convertible_and_are_documented() {
        testFile(DataTypesConfiguration.class.getSimpleName(), "html", 27);
    }

    @Test
    void External_class_used_as_data_type() {
        var files = List.of(ExternalClassAttributeConfiguration.class.getSimpleName(), ExternalClass.class.getSimpleName());
        documentFiles(files, "html", 5);
        validateFiles(ExternalClassAttributeConfiguration.class.getSimpleName(), "html");
    }

    @Test
    void Record_direct_properties_annotation() {
        testFile(RecordDirectProperties.class.getSimpleName(), "md", 2);
    }

    @Test
    void Recursive_configuration_properties() {
        testFile(RecursivePropertiesConfiguration.class.getSimpleName(), "md", 3);
    }

    @Test
    void Constructor_binding_with_name_annotation() {
        testFile(ConstructorBindingWithNameAnnotation.class.getSimpleName(), "md", 3);
    }

    @Test
    void Bean_method_with_properties_annotation() {
        var files = List.of(BeanMethodPropertiesConfiguration.class.getSimpleName(), ExternalClass.class.getSimpleName());
        documentFiles(files, "md", 3);
        validateFiles(BeanMethodPropertiesConfiguration.class.getSimpleName(), "md");
    }

    @Test
    void Javadoc_comment_tags_md() {
        testFile(JavadocTagsConfiguration.class.getSimpleName(), "md", 2);
        testFile(JavadocTagsConfiguration.class.getSimpleName(), "html", 2);
    }


}
