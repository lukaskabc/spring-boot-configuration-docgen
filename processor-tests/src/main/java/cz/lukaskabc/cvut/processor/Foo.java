package cz.lukaskabc.cvut.processor;
// Ukázka javadoc komentáře

/**
 * Text dokumentace
 * <p>
 * <b>Umožňuje HTML formátování {@code inline code}</b>
 *
 * @deprecated více informací
 */
public class Foo {
    private String attribute;

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        System.out.println("attribute = " + attribute);
    }
}