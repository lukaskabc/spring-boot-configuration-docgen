package cz.lukaskabc.cvut.processor.descriptor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Describes JavaBean properties.
 * Bindable property is a field with a setter and optionally a getter.
 */
public class JavaBeanPropertyDescriptor extends AbstractPropertyDescriptor {

    private final boolean requireGetter;

    public JavaBeanPropertyDescriptor(boolean requireGetter) {
        this.requireGetter = requireGetter;
    }

    @Override
    public boolean isProperty(VariableElement e) {
        var enclosingClass = getEnclosingClass(e);
        if (hasSetter(e, enclosingClass)) {
            return hasGetter(e, enclosingClass) || !requireGetter;
        }

        return super.isProperty(e);
    }

    @Override
    protected boolean hasGetter(VariableElement e, TypeElement enclosing) {
        var firstLetter = Character.toUpperCase(e.getSimpleName().charAt(0));
        var getterName = "get" + firstLetter + e.getSimpleName().subSequence(1, e.getSimpleName().length());
        if (hasMethod(enclosing, getterName)) return true;

        return super.hasGetter(e, enclosing);
    }

    @Override
    protected boolean hasSetter(VariableElement e, TypeElement enclosing) {
        if (e.getModifiers().contains(Modifier.FINAL)) return false; // final fields cannot have setters

        var firstLetter = Character.toUpperCase(e.getSimpleName().charAt(0));
        var setterName = "set" + firstLetter + e.getSimpleName().subSequence(1, e.getSimpleName().length());
        if (hasMethod(enclosing, setterName)) return true;

        return super.hasSetter(e, enclosing);
    }
}
