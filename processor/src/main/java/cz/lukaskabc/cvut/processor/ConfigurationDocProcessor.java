package cz.lukaskabc.cvut.processor;

import com.sun.source.doctree.DocTree;
import com.sun.source.util.TreePath;
import cz.lukaskabc.cvut.processor.docsgenerator.ElementDecoratorDocGenerator;
import cz.lukaskabc.cvut.processor.docsgenerator.TemplateDocsGenerator;
import cz.lukaskabc.cvut.processor.visitor.DefaultValueCollector;
import cz.lukaskabc.cvut.processor.visitor.PropertiesClassScanner;
import cz.lukaskabc.cvut.processor.visitor.ValueAnnotationScanner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static cz.lukaskabc.cvut.processor.ProcessorConfiguration.*;

/**
 * Annotation processor generating documentation for Spring Boot configuration with
 * {@link org.springframework.boot.context.properties.ConfigurationProperties @ConfigurationProperties}
 * and {@link org.springframework.beans.factory.annotation.Value @Value} annotations.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({CONFIGURATION_PROPERTIES_ANNOTATION,
        VALUE_ANNOTATION})
@SupportedOptions({ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "configuration_package",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "output_file",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "order",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "prepend_required",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "deprecated_last",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "do_not_merge",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "no_html",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "format",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "template",
        ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX + "env_prefix"})
public class ConfigurationDocProcessor extends AbstractProcessor {

    private final ProcessorConfiguration processorConfiguration = new ProcessorConfiguration();

    /**
     * Decorators of config properties under {@link ConfigurationProperties @ConfigurationProperties} annotated structure
     */
    private final List<ElementDecorator> propertiesDecorators = new ArrayList<>();

    /**
     * Decorators of config properties with {@link org.springframework.beans.factory.annotation.Value @Value} annotation
     */
    private final List<ElementDecorator> valueDecorators = new ArrayList<>();

    /**
     * Paths of annotated elements discovered during several processing rounds
     */
    private final Map<String, Set<TreePath>> watchedElements = new HashMap<>();

    /**
     * Wrapper around {@link ProcessingEnvironment} providing source utilities
     */
    private EnvironmentUtils envUtils = null;

    /**
     * Visitor for collecting default values of elements
     */
    private DefaultValueCollector defaultValueCollector;


    /**
     * Whether the processor has been initialized
     */
    private boolean initialized = false;


    /**
     * Unwrap environment from JetBrains API wrapper (only when running in IntelliJ IDEA)
     *
     * @see <a href="https://youtrack.jetbrains.com/issue/IDEA-274697/java-java.lang.IllegalArgumentException#focus=Comments-27-5084543.0-0">Comment on JetBrains YouTrack</a>
     */
    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass()
                    .getClassLoader()
                    .loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {
            // this may fail if it's running outside IntelliJ IDEA
            // in that case the original object is returned
        }
        return unwrapped != null ? unwrapped : wrapper;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        var environment = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        this.envUtils = new EnvironmentUtils(environment);
        this.defaultValueCollector = new DefaultValueCollector(envUtils);

        Log.init(envUtils);

        // process options from processing environment
        processorConfiguration.processOptions(environment.getOptions());
        // process options from system properties
        processorConfiguration.processSystemProperties();
        // process options from environment variables
        processorConfiguration.processENVOptions();

        // validate initialized state
        initialized = processorConfiguration.validateConfiguration();
    }

    @Override
    protected synchronized boolean isInitialized() {
        if (!initialized)
            return false;

        return super.isInitialized();
    }

    /**
     * Main method for running the annotation processor
     *
     * @return false - not claiming any annotations
     */
    @Override
    public synchronized boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!initialized)
            return false;

        // cache all discovered elements with specific annotations as paths
        var propertiesElements = watchElementsWithAnnotation(roundEnv, CONFIGURATION_PROPERTIES_ANNOTATION);
        var valueElements = watchElementsWithAnnotation(roundEnv, VALUE_ANNOTATION);

        // wait on the last round and then process collected elements
        if (roundEnv.processingOver()) {
            Log.instance().info("Processing configuration properties for documentation");
            // find all configuration options in annotated structures
            // find default values for them
            processConfigurationPropertiesAnnotation(propertiesElements);
            processValueAnnotation(valueElements);
            // generate documentation for collected decorators
            writeDocumentation();
        }

        return false;
    }

    /**
     * Searches current round environment for elements with specified annotation and
     * caches their tree paths for later processing
     *
     * @param roundEnv            current round environment
     * @param annotationQualifier Fully qualified name of annotation
     * @return Set of cached elements with specified annotation
     */
    private Set<Element> watchElementsWithAnnotation(RoundEnvironment roundEnv, String annotationQualifier) {
        var elementUtils = envUtils.elements();
        TypeElement annotationType = elementUtils.getTypeElement(annotationQualifier);
        if (annotationType == null) {
            // annotation was not found
            return Set.of();
        }

        // get annotated elements in this round
        var elements = roundEnv.getElementsAnnotatedWith(annotationType);
        var watched = watchedElements.getOrDefault(annotationQualifier, new LinkedHashSet<>());

        var treeUtils = envUtils.trees();

        if (!elements.isEmpty()) {
            var elementTrees = elements.stream()
                    // filter only elements from specified configuration package
                    .filter(element -> {
                        var pckgName = elementUtils.getPackageOf(element).getQualifiedName().toString();
                        return processorConfiguration.getConfigurationPackage().isBlank() || pckgName.startsWith(processorConfiguration.getConfigurationPackage());
                    })
                    // map to tree paths (elements are mostly invalidated between rounds,
                    // paths remain valid)
                    .map(treeUtils::getPath)
                    .toList();
            watched.addAll(elementTrees);
            watchedElements.put(annotationQualifier, watched);
        }

        // resolve current elements from cached paths and return as set
        return watched.stream().map(treeUtils::getElement).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Process elements with
     * {@link org.springframework.boot.context.properties.ConfigurationProperties
     * ConfigurationProperties} annotation wrapping them into {@link ElementDecorator},
     * resolving default values and storing them in {@link #propertiesDecorators}
     *
     * @param elements Elements with
     *                 {@link org.springframework.boot.context.properties.ConfigurationProperties
     *                 ConfigurationProperties} annotation
     */
    private void processConfigurationPropertiesAnnotation(Set<Element> elements) {

        var propertyClassScanner = new PropertiesClassScanner(envUtils, REQUIRE_GETTER_FOR_PROPERTIES, processorConfiguration.getEnvPrefix());

        for (var element : elements) {
            var annotation = element.getAnnotation(ConfigurationProperties.class);

            switch (element.getKind()) {
                case CLASS, RECORD -> {
                    var typeElement = (TypeElement) element;
                    propertyClassScanner.visitConfiguration(typeElement, typeElement, annotation, propertiesDecorators,
                            new ArrayList<>(), true);
                }
                case METHOD -> {
                    var method = (ExecutableElement) element;
                    var type = method.getReturnType();
                    var typeElement = (TypeElement) envUtils.types().asElement(type);

                    var beanAnnotation = method.getAnnotation(Bean.class);
                    if (beanAnnotation == null) {
                        // TODO: shouldn't this be an error?
                        Log.withContext(method)
                                .warn("Method annotated with @ConfigurationProperties is missing @Bean annotation: "
                                        + method.getSimpleName());
                    }

                    if (typeElement == null) {
                        Log.withContext(element)
                                .warn("Skipping method with @ConfigurationProperties annotation: " + method.getSimpleName()
                                        + " (Type " + type.toString() + " not found)");
                        continue;
                    }

                    var docs = new ArrayList<DocTree>();

                    var methodDoc = envUtils.docTrees().getDocCommentTree(method);
                    if (methodDoc != null)
                        docs.add(methodDoc);

                    propertyClassScanner.visitConfiguration(typeElement, method, annotation, propertiesDecorators,
                            docs, false);
                }
                default -> {
                    // no other kinds should be possible by annotation definition
                    Log.instance()
                            .warn("Skipping element with @ConfigurationProperties annotation: " + element.getKind() + " "
                                    + element.getSimpleName() + " (unsupported element kind)");
                }
            }
        }

        loadDefaultValues(propertiesDecorators);
    }

    /**
     * Process elements with {@link org.springframework.beans.factory.annotation.Value
     * Value} annotation wrapping them into {@link ElementDecorator}, resolving default
     * values and storing them in {@link #valueDecorators}
     *
     * @param elements Elements with
     *                 {@link org.springframework.beans.factory.annotation.Value Value} annotation
     */
    private void processValueAnnotation(Set<Element> elements) {
        var valueScanner = new ValueAnnotationScanner(envUtils);

        for (var element : elements) {
            valueScanner.visit(element, valueDecorators);
        }

        loadDefaultValues(valueDecorators);
    }

    /**
     * Use {@link #defaultValueCollector} to find default values for decorators
     *
     * @param decorators decorators to process
     */
    private void loadDefaultValues(List<ElementDecorator> decorators) {
        for (var decorator : decorators) {
            var defaultValue = defaultValueCollector.findDefaultValue(decorator);

            if (defaultValue != null) {
                decorator.setDefaultValue(defaultValue);
            }
        }
    }

    /**
     * Generates documentation for {@link #propertiesDecorators} and
     * {@link #valueDecorators} and writes output to file
     */
    private void writeDocumentation() {
        var decorators = uniqueDecorators(this.propertiesDecorators, this.valueDecorators);

        Log.instance().debug("Found objects for documentation:");
        Log.instance().debug("    " + this.propertiesDecorators.size() + " configuration attributes");
        Log.instance().debug("    " + this.valueDecorators.size() + " elements with @Value annotation");

        var decoratorDocGenerator = new ElementDecoratorDocGenerator(processorConfiguration.getFormatter());

        // generate description for all decorators (fills .getDescription() output)
        Collection<DocumentedElement> documentedElements = decorators.stream()
                .map(decoratorDocGenerator::generate)
                .filter(Objects::nonNull)
                .toList();

        Log.instance().debug("In total " + documentedElements.size() + " values for documentation.");

        documentedElements = sortDecorators(documentedElements);

        // write file with documentation
        TemplateDocsGenerator docsGenerator = new TemplateDocsGenerator(USE_TEMPLATE_SQUARE_BRACKET_SYNTAX, processorConfiguration.getFormatter());
        docsGenerator.loadTemplate(processorConfiguration.getOutputFile(), processorConfiguration.getTemplatePath());
        docsGenerator.generate(documentedElements);
    }

    /**
     * Eliminates duplicates with the same config option name, merges comments if
     * necessary
     *
     * @param decoratorCollections decorators to process
     * @return unique decorators
     */
    @SafeVarargs
    private Collection<ElementDecorator> uniqueDecorators(Collection<ElementDecorator>... decoratorCollections) {
        var map = new HashMap<String, ElementDecorator>();

        Arrays.stream(decoratorCollections).flatMap(Collection::stream).forEach(decorator -> {

            var cur = map.get(decorator.getConfigOptionName());
            if (cur != null && cur.getDocTree().isPresent()) {
                // if there already exists a decorator with the same config option name
                // and with documentation

                // if comments should be merged and there is a doc tree (comment)
                if (decorator.getDocTree().isPresent()) {
                    if (processorConfiguration.getMergeComments()) {
                        cur.addDocTree(decorator.getDocTree().get());
                    } else {
                        Log.instance()
                                .warn("Skipping secondary comment for " + decorator.getConfigOptionName() + " (on element: "
                                        + decorator.getElement().getSimpleName() + "); comment merging is disabled");
                    }
                }

                return;
            }

            map.put(decorator.getConfigOptionName(), decorator);
        });

        return map.values();
    }

    /**
     * Sorts decorators according to annotation processor configuration
     */
    private Collection<DocumentedElement> sortDecorators(Collection<DocumentedElement> decorators) {

        // create default comparator
        // assuming all elements are equal
        Comparator<DocumentedElement> decoratorComparator = (a, b) -> 0;

        var order = processorConfiguration.getOrder();

        // Lexicographical order A-Z
        if (order == Order.ASC) {
            decoratorComparator = Comparator.comparing(DocumentedElement::getName);
        }

        // Lexicographical order Z-A
        if (order == Order.DESC) {
            decoratorComparator = Comparator.comparing(DocumentedElement::getName).reversed();
        }

        // first prepend required and then compare with lexicographical comparator
        if (processorConfiguration.getPrependRequired()) {
            var nextComp = decoratorComparator;
            decoratorComparator = (a, b) -> {
                // if both are in the same "group", then use next comparator
                if (a.isRequired() == b.isRequired()) {
                    return nextComp.compare(a, b);
                }
                // prepend required ones
                return a.isRequired() ? -1 : 1;
            };
        }

        // move deprecated to the end
        if (processorConfiguration.getDeprecatedLast()) {
            var nextComp = decoratorComparator;
            decoratorComparator = (a, b) -> {
                // if both are in the same "group", then use next comparator
                if (a.isDeprecated() == b.isDeprecated()) {
                    return nextComp.compare(a, b);
                }
                // prepend required ones
                return a.isDeprecated() ? 1 : -1;
            };
        }

        // If order is defined or another sort option is defined, then sort
        if (order != Order.NONE || processorConfiguration.getPrependRequired() || processorConfiguration.getDeprecatedLast()) {
            decorators = decorators.stream().sorted(decoratorComparator).toList();
        }

        return decorators;
    }

}
