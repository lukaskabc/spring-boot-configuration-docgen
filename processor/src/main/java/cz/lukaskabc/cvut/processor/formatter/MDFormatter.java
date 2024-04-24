package cz.lukaskabc.cvut.processor.formatter;

import java.util.List;

/**
 * Provides Markdown formatting
 */
public class MDFormatter extends Formatter {

    private final boolean disableHTMLtags;

    boolean prevCol = false;

    int headColCount = 0;

    public MDFormatter(boolean disableHTMLtags) {
        this.disableHTMLtags = disableHTMLtags;
    }

    @Override
    public String getFileExtension() {
        return "md";
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

        // Use the escaped character in the regex pattern
        return text.replaceAll("(?<!\\\\)(\\\\{2})*" + escapedChar, "\\\\$0");
    }

    /**
     * Replaces plain new lines with HTML line breaks
     *
     * @param text
     * @return
     */
    private String replaceNewLines(String text) {
        return text.replace("\n", linebreak());
    }

    @Override
    public String linebreak() {
        if (disableHTMLtags) {
            return " "; // linebreaks inside table will break the format
        }
        return "<br>";
    }

    @Override
    public Formatter paragraph() {
        return append(eol()).append(eol());
    }

    @Override
    public Formatter bold(String text) {
        return append("**")
                .append(escapeCharacter(text, '*'))
                .append("**");
    }

    @Override
    public Formatter italic(String text) {
        return append("*")
                .append(escapeCharacter(text, '*'))
                .append("*");
    }

    @Override
    public Formatter code(String text) {
        return append("```")
                .append(escapeCharacter(text, '`'))
                .append("```");
    }

    @Override
    public Formatter escape(String text) {
        String escapedText = text;
        var charactersToEscape = List.of('\\', '`', '*', '_', '{', '}', '[', ']', '<', '>', '(', ')', '#', '+', '-', '.', '!', '|');
        for (var ch : charactersToEscape) {
            escapedText = escapeCharacter(escapedText, ch);
        }

        return append(escapedText);
    }

    @Override
    public Formatter append(String text) {
        return super.append(escapeCharacter(text, '|'));
    }

    @Override
    public MDFormatter emptyClone() {
        return new MDFormatter(disableHTMLtags);
    }

}
