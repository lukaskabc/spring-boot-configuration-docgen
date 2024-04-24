package cz.lukaskabc.cvut.processor;

import com.sun.source.tree.CompilationUnitTree;
import cz.lukaskabc.cvut.processor.exception.PrototypeException;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.io.IOException;

/**
 * Logger using {@link Messager} for output
 */
public class Log {

    private static LogInstance plainContext;

    private static Messager messager;

    private static EnvironmentUtils envUtils;

    private Log() {
    }

    public static void init(EnvironmentUtils envUtils) {
        Log.messager = envUtils.processingEnvironment().getMessager();
        Log.plainContext = new LogInstance(null);
        Log.envUtils = envUtils;
    }

    public static LogInstance instance() {
        return plainContext;
    }

    public static LogInstance withContext(Element context) {
        return new LogInstance(context);
    }

    public static class LogInstance implements org.apache.commons.logging.Log {

        private final Element element;

        public LogInstance(Element context) {
            this.element = context;
        }

        /**
         * @return line of source code containing given position, null if error occurred
         */
        public static String getLineFromPositionContaining(CompilationUnitTree compilationUnit, long pos, String searchFor) {
            var lineNumber = compilationUnit.getLineMap().getLineNumber(pos);
            var lineStartPos = compilationUnit.getLineMap().getStartPosition(lineNumber);
            var nextLinePos = compilationUnit.getLineMap().getStartPosition(lineNumber + 1);
            var lineLength = nextLinePos - lineStartPos;

            String line = "";

            try (var fileReader = compilationUnit.getSourceFile().openReader(true)) {
                fileReader.skip(lineStartPos);
                while (!line.contains(searchFor)) {
                    lineStartPos = compilationUnit.getLineMap().getStartPosition(lineNumber);
                    nextLinePos = compilationUnit.getLineMap().getStartPosition(lineNumber + 1);
                    lineLength = nextLinePos - lineStartPos;
                    var buffer = new char[(int) lineLength];
                    var chars = fileReader.read(buffer);
                    if (chars < 1) return null;
                    line = new String(buffer);
                    lineNumber++;
                }
            } catch (IOException e) {
                // failed to read file
            }

            if (!line.isBlank() && line.contains(searchFor)) {
                return line.replaceAll("\\s++$", "");
            }

            return null;
        }

        public void log(Diagnostic.Kind kind, Object message, Throwable t) {
            if (messager == null || kind == null || message == null)
                return;

            if (element != null) {
                messager.printMessage(kind, message + "\n" + getElementDetail(element));
            } else {
                messager.printMessage(kind, message.toString());
            }

            if (t != null) {
                messager.printMessage(Diagnostic.Kind.ERROR, t.getMessage());
                t.printStackTrace(System.err);
            }
        }

        public String getElementDetail(Element element) {

            var path = envUtils.trees().getPath(element);
            var tree = envUtils.trees().getTree(element);

            if (path == null || tree == null)
                return element.toString();

            var filePath = path.getCompilationUnit().getSourceFile().toUri().getPath();

            var splitIndex = filePath.indexOf("/src/");
            if (splitIndex > 0)
                filePath = filePath.substring(splitIndex + 1);

            var pos = envUtils.trees().getSourcePositions().getStartPosition(path.getCompilationUnit(), tree);
            var line = path.getCompilationUnit().getLineMap().getLineNumber(pos);
            long column = 0;

            var lineContent = getLineFromPositionContaining(path.getCompilationUnit(), pos, element.getSimpleName().toString());
            if (lineContent == null || lineContent.isBlank()) {
                lineContent = element.toString();
                column = element.toString().lastIndexOf(element.getSimpleName().toString());
                column = Math.max(0, column);
            } else {
                column = lineContent.indexOf(element.getSimpleName().toString());
            }

            var arrow = " ".repeat((int) Math.min(column, Integer.MAX_VALUE)) + "^";

            return filePath + ":" + line + "\n  " + lineContent + "\n  " + arrow;
        }

        @Override
        public boolean isFatalEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public void fatal(Object message) {
            fatal(message, null);
        }

        @Override
        public void fatal(Object message, Throwable t) {
            log(Diagnostic.Kind.ERROR, "Fatal: " + message.toString(), t);
        }

        @Override
        public void error(Object message) {
            log(Diagnostic.Kind.ERROR, message, null);
        }

        public void throwError(Object message, PrototypeException<? extends RuntimeException> exception) {
            error(message);
            if (exception != null) {
                throw exception.create(message.toString());
            }
        }

        @Override
        public void error(Object message, Throwable t) {
            log(Diagnostic.Kind.ERROR, message, t);
        }

        @Override
        public void warn(Object message) {
            warn(message, null);
        }

        @Override
        public void warn(Object message, Throwable t) {
            log(Diagnostic.Kind.WARNING, message, t);
        }

        @Override
        public void info(Object message) {
            info(message, null);
        }

        @Override
        public void info(Object message, Throwable t) {
            log(Diagnostic.Kind.NOTE, message, t);
        }

        @Override
        public void debug(Object message) {
            debug(message, null);
        }

        @Override
        public void debug(Object message, Throwable t) {
            log(Diagnostic.Kind.NOTE, message, t);
        }

        @Override
        public void trace(Object message) {
            trace(message, null);
        }

        @Override
        public void trace(Object message, Throwable t) {
            log(Diagnostic.Kind.NOTE, message, t);
        }
    }
}
