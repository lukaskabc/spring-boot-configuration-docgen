package cz.lukaskabc.cvut.processor.formatter;

/**
 * Provides a specific text format<br>
 * Methods returning Formatter are fluent, and they are appending to the instance
 */
public abstract class Formatter implements Appendable {

    protected final StringBuilder builder = new StringBuilder();

    public String eol() {
        return "\n";
    }

    /**
     * Removes double whitespace characters including spaces and line breaks
     *
     * @param text Text to be cleaned
     * @return Cleaned text
     */
    public String removeDoubleSpaces(String text) {
        // \\s matches spaces and line breaks
        return text.replaceAll(" {2,}", " ") // remove spaces
                .replaceAll("\\s{2,}", eol()) // remove double line breaks
                .replaceAll("((\\s*)<br>(\\s*)){2,}", linebreak()) // remove multiple spaces and line breaks
                .replaceAll("(<br>|\\s)++$", ""); // remove trailing HTML line breaks or spaces
    }

    public String removeLeadingSpacesAndLineBreaks(String text) {
        return text.replaceAll("^(<br>|\\s)++", "");
    }

    public abstract String getFileExtension();

    /**
     * Sequence forcing new line
     */
    public abstract String linebreak();

    /**
     * Appends new line
     * @see #linebreak()
     */
    public Formatter newline() {
        return rawAppend(linebreak());
    }

    public abstract Formatter paragraph();

    public abstract Formatter bold(String text);

    public abstract Formatter italic(String text);

    public abstract Formatter code(String text);

    public abstract Formatter escape(String text);

    public Formatter append(String text) {
        if (text.length() > 1) // handles appending spaces, new lines etc
            text = removeDoubleSpaces(text);

        builder.append(text);
        return this;
    }

    public Formatter rawAppend(String text) {
        builder.append(text);
        return this;
    }

    /**
     * @return empty clone of this formatter (without any content)
     */
    public abstract Formatter emptyClone();

    @Override
    public String toString() {
        return builder.toString();
    }

    @Override
    public Formatter append(CharSequence csq) {
        return append(csq.toString());
    }

    @Override
    public Formatter append(CharSequence csq, int start, int end) {
        return append(csq.subSequence(start, end).toString());
    }

    @Override
    public Formatter append(char c) {
        return append(String.valueOf(c));
    }
}
