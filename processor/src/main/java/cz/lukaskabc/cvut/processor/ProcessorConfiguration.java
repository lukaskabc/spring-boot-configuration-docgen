package cz.lukaskabc.cvut.processor;

import cz.lukaskabc.cvut.processor.docsgenerator.TemplateDocsGenerator;
import cz.lukaskabc.cvut.processor.formatter.Formatter;
import cz.lukaskabc.cvut.processor.formatter.HTMLFormatter;
import cz.lukaskabc.cvut.processor.formatter.MDFormatter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcessorConfiguration {

    /**
     * Fully qualified identifier for
     * {@link org.springframework.boot.context.properties.ConfigurationProperties
     * ConfigurationProperties} annotation
     */
    public static final String CONFIGURATION_PROPERTIES_ANNOTATION = "org.springframework.boot.context.properties.ConfigurationProperties";

    /**
     * Fully qualified identifier for
     * {@link org.springframework.beans.factory.annotation.Value Value} annotation
     */
    public static final String VALUE_ANNOTATION = "org.springframework.beans.factory.annotation.Value";

    /**
     * Default output file name
     */
    public static final String DEFAULT_OUTPUT_FILE = "springboot-configuration";

    /**
     * Prefix for all processor options
     */
    public static final String PROCESSOR_CONFIGURATION_PREFIX = "configurationdoc.";

    /**
     * Instruct the FreeMarker template engine to use square bracket syntax for templates [#...]
     *
     * @implNote Change to this has to result in change in internal (default) template files and directive definitions (directives.txt)
     * @see <a href="https://freemarker.apache.org/docs/dgui_misc_alternativesyntax.html">FreeMarker alternative syntax</a>
     */
    public static final boolean USE_TEMPLATE_SQUARE_BRACKET_SYNTAX = true;

    /**
     * Whether are getters for fields required to consider them as javabean properties.
     * Spring does not enforce this, only cares about constructors and setters as they are required for binding the values.
     */
    public static final boolean REQUIRE_GETTER_FOR_PROPERTIES = false;

    /**
     * Formatter providing syntax for selected output format
     */
    private Formatter formatter = new HTMLFormatter();

    /**
     * User defined output file path/name,
     * further handled in {@link TemplateDocsGenerator#loadTemplate}
     */
    private String outputFile = null;

    /**
     * User defined path to template file
     */
    private String templatePath = null;

    /**
     * Lexicographic order
     */
    private Order order = Order.ASC;

    /**
     * Whether required configuration options should be printed first
     */
    private boolean prependRequired = false;

    /**
     * Whether deprecated configuration options should be printed last
     */
    private boolean deprecatedLast = false;

    /**
     * When multiple comments are found for a single option, should they be merged?<br>
     * Merge: when comment contents are not yet present in the output, then append them
     */
    private boolean mergeComments = true;

    /**
     * Whether HTML tags should be disabled in Markdown output
     * (primarily HTML new line - &lt;br&gt; tags)
     */
    private boolean noHtmlInMarkdown = false;

    /**
     * User defined package containing configuration classes
     */
    private String configurationPackage = "";

    /**
     * Canonical prefix for all generated environment variables
     */
    private String envPrefix = "";


    /**
     * Options supported by this annotation processor, processed in defined order
     */
    public final List<ProcessorOption> options = List.of(
            new AbstractProcessorOption("configuration_package",
                    "Package containing configuration classes", "<package>") {
                @Override
                public boolean processImpl(String parameter) {
                    configurationPackage = parameter;
                    return true;
                }
            }, new AbstractProcessorOption("output_file", "Output file (filename without extension)", "<file>") {
                @Override
                public boolean processImpl(String parameter) {
                    outputFile = parameter;
                    return true;
                }
            }, new AbstractProcessorOption("order", "Lexicographic order", "<asc|desc|none>") {
                @Override
                public boolean processImpl(String parameter) {

                    return switch (parameter.toLowerCase()) {
                        case "asc", "ascending":
                            order = Order.ASC;
                            yield true;
                        case "desc", "descending":
                            order = Order.DESC;
                            yield true;
                        case "none":
                            order = Order.NONE;
                            yield true;
                        default:
                            yield false;
                    };
                }
            }, new AbstractProcessorOption("prepend_required", "Print required options first", null) {
                @Override
                public boolean processImpl(String ignored) {
                    prependRequired = true;
                    return true;
                }
            }, new AbstractProcessorOption("deprecated_last", "Print deprecated options last", null) {
                @Override
                public boolean processImpl(String ignored) {
                    deprecatedLast = true;
                    return true;
                }
            }, new AbstractProcessorOption("do_not_merge", "Do not merge multiple comments, use only the first one", null) {
                @Override
                public boolean processImpl(String ignored) {
                    mergeComments = false;
                    return true;
                }
            },
            new AbstractProcessorOption("no_html", "Disable html tags in markdown output, this does not escape HTML from javadoc comments", null) {
                @Override
                public boolean processImpl(String ignored) {
                    noHtmlInMarkdown = true;
                    return true;
                }
            },
            new AbstractProcessorOption("format", "Output format: HTML or Markdown", "<HTML | MD>") {
                @Override
                public boolean processImpl(String parameter) {

                    String arg = parameter.toUpperCase()
                            .replaceAll("\\s+", "") // remove whitespace
                            .replace("_", "") // remove underscores
                            .replace("-", ""); // remove hyphens

                    switch (arg) {
                        case "HTML" -> {
                            formatter = new HTMLFormatter();
                        }
                        case "MARKDOWN", "MD" -> {
                            formatter = new MDFormatter(noHtmlInMarkdown);
                        }
                        default -> {
                            return false;
                        }
                    }

                    Log.instance().info("Using output format: " + formatter.getFileExtension().toUpperCase());
                    return true;
                }
            },
            new AbstractProcessorOption("template", "Path to template file", "<file path>") {
                @Override
                public boolean processImpl(String parameter) {
                    templatePath = parameter;
                    return true;
                }
            }, new AbstractProcessorOption("env_prefix", "Canonical prefix for generated environment variables", "<prefix>") {
                @Override
                protected boolean processImpl(String parameterValue) {
                    var param = parameterValue.trim().toUpperCase();

                    if (param.contains("_")) {
                        Log.instance()
                                .error("Environment variable prefix cannot contain underscore. Canonical names should be kebab-case ('-' separated), lowercase alpha-numeric characters and must start with a letter");
                        return false;
                    }

                    Log.instance().info("Using environment variable prefix: " + param);

                    envPrefix = param;
                    return true;
                }
            });

    public void processSystemProperties() {
        var prop = System.getProperties();
        options.forEach(option -> {
            var key = PROCESSOR_CONFIGURATION_PREFIX + option.getName();
            if (prop.containsKey(key)) {
                var value = prop.getProperty(key);
                option.process(value);
            }
        });
    }

    public void processENVOptions() {
        var env = System.getenv();
        options.forEach(option -> {
            var key = PROCESSOR_CONFIGURATION_PREFIX + option.getName();
            key = key.toUpperCase().replace(".", "_");

            if (env.containsKey(key)) {
                var value = env.get(key);
                option.process(value);
            }
        });
    }

    /**
     * Process given options according to definition in {@link #options}
     *
     * @param optionsMap map of option name and parameter value
     * @throws IllegalArgumentException when unknown options are present
     */
    public void processOptions(Map<String, String> optionsMap) {
        String unknownOptions = optionsMap.keySet().stream()
                .filter(key -> key.startsWith(PROCESSOR_CONFIGURATION_PREFIX))
                .filter(key -> options.stream().noneMatch(option -> {
                    var name = PROCESSOR_CONFIGURATION_PREFIX + option.getName();
                    return name.equals(key);
                })).collect(Collectors.joining(", "));

        if (!unknownOptions.isBlank()) {
            Log.instance().warn("Unknown options: " + unknownOptions);
            throw new IllegalArgumentException("Unknown options: " + unknownOptions);
        }

        // process options in their definition order
        for (var option : this.options) {
            // options are defined without the prefix, so prepend it
            var key = PROCESSOR_CONFIGURATION_PREFIX + option.getName();
            // when option is not present in the map, skip it
            if (!optionsMap.containsKey(key)) {
                continue;
            }

            var value = optionsMap.get(key);
            // execute the option with the value
            option.process(value);
        }
    }

    /**
     * Validates current processor state (at the end of initialization)
     *
     * @return true when configuration is valid, throws otherwise
     * @throws IllegalArgumentException when defined output file or template file is invalid or not writable
     */
    public boolean validateConfiguration() {

        if (formatter instanceof HTMLFormatter && noHtmlInMarkdown) {
            Log.instance().warn("HTML format is selected, ignoring no_html option");
        }

        if (outputFile == null) {
            Log.instance().warn("Output file not specified, using default: " + DEFAULT_OUTPUT_FILE);
            outputFile = DEFAULT_OUTPUT_FILE + "." + formatter.getFileExtension();
        } else if (!outputFile.endsWith("." + formatter.getFileExtension())) {
            Log.instance().warn("Output file does not end with ." + formatter.getFileExtension() + " extension");
        }

        var file = new File(outputFile);
        if (file.exists()) {
            if (!file.isFile()) {
                throw new IllegalArgumentException("Specified output file is not a file: " + file.getAbsolutePath());
            }
            if (!file.canWrite()) {
                throw new IllegalArgumentException("Specified output file is not writable: " + file.getAbsolutePath());
            }
        }

        if (templatePath != null) {
            file = new File(templatePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("Specified template file does not exist: " + file.getAbsolutePath());
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException("Specified template file is not a file: " + file.getAbsolutePath());
            }
            if (!file.canRead()) {
                throw new IllegalArgumentException("Specified template file is not readable: " + file.getAbsolutePath());
            }
        }

        return true;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public Order getOrder() {
        return order;
    }

    public boolean getPrependRequired() {
        return prependRequired;
    }

    public boolean getDeprecatedLast() {
        return deprecatedLast;
    }

    public boolean getMergeComments() {
        return mergeComments;
    }

    public boolean getNoHtmlInMarkdown() {
        return noHtmlInMarkdown;
    }

    public String getConfigurationPackage() {
        return configurationPackage;
    }

    public String getEnvPrefix() {
        return envPrefix;
    }

    public enum Order {

        /**
         * A-Z
         */
        ASC,
        /**
         * Z-A
         */
        DESC,
        /**
         * Do not change order
         */
        NONE

    }
}
