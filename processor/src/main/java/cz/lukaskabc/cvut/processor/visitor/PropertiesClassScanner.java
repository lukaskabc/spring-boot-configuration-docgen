package cz.lukaskabc.cvut.processor.visitor;

import com.sun.source.doctree.DocTree;
import cz.lukaskabc.cvut.processor.ElementDecorator;
import cz.lukaskabc.cvut.processor.EnvironmentUtils;
import cz.lukaskabc.cvut.processor.Log;
import cz.lukaskabc.cvut.processor.NameFormatter;
import cz.lukaskabc.cvut.processor.descriptor.*;
import cz.lukaskabc.cvut.processor.docsgenerator.JSR303DocsGenerator;
import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.annotation.Validated;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner14;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Traverse class with
 * {@link org.springframework.boot.context.properties.ConfigurationProperties @ConfigurationProperties} annotation
 * and collects its properties using descriptors.
 */
public class PropertiesClassScanner extends ElementScanner14<Void, PropertiesClassScanner.Params> {

    /**
     * Spring boot conversion service implementation
     */
    private final ConversionService conversionService = new ApplicationConversionService();

    /**
     * General property descriptor chain
     */
    private final PropertyDescriptor propertyDescriptor;

    /**
     * Descriptors for retrieving constructors
     */
    private final AnnotatedConstructorPropertyDescriptor annotatedConstructorDescriptor;

    /**
     * Descriptors for retrieving constructors
     */
    private final SingleConstructorPropertyDescriptor singleConstructorDescriptor;

    /**
     * Descriptor for collection properties
     */
    private final CollectionPropertyDescriptor collectionPropertyDescriptor;

    /**
     * JavaBean properties descriptor
     */
    private final PropertyDescriptor javabeanPropertyDescriptor;

    /**
     * Source utilities
     */
    private final EnvironmentUtils envUtils;

    private final String envPrefix;

    public PropertiesClassScanner(EnvironmentUtils envUtils, boolean requirePropertyGetters, String envPrefix) {
        this.envUtils = envUtils;
        this.envPrefix = envPrefix;

        var annotatedConstructor = new AnnotatedConstructorPropertyDescriptor(envUtils);
        var singleConstructor = new SingleConstructorPropertyDescriptor(envUtils);
        var possiblePropertyDescriptor = new PossiblePropertyDescriptor();
        var collectionDescriptor = new CollectionPropertyDescriptor(envUtils);
        var javabean = new JavaBeanPropertyDescriptor(requirePropertyGetters);
        var booleanIsPrefixedGetter = new BooleanIsPrefixedGetterDescriptor();
        // the order matters, constructors have to be checked before setters

        /*
        Chain of descriptors for properties:

        Annotated constructor - there is constructor annotated with @ConstructorBinding
        ↓
        Single (non-default) constructor
        ↓
        Possible property - check if an element is a record component or a non-static class field
        ↓
        Collection - check if an element is a collection or a map and has a getter
        ↓
        JavaBean - check if an element has getter and setter
        ↓
        boolean getter - boolean getters may be prefixed with "is" instead of "get"

         */

        annotatedConstructor.setNext(singleConstructor);
        singleConstructor.setNext(possiblePropertyDescriptor);

        possiblePropertyDescriptor.setNext(collectionDescriptor);
        collectionDescriptor.setNext(javabean);
        javabean.setNext(booleanIsPrefixedGetter);
        this.propertyDescriptor = annotatedConstructor;
        this.collectionPropertyDescriptor = collectionDescriptor;
        this.javabeanPropertyDescriptor = possiblePropertyDescriptor;

        this.annotatedConstructorDescriptor = new AnnotatedConstructorPropertyDescriptor(envUtils);
        this.singleConstructorDescriptor = new SingleConstructorPropertyDescriptor(envUtils);
    }

    public static boolean hasSpringValidatedAnnotation(TypeElement element) {
        return element.getAnnotation(Validated.class) != null;
    }

