package cz.lukaskabc.cvut.processor.configuration;

import cz.lukaskabc.cvut.processor.configuration.termit.TermitConfiguration;
import cz.lukaskabc.cvut.processor.configuration.termit.TermitRecordConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ComponentScan("cz.lukaskabc.cvut.processor.configuration.termit")
class ConfigurationTest {
    @Autowired
    private BasicValueConfig basicValueConfig;

    @Autowired
    private ConfigurationPropertiesClass configurationPropertiesClass;

    @Autowired
    private TermitConfiguration termitConfiguration;

    @Autowired
    private TermitRecordConfiguration termitRecordConfiguration;

//    @Autowired TermitConfiguration.Repository repository;

    @Test
    void termitConfigurationTest() {
        Assertions.assertNotNull(termitConfiguration.getRepository().getUrl());
//        Assertions.assertNotNull(repository.getUrl());

//        Assertions.assertNotEquals(repository.getUrl(), termitConfiguration.getRepository().getUrl());
    }

    @Test
    void termitRecordConfigurationTest() {
        Assertions.assertNotNull(termitRecordConfiguration.getWorkspace());
        Assertions.assertTrue(termitRecordConfiguration.getWorkspace().allVocabulariesEditable());
    }

    @Test
    void configurationLoads() {
        Assertions.assertNotNull(basicValueConfig);
        Assertions.assertNotNull(configurationPropertiesClass);
        Assertions.assertNotNull(termitConfiguration);
    }


}
