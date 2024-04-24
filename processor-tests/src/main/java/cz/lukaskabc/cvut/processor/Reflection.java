package cz.lukaskabc.cvut.processor;

import java.lang.reflect.Method;

public class Reflection {
    public static void main(String[] args) throws Exception {
        Foo foo = new Foo();

        String setterName = "set" + "Attribute";
        String valueToSet = "value";

        Method m = Foo.class.getDeclaredMethod(setterName, String.class);

        m.invoke(foo, valueToSet);
    }
}