    /**
     * Entry point of the scanner
     * <p>
     * Scans the class structure and collects bindable properties
     * those are wrapped in {@link ElementDecorator} and stored in the list
     *
     * @param element                 Element with {@link ConfigurationProperties @ConfigurationProperties} annotation to scan
     * @param annotation              Annotation instance present on the element
     * @param decorators              List for storing the decorators (wrapped properties)
     * @param additionalDoc           List of additional documentation comments to append to the properties
     * @param allowConstructorBinding Whether to allow constructor binding or force using setters
     *                                (which is required when binding to already existing instances
     *                                - for example, in a case of binding to bean method result)
     * @implNote The point of passing the annotation instance at its own is
     * to support a visiting methods return type and persisting the information about the annotation like prefix
     * (annotation is on the method, not the type we are visiting)
     */
    public void visitConfiguration(TypeElement element, Element annotatedElement, ConfigurationProperties annotation, List<ElementDecorator> decorators, List<DocTree> additionalDoc, boolean allowConstructorBinding) {
        PropertyDescriptor descriptor;
        if (allowConstructorBinding) {
            descriptor = propertyDescriptor;
        } else {
            descriptor = javabeanPropertyDescriptor;
        }

        checkForValidAnnotation(element);

        var annotationPrefix = NameFormatter.firstNonEmpty(annotation.prefix(), annotation.value());
        var prefix = NameFormatter.combine(envPrefix, annotationPrefix);
        var params = new Params(descriptor, decorators, prefix, annotatedElement, null, new ArrayList<>(additionalDoc), hasSpringValidatedAnnotation(element));

        this.visitType(element, params);
    }

    /**
     * Checks whether the element is annotated with {@link Valid @Valid} annotation (which is wrong usage).
     * If so, warning is generated to use {@link Validated @Validated} instead.
     */
    public void checkForValidAnnotation(TypeElement element) {
        if (element.getAnnotation(Valid.class) != null) {
            Log.withContext(element).warn("ConfigurationProperties structure is annotated with @Valid, this is probably wrong usage, Spring's @Validated should be used instead (org.springframework.validation.annotation.Validated)");
        }
    }

    /**
     * If validation is performed from upper context (enclosing class),
     * and the provided element does not have {@link Valid @Valid} annotation,
     * note is generated with recommendation to use it.
     *
     * @param element nested structure
     */
    public void recommendValidAnnotation(Element element, Params params) {
        if (!params.validationActive())
            return;

        if (element.getAnnotation(Valid.class) != null)
            return;

        Log.withContext(element).info("Consider using @Valid annotation on this property");
    }

    @Override
    public Void visitType(TypeElement e, Params params) {
        this.visitEnclosedFields(e, params);
        return null; // no super call (we don't need to visit all subclasses and methods)
    }

    /**
     * Visit all enclosed fields, record components, and constructors
     *
     * @param e element to visit
     */
    public void visitEnclosedFields(Element e, Params params) {
        for (var enclosed : e.getEnclosedElements()) {
            if (hasRecursiveType(enclosed, params)) {
                // visiting recursive types will result in stack overflow
                var nestingAdditionalDocs = new ArrayList<>(params.additionalDoc);
                var nestingParams = new Params(params.propertyDescriptor, params.decorators, params.namePrefix, params.annotatedElement, params.nameOverride, nestingAdditionalDocs, params.validationActive);
                addConstructorParameterForAttribute((VariableElement) enclosed, nestingParams);
                addVariableAsDecorator(enclosed, nestingParams);
                Log.withContext(enclosed).info("Documenting recursive type, ensure proper documentation is provided for this property");
                continue;
            }
            if (enclosed.getKind().isField()
                    || enclosed.getKind() == ElementKind.RECORD_COMPONENT
                    || enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                this.visit(enclosed, params);
            }
        }
    }

