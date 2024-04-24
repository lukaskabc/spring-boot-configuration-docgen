package cz.lukaskabc.cvut.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import javax.swing.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ConfigurationPropertiesScan
public class App {
    public static void main(String[] args) {
        var app = new SpringApplication(App.class);
        // setting global prefix for environment variables
        // https://docs.spring.io/spring-boot/docs/3.2.1/reference/htmlsingle/#features.external-config.system-environment
        app.setEnvironmentPrefix("my_app");
        app.run(args);
    }
}