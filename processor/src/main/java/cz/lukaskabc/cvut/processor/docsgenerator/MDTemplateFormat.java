package cz.lukaskabc.cvut.processor.docsgenerator;

import freemarker.core.CommonMarkupOutputFormat;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class MDTemplateFormat extends CommonMarkupOutputFormat<TemplateMDOutputModel> {

    public static final MDTemplateFormat INSTANCE = new MDTemplateFormat();

    private boolean allowHTMLLineBreaks = false;

    private boolean escapeNewLines = false;

    private MDTemplateFormat() {
    }

    public String escapeMarkdown(String text) {
        String escapedText = text;
        var charactersToEscape = List.of(/*'\\', '`', '*', '_', '{', '}', '[', ']', '<', '>', '(', ')', '#', '+', '-', '.', '!',*/ '|');
        for (var ch : charactersToEscape) {
            escapedText = escapeCharacter(escapedText, ch);
        }

        return replaceNewLines(escapedText);
    }

    /**
     * Escapes unescaped character
     *
     * @param text      Text to be searched for character
     * @param character Character to be escaped
     * @return Escaped text
     */
    private String escapeCharacter(String text, Character character) {
        // This ensures the character is properly escaped for the regex pattern
        String escapedChar = "\\" + character;

        // Escape non escaped characters
        return text.replaceAll("(?<!\\\\)(\\\\{2})*" + escapedChar, "\\\\$0");
    }

    private String linebreak() {
        if (allowHTMLLineBreaks) {
            return "<br>";
        }
        return " ";
    }

    /**
     * Replaces plain new lines with HTML line breaks, if allowed
     *
     * @param text
     * @return
     */
    private String replaceNewLines(String text) {
        if (escapeNewLines)
            return text.replace("\n", linebreak());

        return text;
    }


    @Override
    public void output(String textToEsc, Writer out) throws IOException {
        out.write(escapeMarkdown(textToEsc));
    }

    @Override
    public String escapePlainText(String plainTextContent) {
        return escapeMarkdown(plainTextContent);
    }

    @Override
    public boolean isLegacyBuiltInBypassed(String builtInName) {
        return false;
    }

    @Override
    protected TemplateMDOutputModel newTemplateMarkupOutputModel(String plainTextContent, String markupContent) {
        return new TemplateMDOutputModel(plainTextContent, markupContent);
    }

    @Override
    public String getName() {
        return "md";
    }

    @Override
    public String getMimeType() {
        // https://www.iana.org/assignments/media-types/media-types.xhtml#text
        return "text/markdown";
    }

    public MDTemplateFormat setAllowHTMLLineBreaks(boolean allowHTMLLineBreaks) {
        this.allowHTMLLineBreaks = allowHTMLLineBreaks;
        return this;
    }

    public MDTemplateFormat setEscapeNewLines(boolean escapeNewLines) {
        this.escapeNewLines = escapeNewLines;
        return this;
    }
}
