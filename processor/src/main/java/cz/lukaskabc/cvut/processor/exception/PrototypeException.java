package cz.lukaskabc.cvut.processor.exception;

public interface PrototypeException<T> {

    T create(String message);
}
