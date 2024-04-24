package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

@ConfigurationProperties(prefix = "record-tests")
@Data
public class InnerRecordConfiguration {
    /**
     * Class attribute of a record type
     */
    SimpleRecord simpleRecordAttributeName;

    SimpleRecord attributeWithRecordDefault = new SimpleRecord("value1", "value2");
    SimpleRecord attributeWithRecordDefaultOverwrite = new SimpleRecord("value1", "value2");
    SimpleRecord attributeWithRecordDefaultPartialOverwrite = new SimpleRecord("value1", "value2");

    DefaultComponentRecord initializedRecordWithOneDefaultComponent = new DefaultComponentRecord("value1");
    DefaultComponentRecord configuredRecordWithOneDefaultComponent = new DefaultComponentRecord("value1");

    /**
     * Record Javadoc comment
     *
     * @param component1 Record component1 Javadoc
     * @param component2 Record component2 Javadoc
     */
    public record SimpleRecord(String component1, String component2) {
    }

    public record DefaultComponentRecord(String component1) {
        @ConstructorBinding
        public DefaultComponentRecord(String component1) {
            // example for possible handling of null values
            this.component1 = Optional.ofNullable(component1).orElse("constructor-default-value");
        }
    }
}
