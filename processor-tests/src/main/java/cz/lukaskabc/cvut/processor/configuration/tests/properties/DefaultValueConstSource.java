package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import java.util.Arrays;
import java.util.List;

public class DefaultValueConstSource {
    public static final byte BYTE_CONSTANT = 1;
    public static final String[] STRING_ARRAY_CONSTANT = {"string1", "string2"};
    public static final DefaultValueExternalStaticConfiguration.PublicEnum ENUM_CONSTANT = DefaultValueExternalStaticConfiguration.PublicEnum.A;
    public static final DefaultValueExternalStaticConfiguration.PublicEnum[] ENUM_ARRAY_CONSTANT = {DefaultValueExternalStaticConfiguration.PublicEnum.A, DefaultValueExternalStaticConfiguration.PublicEnum.B};
    public static final Object OBJECT_CONSTANT = new Object();
    public static final LocalInnerClass INNER_CLASS_CONSTANT = new LocalInnerClass();
    public static final int[] INT_ARRAY_CONSTANT = {1, 2, 3};
    public static final List<Integer> INTEGER_LIST_CONSTANT = Arrays.asList(1, 2, 3);
    public static final Object NULL_CONSTANT = null;

    public static class LocalInnerClass {
        private LocalInnerClass() {
        }
    }
}
