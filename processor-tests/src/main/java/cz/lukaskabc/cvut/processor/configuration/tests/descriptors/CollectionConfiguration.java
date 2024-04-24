package cz.lukaskabc.cvut.processor.configuration.tests.descriptors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties
public class CollectionConfiguration {
    /**
     * Uninitialized collection without getter and setter is not bindable (so not documented)
     */
    Collection<String> notInitialized;

    /**
     * Uninitialized collection with getter is not bindable, setter missing
     */
    @Getter
    Collection<String> notInitializedWithGetter;

    /**
     * This attribute is bindable and documented
     */
    @Getter
    @Setter
    Collection<String> notInitializedWithSetter;

    /**
     * No getter is present and is initialized, so the attribute is not bindable
     */
    Collection<String> initializedWithoutGetter = new ArrayList<>();

    /**
     * This attribute is bindable and documented
     */
    @Getter
    Collection<String> initializedWithGetter = new ArrayList<>();

    /**
     * list of integers
     */
    @Getter
    List<Integer> list = new ArrayList<>();

    /**
     * set of dates
     */
    @Getter
    Set<Date> set = new HashSet<>();

    /**
     * map of strings
     */
    @Getter
    Map<String, String> map = new HashMap<>();

    @Getter
    @Setter
    Map<Integer, String> notInitializedMap;
}
