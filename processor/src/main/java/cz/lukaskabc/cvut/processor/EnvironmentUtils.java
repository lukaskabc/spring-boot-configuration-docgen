package cz.lukaskabc.cvut.processor;

import com.sun.source.util.DocTrees;
import com.sun.source.util.Trees;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Wrapper around {@link ProcessingEnvironment} providing source utilities
 */
public record EnvironmentUtils(ProcessingEnvironment processingEnvironment,
                               Trees trees,
                               DocTrees docTrees,
                               Elements elements,
                               Types types) {

    public EnvironmentUtils(ProcessingEnvironment processingEnvironment) {
        this(processingEnvironment,
                Trees.instance(processingEnvironment),
                DocTrees.instance(processingEnvironment),
                processingEnvironment.getElementUtils(),
                processingEnvironment.getTypeUtils());
    }

}
