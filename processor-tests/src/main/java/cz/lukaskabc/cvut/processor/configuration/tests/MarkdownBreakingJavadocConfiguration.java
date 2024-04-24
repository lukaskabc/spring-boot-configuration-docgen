package cz.lukaskabc.cvut.processor.configuration.tests;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class MarkdownBreakingJavadocConfiguration {
    /**
     * This Javadoc contains trailing whitespaces and line breaks <br>
     * <br> <br>
     */
    String foo;

    /**
     * This | Javadoc |
     * contains | trailing | whitespaces and | line | breaks | <br> <br>
     * <p>
     * | and markdown table | syntax | <br> <br>
     * || two
     * ||||||| seven
     * <p>
     * <p>
     *     \\\|
     * ||
     */
    String bar;

    /**
     * Lets __use__ some *markdown* inline **syntax*\* and \\`also` some ```code``` <br>
     */
    String baz;
}
