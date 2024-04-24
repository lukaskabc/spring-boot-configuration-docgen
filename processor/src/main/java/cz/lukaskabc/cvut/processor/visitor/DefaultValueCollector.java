package cz.lukaskabc.cvut.processor.visitor;

import com.sun.source.doctree.DocTree;
import com.sun.source.tree.*;
import com.sun.source.util.SimpleTreeVisitor;
import cz.lukaskabc.cvut.processor.ElementDecorator;
import cz.lukaskabc.cvut.processor.EnvironmentUtils;
import cz.lukaskabc.cvut.processor.descriptor.AbstractPropertyDescriptor;
import cz.lukaskabc.cvut.processor.descriptor.AnnotatedConstructorPropertyDescriptor;
import cz.lukaskabc.cvut.processor.descriptor.SingleConstructorPropertyDescriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultValueCollector extends SimpleTreeVisitor<Object, Element> {

    private static final Set<String> FACTORY_METHODS = Set.of("of", "from", "valueOf", "getInstance", "newInstance");

    private static final String NULL = "null";

    private final EnvironmentUtils envUtils;

    private final AnnotatedConstructorPropertyDescriptor annotatedConstructorPropertyDescriptor;

    private final SingleConstructorPropertyDescriptor singleConstructorPropertyDescriptor;

    public DefaultValueCollector(EnvironmentUtils envUtils) {
        this.envUtils = envUtils;
        this.annotatedConstructorPropertyDescriptor = new AnnotatedConstructorPropertyDescriptor(envUtils);
        this.singleConstructorPropertyDescriptor = new SingleConstructorPropertyDescriptor(envUtils);
    }

    private Optional<VariableElement> findConstructorParameterForAttribute(Element attribute) {
        if (attribute.getKind() != ElementKind.FIELD) {
            return Optional.empty();
        }
        var variable = (VariableElement) attribute;
        if (annotatedConstructorPropertyDescriptor.isProperty(variable)) {
            var constructor = annotatedConstructorPropertyDescriptor.getConstructor((TypeElement) attribute.getEnclosingElement());
            if (constructor.isPresent()) {
                return AbstractPropertyDescriptor.getMethodParam(constructor.get(), variable.getSimpleName().toString(), variable.asType(), envUtils.types());
            }
        }

        if (singleConstructorPropertyDescriptor.isProperty(variable)) {
            var constructor = singleConstructorPropertyDescriptor.getConstructors((TypeElement) attribute.getEnclosingElement()).get(0);
            return AbstractPropertyDescriptor.getMethodParam(constructor, variable.getSimpleName().toString(), variable.asType(), envUtils.types());
        }

        return Optional.empty();
    }

    public String findDefaultValue(ElementDecorator decorator) {
        var docs = Stream.builder().add(decorator.getDocTree().orElse(null));
        for (var d : decorator.getAdditionalDocTrees()) {
            docs.add(d);
        }

        var javadocValue = docs.build().filter(Objects::nonNull)
                .map(docTree -> getDefaultJavadocTag((DocTree) docTree))
                .filter(Optional::isPresent).map(Optional::get).distinct().collect(Collectors.joining("; "));

        if (!javadocValue.isBlank()) {
            return javadocValue;
        }

        var valueAnnotation = evaluateValueAnnotation(decorator.getElement());
        if (valueAnnotation != null) {
            return valueAnnotation;
        }

        var constructorParameter = findConstructorParameterForAttribute(decorator.getElement());
        if (constructorParameter.isPresent()) {
            var paramDecorator = new ElementDecorator(constructorParameter.get(), decorator.getConfigOptionName(), envUtils, List.of());
            var value = findDefaultValue(paramDecorator);
            if (value != null) {
                return value;
            }
        }

        return findElementValue(decorator.getElement());
    }

    private String findElementValue(Element element) {
        // handle @DefaultValue annotation
        // https://docs.spring.io/spring-boot/docs/3.2.1/api/org/springframework/boot/context/properties/bind/DefaultValue.html
        var defaultValueAnnotation = this.getValueFromDefaultValueAnnotation(element);
        if (defaultValueAnnotation.isPresent()) {
            return defaultValueAnnotation.get();
        }

        var constructorParameter = findConstructorParameterForAttribute(element);
        if (constructorParameter.isPresent()) {
            var value = findElementValue(constructorParameter.get());
            if (value != null) {
                return value;
            }
        }

        var tree = envUtils.trees().getTree(element);
        var value = this.visit(tree, element);
        if (value == null || value.equals("Optional.empty()"))
            return null;

        return value.toString();
    }

    private Optional<String> getDefaultJavadocTag(DocTree docTree) {
        var tagContents = new LinkedHashSet<DocTree>();
        new JavadocDefaultTagVisitor().scan(docTree, tagContents);

        String value = tagContents.stream().map(DocTree::toString).collect(Collectors.joining("; "));
        if (value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    /**
     * Visits the initializer in variable tree<br>
     * VariableType variableName = initializer;
     */
    @Override
    public Object visitVariable(VariableTree node, Element original) {
        if (node.getInitializer() != null) {
            return this.visit(node.getInitializer(), original);
        }
        return null;
    }

    @Override
    protected Object defaultAction(Tree node, Element original) {
        return node.toString();
    }

    /**
     * Visits expression in assignment tree<br>
     * variableName = expression;
     */
    @Override
    public Object visitAssignment(AssignmentTree node, Element original) {
        if (node.getExpression() != null) {
            return this.visit(node.getExpression(), original);
        }
        return super.visitAssignment(node, original);
    }

    /**
     * Visits literal expression
     *
     * @return literal value as string or {@link #NULL
     */
    @Override
    public Object visitLiteral(LiteralTree node, Element original) {
        if (node.getValue() != null) return node.getValue().toString();

        return NULL;
    }

    /**
     * object . methodName ( arguments ) <br>
     * <p>
     * if method name is factory method then visits the first argument,
     * visit method select otherwise
     */
    @Override
    public Object visitMethodInvocation(MethodInvocationTree node, Element original) {
        if (node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
            var methodName = ((MemberSelectTree) node.getMethodSelect()).getIdentifier();
            if (FACTORY_METHODS.contains(methodName.toString())
                    && node.getArguments().size() == 1) {
                return this.visit(node.getArguments().get(0), original);
            }
            if (methodName.toString().equals("toString") && node.getArguments().isEmpty()) {
                return this.visit(node.getMethodSelect(), original);
            }
        }
        return node.toString();
    }

    @Override
    public Object visitIdentifier(IdentifierTree node, Element original) {
        var element = resolveFieldInContext(node.toString(), original);
        if (element.isPresent()) {
            var tree = envUtils.trees().getTree(element.get());
            return this.visit(tree, original);
        }

        var candidates = resolveClassImports(node.toString(), original);
        for (var candidate : candidates) {
            var value = attemptStaticFieldReflection(candidate, node.toString());
            if (value.isPresent()) {
                return value.get();
            }
        }

        return node.toString();
    }

    /**
     * expression . memberName
     */
    @Override
    public Object visitMemberSelect(MemberSelectTree node, Element original) {
        if (node.getIdentifier().toString().equals("toString")) {
            return this.visit(node.getExpression(), original);
        }

        var candidates = resolveClassImports(node.getExpression().toString(), original);
        for (var candidate : candidates) {
            var value = attemptStaticFieldReflection(candidate, node.getIdentifier().toString());
            if (value.isPresent()) {
                return value.get();
            }
        }

        // Boolean.TRUE
        // its member select, so there is some expression before the dot so we are looking for class
        var expressionElement = resolveClassInPackage(node.getExpression().toString(), original);
        if (expressionElement.isPresent()) {
            var member = resolveFieldInContext(node.getIdentifier().toString(), expressionElement.get());
            if (member.isPresent()) {
                var tree = envUtils.trees().getTree(member.get());
                if (tree != null) {
                    return this.visit(tree, original);
                }
            }
        }

        if (hasEnumType(original)) {
            return node.getIdentifier().toString();
        }

        return node.toString();
    }

    private boolean hasEnumType(Element element) {
        var type = element.asType();
        while (type.getKind() == TypeKind.ARRAY) {
            type = ((ArrayType) type).getComponentType();
        }
        if (type.getKind() == TypeKind.DECLARED) {
            var declaredType = ((DeclaredType) type).asElement();
            return declaredType.getKind() == ElementKind.ENUM;
        }

        return false;
    }

    /**
     * @return List with direct import of the class or list with all wildcard imports with the class name appended instead of the asterisk
     */
    private List<String> resolveClassImports(String classIdentifier, Element context) {
        var contextPath = envUtils.trees().getPath(context);

        var imports = contextPath.getCompilationUnit().getImports().stream()
                .map(i -> i.getQualifiedIdentifier().toString())
                .distinct().toList();

        var directImport = imports.stream().filter(i -> i.endsWith("." + classIdentifier)).toList();

        if (!directImport.isEmpty())
            return directImport;

        var javaLangPart = resolveClassFromJavaLang(classIdentifier);
        if (javaLangPart.isPresent()) {
            return List.of(javaLangPart.get());
        }

        // java.lang does not require imports, so add it here
        return imports.stream()
                .filter(i -> i.endsWith(".*"))
                .map(i -> i.substring(0, i.length() - 1) + classIdentifier)
                .distinct().toList();
    }

    private Optional<String> resolveClassFromJavaLang(String className) {
        var javaLang = envUtils.elements().getPackageElement("java.lang");
        if (javaLang == null) {
            // that would be strange
            return Optional.empty();
        }

        for (var element : javaLang.getEnclosedElements()) {
            var target = resolveFieldInContext(className, element);
            if (target.isPresent() && target.get() instanceof TypeElement type) {
                return Optional.of(type.getQualifiedName().toString());
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to resolve the class and acquire the static field value
     *
     * @return field value as string or empty optional
     */
    private Optional<String> attemptStaticFieldReflection(String classIdentifier, String classMember) {
        var value = getStaticFieldStringValue(classIdentifier, classMember);
        if (value.isEmpty()) {
            // in case that import is made directly so the last part is identifier of static field
            // strip the last segment after last dot and try reflection with class identifier
            var stripedIdentifier = classIdentifier.substring(0, classIdentifier.lastIndexOf('.'));
            value = getStaticFieldStringValue(stripedIdentifier, classMember);
        }

        return value;
    }

    private Optional<String> getStaticFieldStringValue(String classIdentifier, String classMember) {
        try {
            var clazz = Class.forName(classIdentifier);
            var field = clazz.getField(classMember);
            var value = field.get(null);
            return Optional.ofNullable(objectValueToString(value));
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException |
                 NullPointerException ignored) {
            return Optional.empty();
        }
    }

    private String objectValueToString(Object object) {
        if (object == null)
            return NULL;

        var clazz = object.getClass();

        if (clazz.isArray()) { // pure Array[]
            if (clazz.getComponentType().isEnum()) {
                return "[" +
                        Arrays.stream((Enum[]) object)
                                .map(Enum::name)
                                .collect(Collectors.joining(", "))
                        + "]";
            }

            try {
                var method = Arrays.class.getMethod("toString", clazz);
                return (String) method.invoke(null, object);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
                // lets try just with object
                var array = (Object[]) object;
                var sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < array.length; i++) {
                    sb.append(objectValueToString(array[i]));
                    if (i < array.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
                return sb.toString();
            }
        }
        // Map
        if (Map.class.isAssignableFrom(clazz)) {
            var map = (Map<?, ?>) object;
            return map.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));
        }

        // Enum
        if (clazz.isEnum())
            return ((Enum<?>) object).name();


        // Handles also:
        // Collection
        try {
            var toStringMethod = clazz.getMethod("toString");
            if (toStringMethod.getDeclaringClass().equals(Object.class)) {
                throw new NoSuchMethodException();
            }

        } catch (NoSuchMethodException ignored) {
            return null;
        }

        return object.toString();
    }

    @Override
    public Object visitTypeCast(TypeCastTree node, Element element) {
        if (node.getExpression() != null)
            return visit(node.getExpression(), element);

        return super.visitTypeCast(node, element);
    }

    @Override
    public Object visitNewClass(NewClassTree node, Element element) {
        return null;
    }

    @Override
    public Object visitAnnotatedType(AnnotatedTypeTree node, Element element) {
        return this.visit(node.getUnderlyingType(), element);
    }

    private Optional<Element> resolveClassInPackage(String className, Element original) {
        return resolveElementInContext(className, original, e -> e.getKind() == ElementKind.PACKAGE);
    }

    private Optional<Element> resolveFieldInContext(String fieldName, Element original) {
        return resolveElementInContext(fieldName, original, e -> e.getKind().isClass());
    }

    /**
     * Resolves if there is an element matching given name in the given element, its parents or its children
     *
     * @param name name is split by dot and used as a path in the element tree
     */
    private Optional<Element> resolveElementInContext(String name, Element original, Predicate<Element> shouldAddEnclosed) {
        var path = name.split("\\.");
        var topLevel = path[0];
        var enclosingElement = original;
        var stack = new LinkedList<Element>();

        // add all parents to the stack
        while (enclosingElement.getEnclosingElement() != null) {
            enclosingElement = enclosingElement.getEnclosingElement();
            // later parents should go later so add them to the beginning of the stack
            stack.addFirst(enclosingElement);
        }

        // add current context as last to the stack (first to be taken)
        stack.addLast(original);
        // top level element matching the path
        Element topLevelElement = null;
        while (!stack.isEmpty()) {
            var element = stack.pollLast(); // take last element from stack
            // add all children to the END of the stack (so they are last and will be taken right next round)
            // -> visiting children first and parent later
            if (shouldAddEnclosed.test(element))
                stack.addAll(element.getEnclosedElements());

            // if name matches, break the loop
            if (element.getSimpleName().toString().equals(topLevel)) {
                topLevelElement = element;
                break;
            }
        }
        stack.clear();

        if (topLevelElement == null) {
            return Optional.empty();
        }

        // search the topLevelElement from the path for children matching each part of the path
        var pathElement = topLevelElement;
        for (int i = 1; i < path.length; i++) {
            var pathName = path[i];
            var element = pathElement.getEnclosedElements().stream()
                    .filter(e -> e.getSimpleName().toString().equals(pathName))
                    .findAny();

            if (element.isEmpty()) {
                return Optional.empty();
            }
            pathElement = element.get();
        }
        return Optional.of(pathElement);
    }

    @Override
    public Object visitMethod(MethodTree node, Element element) {
        return null;
    }

    public String evaluateValueAnnotation(Element element) {
        var annotation = element.getAnnotation(Value.class);
        if (annotation == null) {
            return null;
        }

        var expression = annotation.value();

        if (expression.charAt(0) != '$' || expression.charAt(1) != '{' || expression.charAt(expression.length() - 1) != '}') {
            return null;
        }

        var colonIndex = expression.indexOf(':');
        if (colonIndex < 2)
            return null;

        return expression.substring(colonIndex + 1, expression.length() - 1);
    }

    @Override
    public Object visitNewArray(NewArrayTree node, Element element) {
        // { A, B } -> "[A, B]"
        return "[" +
                node.getInitializers().stream()
                        .map(i -> this.visit(i, element))
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
                + "]";
    }

    public Optional<String> getValueFromDefaultValueAnnotation(Element element) {
        var defaultValue = element.getAnnotation(DefaultValue.class);
        if (defaultValue == null) {
            return Optional.empty();
        }

        var value = defaultValue.value();
        if (value == null)
            return Optional.empty();

        if (value.length == 1) {
            return Optional.of(value[0]);
        }

        var builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < value.length; i++) {
            builder.append(value[i]);
            if (i < value.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");

        return Optional.of(builder.toString());
    }
}
