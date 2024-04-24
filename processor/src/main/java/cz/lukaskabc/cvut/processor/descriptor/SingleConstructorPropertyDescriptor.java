package cz.lukaskabc.cvut.processor.descriptor;

import cz.lukaskabc.cvut.processor.EnvironmentUtils;
import cz.lukaskabc.cvut.processor.Log;
import cz.lukaskabc.cvut.processor.exception.InvalidSpringConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Optional;

/**
 * Describes a properties that are bindable by a constructor parameter in class
 * where only a single non-default constructor is present.
 */
public class SingleConstructorPropertyDescriptor extends AbstractConstructorPropertyDescriptor {

    public SingleConstructorPropertyDescriptor(EnvironmentUtils envUtils) {
        super(envUtils);
    }

    @Override
    public Optional<ExecutableElement> getConstructor(TypeElement e) {
        var multiple = getConstructors(e);
        if (multiple.isEmpty()) {
            return Optional.empty();
        }

        if (multiple.size() > 1) {
            if (multiple.stream().noneMatch(c -> c.getParameters().isEmpty())) { // there is no default constructor!
                Log.withContext(e)
                        .throwError("Multiple constructors found without ConstructorBinding annotation. Introduce default constructor or use ConstructorBinding annotation.",
                                new InvalidSpringConfiguration());

            }
            return Optional.empty();
        }
        return Optional.of(multiple.get(0));
    }

    @Override
    public boolean shouldCallSuper(ExecutableElement constructor, VariableElement attribute) {
        return constructor.getParameters().isEmpty() || constructor.getAnnotation(Autowired.class) != null;
    }

    public List<ExecutableElement> getConstructors(TypeElement e) {
        return e.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.CONSTRUCTOR)
                .map(el -> (ExecutableElement) el)
                .toList();
    }


}
