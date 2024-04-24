package cz.lukaskabc.cvut.processor.docsgenerator;

import cz.lukaskabc.cvut.processor.formatter.Formatter;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Escapes line breaks with formatter
 */
public class MarkdownLineEscapeDirective implements TemplateDirectiveModel {

    private static Formatter formatter = null;

    public static void setFormatter(Formatter formatter) {
        MarkdownLineEscapeDirective.formatter = formatter;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        if (!params.isEmpty()) {
            throw new TemplateException("Markdown line escape directive does not support any parameters (" + params.size() + " given)", env);
        }
        if (loopVars.length != 0) {
            throw new TemplateException("Markdown line escape directive does not support any loop variables (" + loopVars.length + " given)", env);
        }
        if (body == null) {
            return;
        }
        if (formatter == null) {
            throw new TemplateException("Internal error: formatter not set in Markdown line escape directive", env);
        }

        body.render(new EscapeWriter(env.getOut()));
    }

    private static class EscapeWriter extends Writer {

        private final Writer out;

        public EscapeWriter(Writer out) {
            this.out = out;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                char c = cbuf[i];
                if (c == '\r') {
                    continue;
                }
                if (c == '\n') {
                    out.write(formatter.linebreak());
                } else {
                    out.write(c);
                }
            }
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }
}
