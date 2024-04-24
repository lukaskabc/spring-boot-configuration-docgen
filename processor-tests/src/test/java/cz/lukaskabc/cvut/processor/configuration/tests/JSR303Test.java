package cz.lukaskabc.cvut.processor.configuration.tests;

import org.junit.jupiter.api.Test;

class JSR303Test extends AbstractProcessorTest {

    @Test
    void Documentation_for_all_constraints() {
        documentFile(JSR303AllConstraintsConfiguration.class.getSimpleName(), "html", 44);
        validateFiles(JSR303AllConstraintsConfiguration.class.getSimpleName(), "html");
    }

    /**
     * Test for multiple constraints with messages<br>
     * test is performed only on some constraints, <br>
     * as all constraints should use same method(s) for generating messages
     */
    @Test
    void Multiple_constraints_with_messages() {
        testFile(JSR303MultipleMessagesConfiguration.class.getSimpleName(), "html", 5);
    }

    @Test
    void Markdown_for_all_JSR303() {
        testFile(JSR303AllConstraintsConfiguration.class.getSimpleName(), "md", 44);
    }

    @Test
    void Markdown_without_HTML_tags_for_all_JSR303() {
        documentFile(JSR303AllConstraintsConfiguration.class.getSimpleName(), "md", 44, "no_html");
        validateFiles(JSR303AllConstraintsConfiguration.class.getSimpleName() + "-no-html", "md");
    }
}
