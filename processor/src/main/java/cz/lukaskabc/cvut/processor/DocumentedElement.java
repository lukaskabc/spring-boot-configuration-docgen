package cz.lukaskabc.cvut.processor;

/**
 * Wrapper for {@link ElementDecorator} that holds generated documentation for the element.
 * Also provides accessors for fields available in template.
 */
public class DocumentedElement {

    private final ElementDecorator decorator;

    private final StringBuilder description = new StringBuilder();

    private final StringBuilder restrictions = new StringBuilder();

    private boolean isDeprecated = false;

    private boolean isRequired = false;

    private boolean isHidden = false;

    public DocumentedElement(ElementDecorator decorator) {
        this.decorator = decorator;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public boolean isRequired() {
        return isRequired;
    }


    public void setDeprecated() {
        isDeprecated = true;
    }

    public void setRequired() {
        isRequired = true;
    }

    public StringBuilder getDescriptionBuilder() {
        return description;
    }

    public String getDescription() {
        return description.toString();
    }

    public ElementDecorator getDecorator() {
        return decorator;
    }

    public StringBuilder getRestrictionsBuilder() {
        return restrictions;
    }

    public String getRestrictions() {
        return restrictions.toString();
    }

    public String getName() {
        return decorator.getConfigOptionName();
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden() {
        isHidden = true;
    }
}
