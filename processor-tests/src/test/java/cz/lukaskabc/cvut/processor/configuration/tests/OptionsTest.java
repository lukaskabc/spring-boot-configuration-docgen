package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.configuration.tests.properties.InnerClassConfiguration;
import org.junit.jupiter.api.Test;

class OptionsTest extends AbstractProcessorTest {
    @Test
    void environment_prefix() {
        var ext = "md";
        documentFile("properties/" + InnerClassConfiguration.class.getSimpleName(), ext, 2, "env_prefix=myprefix");
        validateFiles("OptionEnvPrefix", ext);
    }
}
