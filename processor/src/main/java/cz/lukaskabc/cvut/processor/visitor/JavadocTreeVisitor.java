package cz.lukaskabc.cvut.processor.visitor;

import com.sun.source.doctree.*;
import com.sun.source.util.SimpleDocTreeVisitor;
import cz.lukaskabc.cvut.processor.Log;
import cz.lukaskabc.cvut.processor.formatter.Formatter;

import java.util.List;

/**
 * Generates documentation for Javadoc tags<br>
 * returns true if the @hidden tag is present
 */
public class JavadocTreeVisitor extends SimpleDocTreeVisitor<Boolean, Formatter> {

    public JavadocTreeVisitor() {
        super(false);
    }

    @Override
    protected Boolean defaultAction(DocTree node, Formatter formatter) {
        return false;
    }

    private Boolean append(DocTree node, Formatter formatter) {
        formatter.append(node.toString());
        return false;
    }

    /**
     * Visits an HTML attribute and appends it as string
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitAttribute(AttributeTree node, Formatter formatter) {
        return append(node, formatter);
    }

    /**
     * Visits all DocTrees from list
     *
     * @param list      the list of DocTrees
     * @param formatter String formatter to append the result
     */
    private Boolean visitAllFromList(List<? extends DocTree> list, Formatter formatter, String separator) {
        if (list == null)
            return false;

        var isHidden = false;

        for (var tree : list) {
            isHidden = this.visit(tree, formatter);
            if (isHidden)
                return true;
            if (separator != null)
                formatter.rawAppend(separator);
        }
        return false;
    }

    private Boolean visitAllFromList(List<? extends DocTree> list, Formatter formatter) {
        return visitAllFromList(list, formatter, null);
    }

    /**
     * Visits @deprecated tag
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitDeprecated(DeprecatedTree node, Formatter formatter) {
        formatter.rawAppend("Deprecated: ");
        return visitAllFromList(node.getBody(), formatter);
    }

    /**
     * Visit DocCommentTree (top level comment representation)
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitDocComment(DocCommentTree node, Formatter formatter) {
        var isHidden = false;
        isHidden = visitAllFromList(node.getFullBody(), formatter);
        if (!node.getBlockTags().isEmpty() && !isHidden) {
            formatter.paragraph();
            isHidden = visitAllFromList(node.getBlockTags(), formatter, formatter.linebreak());
        }
        return isHidden;
    }

    /**
     * Visits a start of HTML element "&lt;tag attributes&gt;", appends it as string
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitStartElement(StartElementTree node, Formatter formatter) {
        formatter.rawAppend(node.toString());
        return false;
    }

    @Override
    public Boolean visitEndElement(EndElementTree node, Formatter formatter) {
        formatter.rawAppend(node.toString());
        return false;
    }

    /**
     * Visits raw text node "just text", appends it as string
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitText(TextTree node, Formatter formatter) {
        formatter.append(node.getBody());
        return false;
    }

    /**
     * Visits unknown block tag and continue based on tag name
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitUnknownBlockTag(UnknownBlockTagTree node, Formatter formatter) {
        return switch (node.getTagName().trim().toLowerCase()) {
            case "param":
                yield this.visitParam((ParamTree) node, formatter);
            case "deprecated":
                yield this.visitDeprecated((DeprecatedTree) node, formatter);
            case "hidden":
                yield this.visitHidden((HiddenTree) node, formatter);
            case "see":
                yield this.visitSee((SeeTree) node, formatter);
            case "since":
                yield this.visitSince((SinceTree) node, formatter);
            case JavadocDefaultTagVisitor.DEFAULT_TAG_NAME:
                // implemented in DefaultValueCollector
                yield false;
            default:
                Log.instance().debug("Unimplemented Javadoc block tag " + node.getTagName());
                yield false;
        };
    }

    /**
     * Visits unknown inline tag and continue based on tag name
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitUnknownInlineTag(UnknownInlineTagTree node, Formatter formatter) {
        return switch (node.getTagName().trim().toLowerCase()) {
            case "literal", "code":
                yield this.visitLiteral((LiteralTree) node, formatter);
            case "link", "linkplain":
                yield this.visitLink((LinkTree) node, formatter);
            case JavadocDefaultTagVisitor.DEFAULT_TAG_NAME:
                // implemented in DefaultValueCollector
                yield false;
            default:
                Log.instance().debug("Unimplemented Javadoc inline tag " + node.getTagName());
                yield false;
        };
    }

    /**
     * Visits @link tag - appends only link label with code formatting.
     * <p>
     *
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitLink(LinkTree node, Formatter formatter) {
        var linkBuilder = formatter.emptyClone();
        formatter.rawAppend(" ");

        this.visit(node.getLabel(), linkBuilder);
        boolean isLabelEmpty = linkBuilder.toString().isBlank();

        if (!isLabelEmpty)
            linkBuilder.append(" (");

        visitReference(node.getReference(), linkBuilder);

        if (!isLabelEmpty)
            linkBuilder.append(")");

        formatter.code(linkBuilder.toString());
        return super.visitLink(node, formatter);
    }

    /**
     * Visits @literal @code tags and appends escaped contents
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitLiteral(LiteralTree node, Formatter formatter) {
        var content = formatter.emptyClone();
        formatter.rawAppend(" ");

        if (node.getKind() == DocTree.Kind.CODE) {
            content.code(node.getBody().getBody());
        } else {
            content.escape(node.getBody().getBody());
        }
        // DocTree.Kind.LITERAL has no additional formatting

        formatter.append(content.toString());

        return false;
    }

    /**
     * Visits @param tag, visits it's content
     *
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitParam(ParamTree node, Formatter formatter) {
        return visitAllFromList(node.getDescription(), formatter);
    }

    /**
     * Visits @since tag, appends "Since: " and it's content
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitSince(SinceTree node, Formatter formatter) {
        formatter.rawAppend("Since: ");
        return visitAllFromList(node.getBody(), formatter);
    }

    /**
     * Visits @see tag and append it's content
     * @param node      the node being visited
     * @param formatter String formatter to append the result
     */
    @Override
    public Boolean visitSee(SeeTree node, Formatter formatter) {
        formatter.rawAppend("See: ");
        return visitAllFromList(node.getReference(), formatter);
    }

    @Override
    public Boolean visitReference(ReferenceTree node, Formatter formatter) {
        formatter.escape(node.getSignature());
        return false;
    }

    /**
     * Visit @hidden block tag
     *
     * @return true as hidden is present
     */
    @Override
    public Boolean visitHidden(HiddenTree node, Formatter formatter) {
        return true;
    }
}
