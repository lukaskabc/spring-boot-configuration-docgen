package cz.lukaskabc.cvut.processor.configuration.examples;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ValueAnnotationConfigTest {
    @Autowired
    ValueAnnotationConfig config;

    @Test
    void valuesFromApplicationYaml() {
        Assertions.assertEquals("attribute", config.getAttribute());
        Assertions.assertEquals("constructor", config.getConstructorParameterValue());
        Assertions.assertEquals(10, config.getMethodValue());
        Assertions.assertEquals(3.6, config.getMethodParameterValue());
    }
}
