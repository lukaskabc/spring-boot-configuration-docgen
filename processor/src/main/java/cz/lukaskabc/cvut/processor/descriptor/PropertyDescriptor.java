package cz.lukaskabc.cvut.processor.descriptor;

import javax.lang.model.element.VariableElement;

/**
 * Interface used to describe an Element from Java language model.
 * Especially check if given variable is bindable configuration property.
 */
public interface PropertyDescriptor {

    /**
     * Checks whether the given VariableElement is a bindable configuration property.
     *
     * @param e VariableElement to check
     * @return true if the given element is a bindable configuration property, false otherwise
     */
    boolean isProperty(VariableElement e);
}
