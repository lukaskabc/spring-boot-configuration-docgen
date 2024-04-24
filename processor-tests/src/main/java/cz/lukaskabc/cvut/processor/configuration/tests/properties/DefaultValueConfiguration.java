package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties
public class DefaultValueConfiguration {
    private static final boolean BOOLEAN_CONSTANT = true;
    private static final Boolean BOOLEAN_CONSTANT_WRAPPED = true;
    private static final byte BYTE_CONSTANT = 1;
    private static final Byte BYTE_CONSTANT_WRAPPED = 1;
    private static final short SHORT_CONSTANT = 1;
    private static final Short SHORT_CONSTANT_WRAPPED = 1;
    private static final int INT_CONSTANT = 1;
    private static final Integer INT_CONSTANT_WRAPPED = 1;
    private static final long LONG_CONSTANT = 1L;
    private static final Long LONG_CONSTANT_WRAPPED = 1L;
    private static final float FLOAT_CONSTANT = 1.0f;
    private static final Float FLOAT_CONSTANT_WRAPPED = 1.0f;
    private static final double DOUBLE_CONSTANT = 1.0;
    private static final Double DOUBLE_CONSTANT_WRAPPED = 1.0;
    private static final char CHAR_CONSTANT = 'a';
    private static final Character CHAR_CONSTANT_WRAPPED = 'a';
    private static final String STRING_CONSTANT = "string";
    private static final String[] STRING_ARRAY_CONSTANT = {"string1", "string2"};
    private static final Enum ENUM_CONSTANT = Enum.A;
    private static final Enum[] ENUM_ARRAY_CONSTANT = {Enum.A, Enum.B};
    private static final Object OBJECT_CONSTANT = new Object();
    private static final InnerClass INNER_CLASS_CONSTANT = new InnerClass();
    private static final ExternalClass EXTERNAL_CLASS_CONSTANT = new ExternalClass();
    private static final boolean[] PRIMITIVE_BOOLEAN_ARRAY_CONSTANT = {true, false};
    private static final byte STATIC_CONSTANT_METHOD_PROVIDED = staticByteProvider();
    private static final int[] INT_ARRAY_CONSTANT = {1, 2, 3};
    private static final Integer[] INTEGER_ARRAY_CONSTANT = {1, 2, 3};
    private static final List<Integer> INTEGER_LIST_CONSTANT = Arrays.asList(1, 2, 3);
    private static final Object NULL_CONSTANT = null;
    private static final Map<String, Integer> STRING_TO_INTEGER_MAP_CONSTANT = new HashMap<String, Integer>() {{
        put("one", 1);
        put("two", 2);
        put("three", 3);
    }};
    private static final Map<String, Integer> STRING_TO_INTEGER_MAP_CONSTANT_STATIC = new HashMap<String, Integer>();

    static {
        STRING_TO_INTEGER_MAP_CONSTANT_STATIC.put("one", 1);
        STRING_TO_INTEGER_MAP_CONSTANT_STATIC.put("two", 2);
        STRING_TO_INTEGER_MAP_CONSTANT_STATIC.put("three", 3);
    }

    private final long CONSTANT_METHOD_PROVIDED = longProvider();
    boolean booleanPrimitive = true;
    boolean booleanPrimitiveConstant = BOOLEAN_CONSTANT;
    boolean booleanPrimitiveNone;
    Boolean booleanWrapped = true;
    Boolean booleanObject = Boolean.TRUE;
    Boolean booleanWrappedConstant = BOOLEAN_CONSTANT_WRAPPED;
    Boolean booleanObjectNone;

    byte bytePrimitive = 1;
    byte bytePrimitiveConstant = BYTE_CONSTANT;
    byte bytePrimitiveNone;
    Byte byteWrapped = 1;
    Byte byteObject = Byte.valueOf((byte) 1);
    Byte byteWrappedConstant = BYTE_CONSTANT_WRAPPED;
    Byte byteObjectNone;

    short shortPrimitive = 1;
    short shortPrimitiveConstant = SHORT_CONSTANT;
    short shortPrimitiveNone;
    Short shortWrapped = 1;
    Short shortObject = Short.valueOf((short) 1);
    Short shortWrappedConstant = SHORT_CONSTANT_WRAPPED;
    Short shortObjectNone;

    int intPrimitive = 1;
    int intPrimitiveConstant = INT_CONSTANT;
    int intPrimitiveNone;
    Integer intWrapped = 1;
    Integer intObject = Integer.valueOf(1);
    Integer intWrappedConstant = INT_CONSTANT_WRAPPED;
    Integer intObjectNone;

