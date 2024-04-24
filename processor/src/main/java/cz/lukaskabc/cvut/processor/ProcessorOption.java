package cz.lukaskabc.cvut.processor;

/**
 * Interface for annotation processor options
 */
public interface ProcessorOption {

    /**
     * @return name of the option
     */
    String getName();

    /**
     * Called when the option is present in the command line with its parameter or null if no parameter is present
     *
     * @param parameterValue null or String value of the parameter
     * @throws IllegalArgumentException if the option usage is invalid (wrong parameter)
     */
    void process(String parameterValue) throws IllegalArgumentException;
}
