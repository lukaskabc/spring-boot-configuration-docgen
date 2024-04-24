package cz.lukaskabc.cvut.processor.configuration.tests;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransparentDiagnosticCollector implements DiagnosticListener<JavaFileObject> {
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        String msg =
                "[" + diagnostic.getKind() + "] " +
                        diagnostic.getMessage(null);

        System.err.println(msg);

        diagnostics.add(diagnostic);
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }
}
