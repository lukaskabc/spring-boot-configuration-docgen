package cz.lukaskabc.cvut.processor.descriptor;

import cz.lukaskabc.cvut.processor.EnvironmentUtils;
import cz.lukaskabc.cvut.processor.Log;
import cz.lukaskabc.cvut.processor.exception.InvalidSpringConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Optional;

/**
 * Checks whether there is annotated constructor
 * and whether there is a parameter matching the field name and type<br>
 * returns true if parameter is present, false if its not present <br>
 * calls next when annotated constructor is not found
 */
public class AnnotatedConstructorPropertyDescriptor extends AbstractConstructorPropertyDescriptor {

    public AnnotatedConstructorPropertyDescriptor(EnvironmentUtils envUtils) {
        super(envUtils);
    }

    @Override
    public Optional<ExecutableElement> getConstructor(TypeElement e) {
        // TODO: check deprecated ConstructorBinding (according to supported version if its needed)
        var constructors = e.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.CONSTRUCTOR)
                .map(el -> (ExecutableElement) el)
                .filter(el -> el.getAnnotation(ConstructorBinding.class) != null)
                .toList();
        if (constructors.size() > 1) {
            Log.withContext(e)
                    .throwError("Multiple constructors with ConstructorBinding annotation, consult Spring Boot documentation for correct usage",
                            new InvalidSpringConfiguration());
        } else if (constructors.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(constructors.get(0));
    }

    @Override
    public boolean shouldCallSuper(ExecutableElement constructor, VariableElement attribute) {
        var anyAutowiredConstructorPresent = constructor.getEnclosingElement().getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .anyMatch(c -> c.getAnnotation(Autowired.class) != null);
        if (anyAutowiredConstructorPresent) {
            Log.withContext(constructor).throwError("Class " + constructor.getEnclosingElement().getSimpleName() +
                    " declares @Autowired and @ConstructorBinding constructor", new InvalidSpringConfiguration());
        }

        if (constructor.getParameters().isEmpty()) {
            Log.withContext(constructor).warn("Constructor with ConstructorBinding annotation has no parameters");
        }

        return false;
    }
}
