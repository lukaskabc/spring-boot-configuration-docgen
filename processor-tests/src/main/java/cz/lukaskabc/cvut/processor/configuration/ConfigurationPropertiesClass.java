package cz.lukaskabc.cvut.processor.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@ConfigurationProperties(prefix = "configuration-properties")
@Validated
public class ConfigurationPropertiesClass {
    @Setter
    @Getter
    public String[] stringArray;

    @NotNull
//    @Getter
    @Setter
    public String cpTopLevelValue;
    private String cpPrivateValueNoAccessors;

    @Getter
    @Setter
    private String cpPrivateValue;

    public static class CPSubClass {
        public String cpSubClassValue;

        public String getCpSubClassValue() {
            return cpSubClassValue;
        }

        public void setCpSubClassValue(String cpSubClassValue) {
            this.cpSubClassValue = cpSubClassValue;
        }
    }


}
