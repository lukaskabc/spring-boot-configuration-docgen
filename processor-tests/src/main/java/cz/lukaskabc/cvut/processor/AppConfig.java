package cz.lukaskabc.cvut.processor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("#{valueAnnotationConfig.intValue != null}")
    boolean isIntegerPresentInConfig;
}