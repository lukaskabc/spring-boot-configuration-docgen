package cz.lukaskabc.cvut.processor.visitor;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.util.DocTreeScanner;
import cz.lukaskabc.cvut.processor.ProcessorConfiguration;

import java.util.Set;

public class JavadocDefaultTagVisitor extends DocTreeScanner<Void, Set<DocTree>> {
    public static final String DEFAULT_TAG_NAME = ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "default";

    @Override
    public Void visitUnknownBlockTag(UnknownBlockTagTree node, Set<DocTree> contents) {
        if (node.getTagName().equals(DEFAULT_TAG_NAME)) {
            contents.addAll(node.getContent());
        }
        return super.visitUnknownBlockTag(node, contents);
    }

    @Override
    public Void visitUnknownInlineTag(UnknownInlineTagTree node, Set<DocTree> contents) {
        if (node.getTagName().equals(DEFAULT_TAG_NAME)) {
            contents.addAll(node.getContent());
        }
        return super.visitUnknownInlineTag(node, contents);
    }
}
