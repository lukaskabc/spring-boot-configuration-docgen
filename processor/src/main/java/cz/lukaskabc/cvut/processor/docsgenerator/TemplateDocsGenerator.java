package cz.lukaskabc.cvut.processor.docsgenerator;

import cz.lukaskabc.cvut.processor.DocumentedElement;
import cz.lukaskabc.cvut.processor.Log;
import cz.lukaskabc.cvut.processor.formatter.Formatter;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.ConditionalTemplateConfigurationFactory;
import freemarker.cache.FileExtensionMatcher;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.TemplateConfiguration;
import freemarker.template.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TemplateDocsGenerator {

    private final Configuration engineConfig = new Configuration(Configuration.VERSION_2_3_32);

    private final Formatter formatter;

    private File outputFile = null;

    private Template template = null;

    public TemplateDocsGenerator(boolean useSquareBracketSyntax, Formatter formatter) {
        this.formatter = formatter;
        engineConfig.setDefaultEncoding(StandardCharsets.UTF_8.name());
        engineConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        engineConfig.setLogTemplateExceptions(false);
        engineConfig.setWrapUncheckedExceptions(true);
        engineConfig.setFallbackOnNullLoopVariable(false);
        engineConfig.setTagSyntax(useSquareBracketSyntax ? Configuration.SQUARE_BRACKET_TAG_SYNTAX : Configuration.ANGLE_BRACKET_TAG_SYNTAX);

        configureTemplate();
    }

    private void configureTemplate() {
        // TODO: další template makra ?
        // https://freemarker.apache.org/docs/pgui_config_templateconfigurations.html
        // https://freemarker.apache.org/docs/pgui_datamodel_directive.html
        var markdown = new TemplateConfiguration();
        markdown.setOutputFormat(MDTemplateFormat.INSTANCE);
        engineConfig.setTemplateConfigurations(new ConditionalTemplateConfigurationFactory(
                new FileExtensionMatcher("md"),
                markdown
        ));

        engineConfig.addAutoInclude("directives.txt");
        engineConfig.setAutoEscapingPolicy(Configuration.DISABLE_AUTO_ESCAPING_POLICY);

        MarkdownLineEscapeDirective.setFormatter(formatter);
        engineConfig.setSharedVariable("markdown_single_line", new MarkdownLineEscapeDirective());
    }

    public void loadTemplate(String outputFile, String templatePath) {
        this.outputFile = new File(outputFile);

        var classLoader = new ClassTemplateLoader(this.getClass(), "/templates");

        File templateFile;
        if (templatePath != null) {
            // if template is not specified, use class loader to load the internal template matching selected format (formatter)

            templateFile = new File(templatePath);
            var directory = templateFile.getAbsoluteFile().getParent();
            if (directory == null) directory = "./";

            try {
                var fileLoader = new FileTemplateLoader(new File(directory));

                engineConfig.setTemplateLoader(fileLoader);

                Log.instance().debug("Loading template from " + templateFile.getAbsolutePath());
                this.template = engineConfig.getTemplate(templateFile.getName());
            } catch (TemplateNotFoundException e) {
                Log.instance().error("Template not found: " + templateFile.getAbsolutePath());
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.instance().error("Template format error: " + templateFile.getAbsolutePath());
                throw new RuntimeException(e);
            }
            return;
        }

        engineConfig.setTemplateLoader(classLoader);

        try {
            Log.instance().debug("Loading internal template with " + formatter.getFileExtension() + " format");
            this.template = engineConfig.getTemplate("table." + formatter.getFileExtension());
        } catch (TemplateNotFoundException e) {
            Log.instance().error("Internal template not found for format " + formatter.getFileExtension());
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.instance().error("Internal template format error: " + formatter.getFileExtension());
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getTemplateData(Collection<DocumentedElement> elements) {
        var map = new HashMap<String, Object>();
        map.put("options", elements);
        map.put("format", formatter.getFileExtension().toUpperCase());
        map.put("nl", formatter.linebreak());

        return Collections.unmodifiableMap(map);
    }

    public void generate(Collection<DocumentedElement> elements) {
        if (template == null) {
            throw new IllegalStateException("Template not loaded");
        }

        var data = getTemplateData(elements);

        Log.instance().debug("Writing documentation to " + outputFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(outputFile)) {
            template.process(data, writer);
        } catch (IOException e) {
            Log.instance().error("Error writing to file " + outputFile);
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            Log.instance().error("Error processing output template");
            throw new RuntimeException(e);
        }
    }

}