    /**
     * @return true if the type of the element matches any it's enclosing classes, false otherwise
     */
    public boolean hasRecursiveType(Element e, Params params) {
        var type = e.asType();
        var parent = e.getEnclosingElement();
        while (parent != null && parent.getKind().isClass()) {
            if (envUtils.types().isSameType(parent.asType(), type)) {
                recommendValidAnnotation(e, params);
                return true;
            }
            parent = parent.getEnclosingElement();
        }
        return false;
    }

    /**
     * Visits a variable<br>
     * creating nested params<br>
     * adding corresponding constructor parameter<br>
     * and visiting the variable if its considered as property
     *
     * @param e the element to visit
     */
    @Override
    public Void visitVariable(VariableElement e, Params params) {
        var type = e.asType();
        var nestingAdditionalDocs = new ArrayList<>(params.additionalDoc);
        var nestingParams = new Params(params.propertyDescriptor, params.decorators, params.namePrefix, params.annotatedElement, params.nameOverride, nestingAdditionalDocs, params.validationActive);

        addConstructorParameterForAttribute(e, nestingParams);

        if (!params.propertyDescriptor.isProperty(e)) {
            return super.visitVariable(e, nestingParams);
        }

        if(!params.validationActive() && !JSR303DocsGenerator.getAnnotations(e).isEmpty()) {
            if (params.annotatedElement.getAnnotation(Validated.class) == null) {
                Log.withContext(params.annotatedElement).warn("Element is missing @Validated annotation (org.springframework.validation.annotation.Validated).\nThis warning is shown because configuration class contains attribute with JSR-303 annotation (" + e.asType().toString() + " " + e.getSimpleName() + "), but validation is not triggered.");
            } else {
                Log.withContext(e).warn("Found JSR-303 annotation on attribute in configuration class, but validation is not triggered.\nAre you missing @Valid annotation on attribute with type of enclosing class ("+e.getEnclosingElement().getSimpleName()+")?\nJSR-303 annotation found on attribute:");
            }
        }

        visitVariableWithType(e, type, nestingParams);

        return null;
    }

    /**
     * Checks whether there is a constructor used for binding in enclosing class,
     * if so, resolves the constructor parameter with matching name and type,
     * and appends its docs (if present) to the additional docs.
     * <p>
     * If there is {@link org.springframework.boot.context.properties.bind.Name @Name} annotation present
     * on the parameter, the parameter is visited as a variable element with type and name override set.
     * Comments from the class attribute in this case should be merged during documentation generation.
     *
     * @param e field element
     */
    private void addConstructorParameterForAttribute(VariableElement e, Params params) {
        if (!e.getEnclosingElement().getKind().isClass())
            return;

        Optional<ExecutableElement> constructor = Optional.empty();
        try {
            // check if there is a constructor used for binding,
            // and allow throwing exception in case of name override with @Name annotation
            if (annotatedConstructorDescriptor.isProperty(e, true)) {
                constructor = annotatedConstructorDescriptor.getConstructor((TypeElement) e.getEnclosingElement());
            } else if (singleConstructorDescriptor.isProperty(e, true)) {
                constructor = singleConstructorDescriptor.getConstructor((TypeElement) e.getEnclosingElement());
            }
        } catch (AbstractConstructorPropertyDescriptor.PropertyHasNameAnnotationException ex) {
            var paramsOverride = new Params(params.propertyDescriptor, params.decorators, params.namePrefix, params.annotatedElement, ex.getAnnotationValue(), params.additionalDoc, params.validationActive);
            visitVariableWithType(ex.getParameter(), ex.getParameter().asType(), paramsOverride);
            return;
        }

        if (constructor.isEmpty())
            return;

        var param = AbstractPropertyDescriptor.getMethodParam(constructor.get(), e.getSimpleName().toString(), e.asType(), envUtils.types());
        if (param.isEmpty())
            return; // this should already be checked by isProperty method

        var paramDocs = ElementDecorator.getParamDocTagTree(param.get(), envUtils.docTrees());
        params.additionalDoc.add(paramDocs);
    }

