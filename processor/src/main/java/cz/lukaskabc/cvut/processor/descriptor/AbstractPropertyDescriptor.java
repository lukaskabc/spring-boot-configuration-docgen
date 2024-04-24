package cz.lukaskabc.cvut.processor.descriptor;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

/**
 * Describes an Element, whether it is a bindable property or not.
 */
public abstract class AbstractPropertyDescriptor implements PropertyDescriptor {

    private AbstractPropertyDescriptor next;

    public static Optional<VariableElement> getMethodParam(ExecutableElement method, String fieldName, TypeMirror fieldType, Types typeUtils) {
        return method.getParameters().stream() // stream of constructor parameters
                .filter(el -> el.getSimpleName().toString().equals(fieldName)) // parameter name matches field name
                .filter(el -> typeUtils.isSameType(el.asType(), fieldType)) // parameter type matches the field type
                .map(el -> (VariableElement) el)
                .findFirst(); // parameter type matches the field type
    }

    public static TypeElement getEnclosingClass(VariableElement e) {
        var enclosing = e.getEnclosingElement();
        while (!enclosing.getKind().isClass()) {
            if (enclosing.getEnclosingElement() == null) {
                throw new IllegalStateException("No enclosing class found for " + e.getSimpleName());
            }
            enclosing = enclosing.getEnclosingElement();
        }
        return (TypeElement) enclosing;
    }

    public static boolean hasMethod(Element e, String methodName) {
        return e.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.METHOD)
                .map(el -> (ExecutableElement) el)
                .anyMatch(el -> el.getSimpleName().toString().equals(methodName));
    }

    @Override
    public boolean isProperty(VariableElement e) {
        if (next != null) {
            return next.isProperty(e);
        }

        return false;
    }

    protected boolean hasGetter(VariableElement e, TypeElement enclosing) {
        if (next != null) {
            return next.hasGetter(e, enclosing);
        }

        return false;
    }

    protected boolean hasSetter(VariableElement e, TypeElement enclosing) {
        if (next != null) {
            return next.hasSetter(e, enclosing);
        }

        return false;
    }

    public void setNext(AbstractPropertyDescriptor next) {
        this.next = next;
    }
}
