package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.configuration.YamlPropertySourceFactory;
import cz.lukaskabc.cvut.processor.configuration.tests.properties.BeanMethodPropertiesConfiguration;
import cz.lukaskabc.cvut.processor.configuration.tests.properties.ConstructorBindingWithNameAnnotation;
import cz.lukaskabc.cvut.processor.configuration.tests.properties.RecursivePropertiesConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@PropertySource(value = "classpath:/configurations/value-bind-test.yaml", factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties({RecursivePropertiesConfiguration.class, ConstructorBindingWithNameAnnotation.class})
class SpringValueBindTest {
    @Autowired
    RecursivePropertiesConfiguration recursivePropertiesConfiguration;

    @Autowired
    ConstructorBindingWithNameAnnotation constructorBindingWithNameAnnotation;

    @Autowired
    BeanMethodPropertiesConfiguration beanMethodPropertiesConfiguration;

    @Test
    void recursive_configuration() {
        var stringTop = recursivePropertiesConfiguration.getAttribute();
        var nestedObject = recursivePropertiesConfiguration.getRecordAttribute();

        assertEquals("value", stringTop);
        assertNotNull(nestedObject);

        var nestedString = nestedObject.nestedAttribute();
        assertEquals("value2", nestedString);

        assertNull(nestedObject.nestedRecursiveAttribute());
    }

    @Test
    void constructor_binding_with_name_annotation() {
        var annotatedConstructor = constructorBindingWithNameAnnotation.getAnnotatedConstructor();
        var singleConstructor = constructorBindingWithNameAnnotation.getSingleConstructor();

        assertNotNull(annotatedConstructor);
        assertNotNull(singleConstructor);

        assertEquals("valid", annotatedConstructor.getName());
        assertEquals("valid", singleConstructor.getName());
    }

    @Test
    void properties_annotated_method_binding() {
        var externalClass = beanMethodPropertiesConfiguration.externalClass();
        var innerClass = beanMethodPropertiesConfiguration.innerClass();
        var configValue = "config value";

        assertNotNull(externalClass);
        assertNotNull(innerClass);

        assertEquals(configValue, externalClass.testGetValue());
        assertEquals(5, externalClass.testGetSubClassAttribute().testGetSubValue());

        assertEquals("value from bean method", innerClass.getFinalAttribute());
        assertEquals(configValue, innerClass.getNonFinalAttribute());
    }
}
