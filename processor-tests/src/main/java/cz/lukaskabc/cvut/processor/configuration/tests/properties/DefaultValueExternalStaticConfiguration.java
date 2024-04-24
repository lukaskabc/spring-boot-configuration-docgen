package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static cz.lukaskabc.cvut.processor.configuration.tests.properties.DefaultValueConstSource.*;

@ConfigurationProperties
@Data
public class DefaultValueExternalStaticConfiguration {
    byte byteAttribute = BYTE_CONSTANT;
    String[] stringArrayAttribute = DefaultValueConstSource.STRING_ARRAY_CONSTANT;
    PublicEnum enumAttribute = DefaultValueConstSource.ENUM_CONSTANT;
    PublicEnum[] enumArrayAttribute = ENUM_ARRAY_CONSTANT;

    Object objectAttribute = DefaultValueConstSource.OBJECT_CONSTANT;
    int[] intArrayAttribute = DefaultValueConstSource.INT_ARRAY_CONSTANT;
    java.util.List<Integer> integerListAttribute = DefaultValueConstSource.INTEGER_LIST_CONSTANT;
    Object nullAttribute = DefaultValueConstSource.NULL_CONSTANT;
    Object importedNullAttribute = NULL_CONSTANT;

    String enumToStringAttribute = ENUM_CONSTANT.toString();
    String enumWithClassNameAttribute = DefaultValueConstSource.ENUM_CONSTANT.toString();
    String enumStringValueOfAttribute = String.valueOf(ENUM_CONSTANT);
    String enumStringValueOfWithClassNameAttribute = String.valueOf(DefaultValueConstSource.ENUM_CONSTANT);

    public enum PublicEnum {A, B}
}