    /**
     * Visits a method<br>
     * if its annotated with {@link ConstructorBinding @ConstructorBinding}, visits its parameters
     */
    @Override
    public Void visitExecutable(ExecutableElement e, Params params) {

        if (e.getAnnotation(ConstructorBinding.class) != null) {
            for (var param : e.getParameters()) {
                this.visitVariable(param, params);
            }
        }

        return null;
    }

    /**
     * Visits a variable with provided type
     * <p>
     * If the type is primitive, the variable is added as a decorator,
     * otherwise, the type is checked and visited accordingly (Array, Declared)
     */
    public void visitVariableWithType(Element e, TypeMirror type, Params params) {
        // really primitive types like boolean, int, ...
        var p = params.divingClone(e.getSimpleName().toString(), false /* primitives do not require validation of their structure */);
        if (type.getKind().isPrimitive()) {
            addVariableAsDecorator(e, p);
            return;
        }
        if (type.getKind() == TypeKind.ARRAY) {
            Log.withContext(e).info("Documenting array type, ensure proper documentation is provided for this property");
            addVariableAsDecorator(e, p);
            return;
        }

        if (type.getKind() == TypeKind.DECLARED) {
            visitVariableWithDeclared(e, type, params);
        } else {
            Log.withContext(e).debug("Skipping element with unsupported type " + type.getKind());
        }
    }

    /**
     * Visits a variable with declared type
     * <p>
     * Determines the kind of the declared type and visits it accordingly<br>
     * Enums are directly added as decorators<br>
     * Records are visited<br>
     * Classes and interfaces are checked for their type,<br>
     * if they are convertible by Spring (so they have some common data type), they are added as decorators.<br>
     * They are checked if they are nested structures (another config class) and visited if so.<br>
     * Last check is performed if the property is a collection/map and has getter, added as decorator if so.<br>
     * <b>If those checks fail, the property is added as a decorator nevertheless, but warning is generated.</b>
     */
    public void visitVariableWithDeclared(Element e, TypeMirror type, Params params) {
        var decl = (DeclaredType) type;
        var declElement = decl.asElement();
        checkForNestedConfiguration(declElement);

        params.additionalDoc.add(envUtils.docTrees().getDocCommentTree(e));

        var paramsWithName = params.divingClone(e.getSimpleName().toString(), params.validationActive() && hasJakartaValidAnnotation(e));
        switch (declElement.getKind()) {
            case CLASS, INTERFACE -> {
                // Common type convertible by Spring conversion services
                // excludes collections as they are handled at the end
                if (isSpringConvertible((TypeElement) declElement) && !isCollectionProperty((VariableElement) e)) {
                    addVariableAsDecorator(e, paramsWithName);
                    return;
                }

                recommendValidAnnotation(e, params /*passing old params with validation status from upper context*/);

                if (isNested(e, declElement)) {
                    this.visit(declElement, paramsWithName);
                    return;
                }

                if (!(e.getKind() == ElementKind.FIELD && isCollectionProperty((VariableElement) e))) {
                    // last check, if it's nothing above,
                    // and it's not a collection nor map, then warning is generated
                    Log.withContext(e).warn("Type " + ((TypeElement) declElement).getQualifiedName() + " is not convertible from String without additional ConversionService, use JavaDoc tag @hidden to hide this property");
                }

                // it is a collection or map
                Log.withContext(e).info("Documenting collection type, ensure proper documentation is provided for this property");
                addVariableAsDecorator(e, paramsWithName);
            }
            case RECORD -> { // visit record components
                recommendValidAnnotation(e, params /*passing old params with validation status from upper context*/);
                this.visit(declElement, paramsWithName);
            }

            case ENUM ->  // nothing to visit in enum
                    addVariableAsDecorator(e, paramsWithName);

            default -> Log.withContext(e).error("Unknown element kind: " + declElement.getKind());
        }
    }

