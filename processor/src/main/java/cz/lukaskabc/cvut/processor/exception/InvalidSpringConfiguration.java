package cz.lukaskabc.cvut.processor.exception;

public class InvalidSpringConfiguration extends RuntimeException implements PrototypeException<InvalidSpringConfiguration> {

    public InvalidSpringConfiguration(String message) {
        super(message);
    }

    public InvalidSpringConfiguration() {
        super();
    }

    @Override
    public InvalidSpringConfiguration create(String message) {
        return new InvalidSpringConfiguration(message);
    }
}
