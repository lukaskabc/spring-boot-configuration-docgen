package cz.lukaskabc.cvut.processor.descriptor;

import cz.lukaskabc.cvut.processor.EnvironmentUtils;
import cz.lukaskabc.cvut.processor.Log;
import org.springframework.boot.context.properties.bind.Name;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Optional;

/**
 * Abstract descriptor for properties bindable through constructor parameters.
 */
public abstract class AbstractConstructorPropertyDescriptor extends AbstractPropertyDescriptor {

    protected final EnvironmentUtils envUtils;

    protected AbstractConstructorPropertyDescriptor(EnvironmentUtils envUtils) {
        this.envUtils = envUtils;
    }

    public abstract Optional<ExecutableElement> getConstructor(TypeElement e);

    public abstract boolean shouldCallSuper(ExecutableElement constructor, VariableElement attribute);

    @Override
    public final boolean isProperty(VariableElement e) {
        return isProperty(e, false);
    }

    public final boolean isProperty(VariableElement e, boolean allowThrow) {
        var enclosingClass = getEnclosingClass(e);
        var optionalConstructor = getConstructor(enclosingClass);
        if (optionalConstructor.isEmpty()) {
            return super.isProperty(e);
        }

        var constructor = optionalConstructor.get();

        if (shouldCallSuper(constructor, e)) {
            return super.isProperty(e);
        }

        var parameter = getMethodParam(constructor, e.getSimpleName().toString(), e.asType(), envUtils.types());
        if (parameter.isEmpty()) {
            return false;
        }

        var nameAnnotation = parameter.get().getAnnotation(Name.class);
        if (nameAnnotation == null) {
            // no @Name annotation, use parameter name and so it exists
            return true;
        }

        if (nameAnnotation.value().isBlank() || nameAnnotation.value().equals(e.getSimpleName().toString())) {
            // @Name annotation actually matches the parameter name
            Log.withContext(parameter.get()).warn("Parameter has @Name annotation with same value as parameter name, this is redundant and can be removed.");
            return true;
        }

        // @Name annotation value is different from parameter name
        if (allowThrow) {
            throw new PropertyHasNameAnnotationException(nameAnnotation.value(), parameter.get());
        }

        return false;
    }

    /**
     * Exception thrown when a constructor parameter has a @Name annotation with a value different from the parameter name.
     */
    public static class PropertyHasNameAnnotationException extends RuntimeException {

        private final String annotationValue;

        private final VariableElement parameter;

        public PropertyHasNameAnnotationException(String annotationValue, VariableElement parameter) {
            this.annotationValue = annotationValue;
            this.parameter = parameter;
        }

        public String getAnnotationValue() {
            return annotationValue;
        }

        public VariableElement getParameter() {
            return parameter;
        }
    }
}
