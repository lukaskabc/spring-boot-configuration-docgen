package cz.lukaskabc.cvut.processor.descriptor;

import com.sun.source.tree.VariableTree;
import cz.lukaskabc.cvut.processor.EnvironmentUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.Map;

/**
 * Describes properties with a Collection or Map type
 */
public class CollectionPropertyDescriptor extends AbstractPropertyDescriptor {

    private final EnvironmentUtils envUtils;

    private final TypeMirror collectionType;

    private final TypeMirror mapType;

    public CollectionPropertyDescriptor(EnvironmentUtils envUtils) {
        super();
        this.envUtils = envUtils;

        TypeElement collection = envUtils.elements().getTypeElement(Collection.class.getName());
        TypeElement map = envUtils.elements().getTypeElement(Map.class.getName());
        var wildcard = envUtils.types().getWildcardType(null, null);

        collectionType = envUtils.types().getDeclaredType(collection, wildcard);
        mapType = envUtils.types().getDeclaredType(map, wildcard, wildcard);
    }

    @Override
    public boolean isProperty(VariableElement e) {
        if (isCollectionOrMap(e) && isInitialized(e)) {
            return hasGetter(e);
        }

        return super.isProperty(e);
    }

    public boolean isCollectionOrMap(VariableElement e) {
        if (e.asType().getKind() == TypeKind.DECLARED) {

            // if it's collection or map
            return envUtils.types().isAssignable(e.asType(), collectionType)
                    || envUtils.types().isAssignable(e.asType(), mapType);
        }
        return false;
    }

    public boolean isInitialized(VariableElement e) {
        var tree = (VariableTree) envUtils.trees().getTree(e);
        return tree.getInitializer() != null;
    }

    public boolean hasGetter(VariableElement e) {
        return hasGetter(e, getEnclosingClass(e));
    }
}
