package cz.lukaskabc.cvut.processor.docsgenerator;

import cz.lukaskabc.cvut.processor.DocumentedElement;
import cz.lukaskabc.cvut.processor.ElementDecorator;
import cz.lukaskabc.cvut.processor.formatter.Formatter;
import cz.lukaskabc.cvut.processor.visitor.JavadocTreeVisitor;

/**
 * Generates documentation for {@link ElementDecorator}, includes javadoc tags and JSR-303 annotations
 */
public class ElementDecoratorDocGenerator {

    private final JavadocTreeVisitor javadocFormatter;

    private final JSR303DocsGenerator jsr303DocsGenerator;

    private final Formatter formatter;

    public ElementDecoratorDocGenerator(Formatter formatter) {
        this.formatter = formatter;
        javadocFormatter = new JavadocTreeVisitor();
        jsr303DocsGenerator = new JSR303DocsGenerator();
    }

    /**
     * Generates documentation for {@link ElementDecorator}, includes javadoc tags and JSR-303 annotations
     *
     * @return {@link DocumentedElement} with generated documentation or null if the element is hidden
     */
    public DocumentedElement generate(ElementDecorator element) {
        var documented = new DocumentedElement(element);
        var optionalTree = element.getDocTree();
        var description = formatter.emptyClone();
        boolean isHidden = false;

        if (optionalTree.isPresent()) { // only if there is javadoc
            var rootTree = optionalTree.get();

            isHidden = javadocFormatter.visit(rootTree, description);
            if (isHidden) {
                documented.setHidden();
            }
        }

        if (!element.getAdditionalDocTrees().isEmpty() && !isHidden) {
            if (!description.toString().isBlank()) // if there is a description before this one, add a paragraph
                description.paragraph();

            var it = element.getAdditionalDocTrees().iterator();
            while (it.hasNext()) {
                var additionalDocTree = it.next();

                var additionalFormatter = formatter.emptyClone();
                if (additionalDocTree != null)
                    isHidden = javadocFormatter.visit(additionalDocTree, additionalFormatter);

                if (isHidden) {
                    documented.setHidden();
                    break;
                }

                // if not yet present and not empty
                if (!isCommentAlreadyPresent(additionalFormatter.toString(), description.toString()) && !additionalFormatter.toString().isBlank()) {
                    description.append(additionalFormatter.toString());

                    if (it.hasNext())
                        description.paragraph();
                }
            }
        }

        var restrictions = formatter.emptyClone();

        if (element.getDefaultValue().isPresent()) {
            var value = element.getDefaultValue().get();
            if (!value.isBlank() && !value.equals("\"\"")) {
                restrictions.rawAppend("Default value: ")
                        .code(value)
                        .newline();
            }
        }

        jsr303DocsGenerator.generate(restrictions, documented);
        checkDeprecated(documented);

        if (documented.isHidden()) {
            return null;
        }

        var desc = finalDocsStringFormat(description.toString());
        var restrict = finalDocsStringFormat(restrictions.toString());

        documented.getDescriptionBuilder().append(desc);
        documented.getRestrictionsBuilder().append(restrict);

        return documented;
    }

    private boolean isCommentAlreadyPresent(String needle, String haystack) {
        return haystack.replaceAll("\\s|(<br>)", "").toLowerCase().contains(needle.replaceAll("\\s|(<br>)", "").toLowerCase());
    }

    private String finalDocsStringFormat(String text) {
        text = formatter.removeDoubleSpaces(text);
        text = formatter.removeLeadingSpacesAndLineBreaks(text);
        return text;
    }

    private void checkDeprecated(DocumentedElement element) {
        if (element.getDecorator().getElement().getAnnotation(Deprecated.class) != null) {
            element.setDeprecated();
        }
    }
}
