package cz.lukaskabc.cvut.processor.configuration.examples;

import cz.lukaskabc.cvut.processor.configuration.termit.FullTermitConfiguration;
import cz.lukaskabc.cvut.processor.configuration.tests.AbstractProcessorTest;
import org.junit.jupiter.api.Test;

public class TermitConfigTest extends AbstractProcessorTest {
    @Override
    protected String getFolderName() {
        return "../termit";
    }

    @Test
    public void Document_termit_configuration() {
        testFile(FullTermitConfiguration.class.getSimpleName(), "html", 34);
    }
}
