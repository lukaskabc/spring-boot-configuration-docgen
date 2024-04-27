# Documentation generator for Spring Boot configuration

Provides **annotation processor**, which collects Javadoc from Spring Boot [configuration properties](https://docs.spring.io/spring-boot/docs/3.2.1/reference/html/features.html#features.external-config.typesafe-configuration-properties.java-bean-binding) classes and [value](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/value-annotations.html) elements
and generates Markdown or HTML documentation for available configuration options using **environmental properties**.
Also supports documentation for JSR 303 validation annotations.

[Example output](example-output.md)

Currently, it is not possible to switch from the environmental variables format.
Initially, the configuration was intended for container customization using environmental variables.

### Other solutions
If this project does not suit your needs (for example, you do not want to use environmental variables), take a look at [Spring Configuration Property Documenter](https://github.com/rodnansol/spring-configuration-property-documenter),
or for a complex documentation solution [Jamal](https://github.com/verhas/jamal).

## Basic installation & usage

The annotation processor is available in [processor module](/processor/) and can be used with any build ecosystem
(even with just plain javac).  

Example usage with Maven:  
Creates a profile with id `configuration-doc` that defines the compiler plugin in annotation processing mode.
One annotation processor is specified, and one compiler arg for processor configuration is present as an example
(it is not necessary as HTML is a default format).  
Executing this profile (`maven compile -P configuration-doc`) should generate file `springboot-configuration.html`. 

```XML
<profiles>
    <profile>
        <id>configuration-doc</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <proc>only</proc>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>cz.lukaskabc.cvut.processor</groupId>
                                <artifactId>spring-boot-configuration-docgen-processor</artifactId>
                                <version>1.0</version>
                            </path>
                        </annotationProcessorPaths>
                        <compilerArgs>
                            <arg>-Aconfigurationdoc.format=HTML</arg>
                        </compilerArgs>
                        <source>${java.version}</source>
                        <target>${java.version}</target>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

## Customization

The annotation processor supports several arguments which control its output and none of them are required.
Every argument can be specified as shown in the example above.
```XML
<arg>-Aconfigurationdoc.ARGUMENT_NAME=VALUE</arg>
```
`-A` stands for argument for annotation processor  
`configurationdoc.` is a prefix used by this processor  
followed by the argument name  
and the value if required

**Supported arguments are:**

| Argument name           | description                                                                                                                |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `output_file`           | Specifies path to the output file<br>Default: `springboot-configuration`                                                   |
| `configuration_package` | Allows to limit documentation only to a specific package                                                                   |
| `format`                | Supported formats: `HTML` and `MD`, note that HTML is supported in many Markdown parsers<br>Default: `HTML`                |
| `template`              | Path to the FreeMarker template used for generation                                                                        |
| `order`                 | Alphabetical order for environmental variables in documentation (`ASC`, `DESC`, `NONE`)<br>Default: `ASC`                  |
| `env_prefix`            | Global prefix for all generated environmental variables                                                                    |
| `prepend_required`      | Prepends all "required" variables                                                                                          |
| `deprecated_last`       | Moves "deprecated" variables to the end                                                                                    |
| `do_not_merge`          | Disables documentation chaining, which is used for merging Javadoc comments from upper contexts (class attributes)         |
| `no_html`               | When Markdown format is used, this processor generates some HTML tags (like `<br>`); using this argument will disable them |


**Javadoc tags**  
If you want to exclude some parameters from documentation, you can use the Javadoc tag `@hidden.`

Although the documentation processor tries to resolve default values for configuration parameters, there are cases when it is not possible or simply inaccurate for your exact use case.  
For this case, the processor supports the Javadoc tag `@configurationdoc.default`, whose value will override the automatically resolved value.  
You might need to add it as custom tag if Javadoc tool is used ([Maven Javadoc plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/examples/tag-configuration.html)).


## Templates

The processor uses the Apache FreeMarker™ template engine for generating the output file.
There are two internal templates prepared for [HTML](/processor/src/main/resources/templates/table.html) and [Markdown](/processor/src/main/resources/templates/table.md) formats and [global directives](/processor/src/main/resources/templates/directives.txt) (macros), 
which can also be used in custom templates specified by the `template` argument.

For example, if you need to add custom configuration parameters that are not explicitly part of the application code,
you can copy the internal template and add lines to the table manually.
Alternatively, you can create some "dummy" attribute with `@Value` annotation somewhere and add documentation to it.

The processor uses FreeMarker [square bracket syntax](https://freemarker.apache.org/docs/dgui_misc_alternativesyntax.html).  
For further reference, please use [FreeMarker documentation](https://freemarker.apache.org/docs/index.html).


## Bachelor thesis

This project initially started as a bachelor's thesis at [Czech Technical University in Prague, Faculty of Electrical Engineering](https://fel.cvut.cz/en).  
<!-- TODO: The Thesis is available in Czech on [DSpace](#). -->  
The goal was to create a tool that could be easily integrated into existing Java projects,
and will generate documentation from Spring Boot configuration classes ([ConfigurationProperties](https://docs.spring.io/spring-boot/docs/3.2.1/reference/html/features.html#features.external-config.typesafe-configuration-properties.java-bean-binding)) using Javadoc comments.
Configuration is in form of environmental variables.
Emphasis was placed on using Markdown for the output format to facilitate posting the documentation on GitHub in a README file or similar.
The project was primarily developed to generate documentation of [TermIt](https://github.com/kbss-cvut/termit).
