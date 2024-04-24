package cz.lukaskabc.cvut.processor.configuration.tests.value;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ValueAnnotationCommentOverloading {
    /**
     * First usage of value
     */
    @Value("${differentComments}")
    String firstUsage;

    /**
     * Second usage of value
     */
    @Value("${differentComments}")
    String secondUsage;

    /**
     * Repeated usage of the same comment
     */
    @Value("${sameComments}")
    String firstUsageSame;

    /**
     * Repeated usage of the same comment
     */
    @Value("${sameComments}")
    String secondUsageSame;
}
