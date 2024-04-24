package cz.lukaskabc.cvut.processor;

/**
 * Base class for annotationprocessor (CLI) options
 */
public abstract class AbstractProcessorOption implements ProcessorOption {

    private final String name;

    private final String description;

    private final String parameter;

    AbstractProcessorOption(String name, String description, String parameter) {
        this.name = name;
        this.description = description;
        this.parameter = parameter;
    }

    public String invalidUsage() {
        String param = parameter == null ? "" : " " + parameter;

        return "Invalid " + name + " parameter\n" +
                "Usage: " + name + param + "\n" + description;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Implementation of the process method for specific option
     *
     * @param parameterValue null or String value of the parameter
     * @return true on success, false on failure or invalid parameter
     * @implNote when false is returned, the invalidUsage error is printed and IllegalArgumentException is thrown
     */
    protected abstract boolean processImpl(String parameterValue);

    @Override
    public final void process(String parameterValue) {
        if (parameter != null && (parameterValue == null || parameterValue.isBlank())) {
            System.err.println(invalidUsage());
            throw new IllegalArgumentException();
        } else if (parameter != null) {
            parameterValue = parameterValue.trim();
        }


        if (!processImpl(parameterValue)) {
            System.err.println(invalidUsage());
            throw new IllegalArgumentException();
        }
    }
}
