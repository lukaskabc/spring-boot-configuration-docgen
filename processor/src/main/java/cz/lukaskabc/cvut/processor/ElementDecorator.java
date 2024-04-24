package cz.lukaskabc.cvut.processor;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Wrapper for {@link Element}
 */
public class ElementDecorator {

    private final Element element;

    private final String configOptionName;

    private final DocTree docTree;

    private final Set<DocTree> additionalDocTrees = new LinkedHashSet<>();

    private String defaultValue = null;

    public ElementDecorator(Element element, String configOptionName, EnvironmentUtils envUtils, List<DocTree> additionalDocTrees) {
        this.element = element;
        this.configOptionName = NameFormatter.combine("", configOptionName); // ensures correct format
        this.additionalDocTrees.addAll(additionalDocTrees);

        DocTree elDocTree = envUtils.docTrees().getDocCommentTree(element);

        if (elDocTree == null && element.getKind() == ElementKind.PARAMETER) {
            elDocTree = getParamDocTagTree((VariableElement) element, envUtils.docTrees());
        }

        if (elDocTree == null && isRecordComponent()) {
            this.docTree = getRecordComponentDocTree(envUtils.docTrees());
        } else {
            this.docTree = elDocTree;
        }
    }

    public ElementDecorator(Element element, String configOptionName, DocTree docTree) {
        this.element = element;
        this.configOptionName = NameFormatter.combine("", configOptionName);
        this.docTree = docTree;
    }

    public static ParamTree getParamDocTagTree(VariableElement parameter, DocTrees docTrees) {
        assert parameter.getKind() == ElementKind.PARAMETER;
        var tree = docTrees.getDocCommentTree(parameter.getEnclosingElement());
        return getParamDocTagTree(tree, parameter.getSimpleName());
    }

    public static ParamTree getParamDocTagTree(DocCommentTree tree, Name paramName) {

        if (tree != null) {
            for (var blockTag : tree.getBlockTags()) {
                if (blockTag.getKind() == DocTree.Kind.PARAM) {
                    var paramTree = (ParamTree) blockTag;
                    if (paramTree.getName().getName().equals(paramName)) {
                        return paramTree;
                    }
                }
            }
        }
        return null;
    }

    /**
     * When an element is a record component, then returns DocTree from @param block tag
     * returns null otherwise
     */
    private DocTree getRecordComponentDocTree(DocTrees docTrees) {
        if (!isRecordComponent()) return null;
        var rec = element.getEnclosingElement();

        return getParamDocTagTree(docTrees.getDocCommentTree(rec), element.getSimpleName());
    }

    public void addDocTree(DocTree docTree) {
        additionalDocTrees.add(docTree);
    }

    public Set<DocTree> getAdditionalDocTrees() {
        return additionalDocTrees;
    }

    public Optional<DocTree> getDocTree() {
        return Optional.ofNullable(docTree);
    }

    public Element getElement() {
        return element;
    }

    public String getConfigOptionName() {
        return configOptionName;
    }

    private boolean isRecordComponent() {
        return element.getEnclosingElement().getKind() == ElementKind.RECORD;
    }

    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
