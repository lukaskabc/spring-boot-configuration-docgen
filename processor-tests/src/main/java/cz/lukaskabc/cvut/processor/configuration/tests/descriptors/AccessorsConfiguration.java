package cz.lukaskabc.cvut.processor.configuration.tests.descriptors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class AccessorsConfiguration {
    @Getter
    @Setter
    private static String lombokStaticAccessor;
    @Getter
    @Setter
    private final String lombokFinalAccessor = "final";
    public String publicAttr;
    private String noAccessor;
    private String plainAccessor;
    private String plainGetterOnly;
    private String plainSetterOnly;
    @Getter
    private String lombokGetterOnly;
    @Setter
    private String lombokSetterOnly;
    @Getter
    @Setter
    private String lombokAccessor;

    public String getPlainAccessor() {
        return plainAccessor;
    }

    public void setPlainAccessor(String plainAccessor) {
        this.plainAccessor = plainAccessor;
    }

    public String getPlainGetterOnly() {
        return plainGetterOnly;
    }

    public void setPlainSetterOnly(String plainSetterOnly) {
        this.plainSetterOnly = plainSetterOnly;
    }
}
