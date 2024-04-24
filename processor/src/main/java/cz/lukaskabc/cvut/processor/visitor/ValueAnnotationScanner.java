package cz.lukaskabc.cvut.processor.visitor;

import com.sun.source.doctree.DocTree;
import cz.lukaskabc.cvut.processor.ElementDecorator;
import cz.lukaskabc.cvut.processor.EnvironmentUtils;
import cz.lukaskabc.cvut.processor.Log;
import org.springframework.beans.factory.annotation.Value;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor14;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Recursively traverses all elements and collects elements with @{@link Value} annotation
 */
public class ValueAnnotationScanner extends SimpleElementVisitor14<Void, List<ElementDecorator>> {

    private final EnvironmentUtils envUtils;

    public ValueAnnotationScanner(EnvironmentUtils envUtils) {
        this.envUtils = envUtils;
    }

    /**
     * @return true if the element is not annotated with @{@link Value}
     */
    private boolean noValueAnnotation(Element element) {
        return element.getAnnotation(Value.class) == null;
    }

    /**
     * Extracts configuration name from @{@link Value} annotation
     * <p>
     * Supports only format {@code ${configuration.name:default value}}
     *
     * @param element with @{@link Value} annotation
     * @return String with configuration name
     */
    private Optional<String> getConfigurationFromValueAnnotation(Element element) {
        var annotation = element.getAnnotation(Value.class);
        var value = annotation.value();

        if (value.charAt(0) != '$' && value.charAt(1) != '{' && value.charAt(value.length() - 1) != '}') {
            Log.withContext(element).warn("Skipping @Value annotation (only supports format ${path.to.value:defaultValue})");
            return Optional.empty();
        }

        value = value.substring(2); // removes ${ from beginning

        int index = 0;
        char currentCharacter;

        Set<Character> allowedChars = Set.of('-', '.', '_');

        while (index < value.length()) {
            currentCharacter = value.charAt(index);

            // if character is allowed or alphanumeric, continue to the next one
            if (Character.isAlphabetic(currentCharacter) ||
                    Character.isDigit(currentCharacter) ||
                    allowedChars.contains(currentCharacter)) {
                index++;
            } else {
                break;
            }
        }

        String configName = value.substring(0, index);

        return Optional.of(configName);
    }

    /**
     * Visits method with @{@link Value} annotation<br>
     * Checks if there are any parameters without {@link Value @Value} annotation
     * and if so, add the config property to the list of decorators
     * Visit its parameters otherwise
     */
    @Override
    public Void visitExecutable(ExecutableElement e, List<ElementDecorator> decorators) {
        if (noValueAnnotation(e))
            return super.visitExecutable(e, decorators);

        if (e.getParameters().isEmpty()) {
            Log.withContext(e).warn("No parameters found in method annotated with @Value! (skipping)");
            return super.visitExecutable(e, decorators);
        }

        // its method with @Value
        // https://gitlab.com/lukaskabc/bp-spring-markdown-docs/-/blob/0cd0d5a87e8fc7d681e68960cb10960006892b60/document/tex/technology_list.tex#L152
        /*
        @Value()
        Type method(@Value Param param, Param param2){};
         */
        // check if there are some "free" parameters without @Value annotation
        VariableElement freeParam = null;
        for (var param : e.getParameters()) {
            if (param.getAnnotation(Value.class) == null) {
                freeParam = param;
                break;
            }
        }

        if (freeParam == null) {
            Log.withContext(e).warn("No parameters without @Value annotation found in method! (skipping)");
            return super.visitExecutable(e, decorators);
        }

        var configName = getConfigurationFromValueAnnotation(e);
        if (configName.isEmpty())
            return super.visitExecutable(e, decorators);

        var commentTree = envUtils.docTrees().getDocCommentTree(e);
        var methodComment = new ArrayList<DocTree>();
        if (commentTree != null) {
            methodComment.addAll(commentTree.getBody());
        }
        var decorator = new ElementDecorator(freeParam, configName.get(), envUtils, methodComment);
        decorators.add(decorator);

        return null;
    }

    @Override
    public Void visitVariable(VariableElement e, List<ElementDecorator> decorators) {
        if (noValueAnnotation(e))
            return super.visitVariable(e, decorators);

        return switch (e.getKind()) {
            case FIELD -> visitValueAttribute(e, decorators);
            case PARAMETER -> visitValueParameter(e, decorators);
            default -> super.visitVariable(e, decorators);
        };
    }

    private Void visitValueAttribute(VariableElement e, List<ElementDecorator> decorators) {
        // class attribute with @Value annotation
        var configName = getConfigurationFromValueAnnotation(e);
        if (configName.isEmpty())
            return super.visitVariable(e, decorators);

        decorators.add(new ElementDecorator(e, configName.get(), envUtils, List.of()));
        return super.visitVariable(e, decorators);
    }

    private Void visitValueParameter(VariableElement e, List<ElementDecorator> decorators) {
        // method parameter with @Value annotation
        // Type method(@Value Param param){};

        var configName = getConfigurationFromValueAnnotation(e);
        if (configName.isEmpty())
            return super.visitVariable(e, decorators);

        var method = (ExecutableElement) e.getEnclosingElement(); // method or constructor
        if (method.getKind() != ElementKind.METHOD && method.getKind() != ElementKind.CONSTRUCTOR)
            throw new IllegalStateException("Method parameter not enclosed by method (" + method.getKind() + ")");

        var methodDocTree = envUtils.docTrees().getDocCommentTree(method);
        var docTree = ElementDecorator.getParamDocTagTree(methodDocTree, e.getSimpleName());

        decorators.add(new ElementDecorator(e, configName.get(), docTree));

        return super.visitVariable(e, decorators);
    }

}