    long longPrimitive = 1L;
    long longPrimitiveConstant = LONG_CONSTANT;
    long longPrimitiveNone;
    Long longWrapped = 1L;
    Long longObject = Long.valueOf(1L);
    Long longWrappedConstant = LONG_CONSTANT_WRAPPED;
    Long longObjectNone;

    float floatPrimitive = 1.0f;
    float floatPrimitiveConstant = FLOAT_CONSTANT;
    float floatPrimitiveNone;
    Float floatWrapped = 1.0f;
    Float floatObject = Float.valueOf(1.0f);
    Float floatWrappedConstant = FLOAT_CONSTANT_WRAPPED;
    Float floatObjectNone;

    double doublePrimitive = 1.0;
    double doublePrimitiveConstant = DOUBLE_CONSTANT;
    double doublePrimitiveNone;
    Double doubleWrapped = 1.0;
    Double doubleObject = Double.valueOf(1.0);
    Double doubleWrappedConstant = DOUBLE_CONSTANT_WRAPPED;
    Double doubleObjectNone;

    char charPrimitive = 'a';
    char charPrimitiveConstant = CHAR_CONSTANT;
    char charPrimitiveNone;
    Character charWrapped = 'a';
    Character charObject = Character.valueOf('a');
    Character charWrappedConstant = CHAR_CONSTANT_WRAPPED;
    Character charObjectNone;

    String string = "stringValue";
    String stringObject = new String("string");
    String stringConstant = STRING_CONSTANT;
    String stringNone;

    Enum enumAttribute = Enum.A;
    Enum enumObject = Enum.valueOf("A");
    Enum enumAttributeConstant = ENUM_CONSTANT;
    Enum enumAttributeNone;

    Enum[] enumArrayAttribute = {Enum.A, Enum.B};
    Enum[] enumArrayAttributeConstant = ENUM_ARRAY_CONSTANT;
    Enum[] enumArrayAttributeNone;

    Object objectAttribute = new Object();
    Object objectAttributeConstant = OBJECT_CONSTANT;
    Object objectAttributeNone;
    Object nullConstant = NULL_CONSTANT;
    Object nullObject = null;

    InnerClass innerClassAttribute = new InnerClass();
    InnerClass innerClassAttributeConstant = INNER_CLASS_CONSTANT;
    InnerClass innerClassAttributeNone;

    @NestedConfigurationProperty
    ExternalClass externalClassAttribute = new ExternalClass();
    @NestedConfigurationProperty
    ExternalClass externalClassConstant = EXTERNAL_CLASS_CONSTANT;
    @NestedConfigurationProperty
    ExternalClass externalClassAttributeNone;
    // without annotation
    ExternalClass nonConvertibleAttribute;

    String[] stringArrayAttribute = {"string1", "string2"};
    String[] stringArrayAttributeConstant = STRING_ARRAY_CONSTANT;
    String[] stringArrayAttributeNone;

    boolean[] primitiveBooleanArrayAttribute = {true, false};
    boolean[] primitiveBooleanArrayAttributeConstant = PRIMITIVE_BOOLEAN_ARRAY_CONSTANT;
    boolean[] primitiveBooleanArrayAttributeNone;

    byte staticMethodProvided = staticByteProvider();
    byte staticMethodProvidedConstant = STATIC_CONSTANT_METHOD_PROVIDED;

    long methodProvided = longProvider();
    long methodProvidedConstant = CONSTANT_METHOD_PROVIDED;


    List<Integer> integerList = Arrays.asList(1, 2, 3);
    List<Integer> integerListConstant = INTEGER_LIST_CONSTANT;
    List<Integer> integerListNone;


    Map<String, Integer> stringToIntegerMap = new HashMap<>();
    Map<String, Integer> stringToIntegerMapConstant = STRING_TO_INTEGER_MAP_CONSTANT;
    Map<String, Integer> stringToIntegerMapConstantStatic = STRING_TO_INTEGER_MAP_CONSTANT_STATIC;
    Map<String, Integer> stringToIntegerMapNone;

    @Value("${number-value}")
    Integer valueAnnotationWithoutDefault;

    @Value("${value-attribute:I am default}") // yes Spring says its valid
    String valueAnnotationWithDefault;

    private static byte staticByteProvider() {
        return 1;
    }

    private long longProvider() {
        return 1L;
    }

    private enum Enum {A, B}

    private static class InnerClass {
        private String foo = "bar";

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

}
