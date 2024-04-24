package cz.lukaskabc.cvut.processor.formatter;

/**
 * Provides HTML formatting
 */
public class HTMLFormatter extends Formatter {

    @Override
    public String getFileExtension() {
        return "html";
    }

    @Override
    public String linebreak() {
        return "<br>" + eol();
    }

    @Override
    public Formatter paragraph() {
        return rawAppend("<br><br>").append(eol()); // using <p> causes unexpected space on GitHub depending on previous <p> usage
    }

    @Override
    public Formatter bold(String text) {
        return append("<b>")
                .append(text)
                .append("</b>");
    }

    @Override
    public Formatter italic(String text) {
        return append("<i>")
                .append(text)
                .append("</i>");
    }

    @Override
    public Formatter code(String text) {
        return append("<code>")
                .escape(text)
                .append("</code>");
    }

    @Override
    public Formatter escape(String text) {
        return append(text.trim()
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\\n", ""));
    }

    @Override
    public Formatter emptyClone() {
        return new HTMLFormatter();
    }
}
