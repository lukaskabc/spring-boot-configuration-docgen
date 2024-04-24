package cz.lukaskabc.cvut.processor.configuration.tests;

import org.junit.jupiter.api.Test;

class MarkdownTest extends AbstractProcessorTest {
    @Test
    void Markdown_breaking_javadoc_is_properly_escaped() {
        // common markdown as bold, italic, etc. is not escaped, but a table format is escaped
        testFile(MarkdownBreakingJavadocConfiguration.class.getSimpleName(), "md", 3);
    }

    @Test
    void Markdown_without_HTML_tags() {
        documentFile(MarkdownBreakingJavadocConfiguration.class.getSimpleName(), "md", 3, "no_html");
        validateFiles(MarkdownBreakingJavadocConfiguration.class.getSimpleName() + "-no-html", "md");
    }


}
