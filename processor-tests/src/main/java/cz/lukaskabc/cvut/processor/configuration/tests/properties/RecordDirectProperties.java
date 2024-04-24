package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Record javadoc
 *
 * @param componentA    componentA javadoc description
 * @param listComponent listComponent javadoc description
 */
@ConfigurationProperties
public record RecordDirectProperties(@DefaultValue("compA default") String componentA, List<Integer> listComponent) {

}
