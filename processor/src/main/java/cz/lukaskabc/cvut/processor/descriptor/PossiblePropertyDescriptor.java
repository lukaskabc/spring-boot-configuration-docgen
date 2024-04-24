package cz.lukaskabc.cvut.processor.descriptor;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * Checks if a field is a record component, then returns true.
 * Otherwise, it checks if the field isn't static and is in a class.
 * Returns false if it is static or not in a class.
 * Calls next otherwise.
 */
public class PossiblePropertyDescriptor extends AbstractPropertyDescriptor {

    @Override
    public boolean isProperty(VariableElement e) {
        // record component is property (although VariableElement should never be record component)
        if (isRecordComponent(e))
            return true;

        if (!e.getKind().isField() // not a field
                || e.getModifiers().contains(Modifier.STATIC) // static field
                || e.getEnclosingElement().getKind() != ElementKind.CLASS) // not in a class
            return false;

        return super.isProperty(e);
    }

    public boolean isRecordComponent(VariableElement e) {
        return e.getEnclosingElement().getKind() == ElementKind.RECORD
                && e.getKind() == ElementKind.RECORD_COMPONENT;
    }


}