    /**
     * @return true if {@link jakarta.validation.Valid @Valid} annotation is present on the element, false otherwise
     */
    private boolean hasJakartaValidAnnotation(Element e) {
        return e.getAnnotation(jakarta.validation.Valid.class) != null;
    }

    /**
     * @param e           element to check
     * @param declElement declared type of the element
     * @return true if {@link NestedConfigurationProperty @NestedConfigurationProperty} annotation is present on the element,
     * or if the declared type is nested in the enclosing classes of the element, false otherwise
     */
    private boolean isNested(Element e, Element declElement) {
        var nestedAnnotation = e.getAnnotation(NestedConfigurationProperty.class);
        if (nestedAnnotation != null) {
            return true;
        }

        var outerClass = e.getEnclosingElement();

        for (var parent = declElement.getEnclosingElement(); parent != null && parent.getKind().isClass(); parent = parent.getEnclosingElement()) {
            if (outerClass.equals(parent)) {
                return true;
            }
            for (var enclosed : parent.getEnclosedElements()) {
                if (envUtils.types().isSameType(enclosed.asType(), e.asType()))
                    return true;
            }
        }

        return false;
    }

    /**
     * @return true if the element has a collection (or map) type and has a getter, false otherwise
     */
    private boolean isCollectionProperty(VariableElement e) {
        return collectionPropertyDescriptor.isCollectionOrMap(e) && collectionPropertyDescriptor.hasGetter(e);
    }

    /**
     * @return true if the type is convertible by Spring conversion services, false otherwise
     */
    private boolean isSpringConvertible(TypeElement type) {
        try {
            var sourceType = String.class;
            var targetType = Class.forName(type.getQualifiedName().toString());

            return conversionService.canConvert(sourceType, targetType);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void addVariableAsDecorator(Element e, Params params) {
        params.decorators.add(new ElementDecorator(e, params.namePrefix, envUtils, params.additionalDoc));
    }

    /**
     * Checks whether the element is annotated with {@link ConfigurationProperties @ConfigurationProperties} or {@link Configuration @Configuration} annotation,
     * and simultaneously it's parent is annotated with {@link ConfigurationProperties @ConfigurationProperties} annotation.
     * Which is probably unintended usage, so warning is generated.
     */
    private void checkForNestedConfiguration(Element e) {
        var confProperties = e.getAnnotation(ConfigurationProperties.class);
        var configuration = e.getAnnotation(Configuration.class);

        var annotation = Stream.of(confProperties, configuration).filter(Objects::nonNull).map(a -> a.annotationType().getSimpleName()).findAny();

        if (annotation.isPresent()) {
            var annotationName = annotation.get();

            var parent = e.getEnclosingElement();
            while (parent != null) {
                if (parent.getAnnotation(ConfigurationProperties.class) != null) {
                    Log.withContext(e).warn("Found " + annotationName + " annotation on nested class (" + e.getSimpleName() + ") in class with @ConfigurationProperties annotation (" + parent.getSimpleName() + "), this is probably wrong usage, consult Spring Boot documentation");
                    break;
                }
                parent = parent.getEnclosingElement();
            }
        }
    }

    public record Params(PropertyDescriptor propertyDescriptor, List<ElementDecorator> decorators, String namePrefix,
                         Element annotatedElement,
                         String nameOverride,
                         List<DocTree> additionalDoc, boolean validationActive) {

        /**
         * Appends name to the prefix and handles the name override
         *
         * @param name name to append
         * @return new prefix with name or override appended
         */
        private String getNewPrefix(String name) {
            if (nameOverride != null) {
                return NameFormatter.combine(namePrefix, nameOverride);
            }
            return NameFormatter.combine(namePrefix, name);
        }

        /**
         * @return new Params object with name (possibly overridden) appended to the prefix
         */
        public Params divingClone(String name, boolean validationActive) {
            var newPrefix = getNewPrefix(name);
            return new Params(this.propertyDescriptor, decorators, newPrefix, annotatedElement, null, additionalDoc, validationActive);
        }
    }
}
