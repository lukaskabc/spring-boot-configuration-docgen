package cz.lukaskabc.cvut.processor.docsgenerator;

import cz.lukaskabc.cvut.processor.DocumentedElement;
import cz.lukaskabc.cvut.processor.formatter.Formatter;

/**
 * Generates documentation for single {@link DocumentedElement}
 */
public interface DocGenerator {

    boolean generate(Formatter builder, DocumentedElement element);
}
