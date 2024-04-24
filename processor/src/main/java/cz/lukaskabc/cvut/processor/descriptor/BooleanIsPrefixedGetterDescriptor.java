package cz.lukaskabc.cvut.processor.descriptor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Describers getters prefixed with "is" instead of "get".
 */
public class BooleanIsPrefixedGetterDescriptor extends AbstractPropertyDescriptor {

    @Override
    protected boolean hasGetter(VariableElement e, TypeElement enclosing) {
        var firstLetter = Character.toUpperCase(e.getSimpleName().charAt(0));
        var getterName = "is" + firstLetter + e.getSimpleName().subSequence(1, e.getSimpleName().length());
        if (hasMethod(enclosing, getterName)) return true;

        return super.hasGetter(e, enclosing);
    }
}
