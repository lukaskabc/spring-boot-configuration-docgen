package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Data
@ConfigurationProperties
public class JavadocTagsConfiguration {

    /**
     * Standard Javadoc text. With more than one sentence.
     * And more than one line. Also {@code lets add some <code> *formatted* text}.
     * <p>
     * With some HTML paragraph.<br>and HTML new lines.<br>
     * {@literal <html literal="text">}<br>
     * {@literal *MD* literal _text\_}<br>
     * {@link JavadocTagsConfiguration} without label<br>
     * {@link JavadocTagsConfiguration with label}<br>
     * {@linkplain JavadocTagsConfiguration} plain without label<br>
     * {@linkplain JavadocTagsConfiguration with*<literal>*label}
     * @see JavadocTagsConfiguration.InnerClass
     * @see <a href="#">HTML link</a>
     * @since 1.5
     * @deprecated description of deprecated javadoc tag
     */
    private String attribute;

    /**
     * @deprecated
     */
    private InnerClass nested;

    /**
     * This is a hidden attribute
     * @hidden This attribute is not show in docs
     */
    private Integer hiddenAttribute;

    public static class InnerClass {
        private String innerAttribute;

        /**
         * This is a description of the constructor in inner class
         * @author This tag should NOT be included.
         * @exception Exception This tag should NOT be included.
         * @param innerAttribute this is included in docs
         */
        @ConstructorBinding
        public InnerClass(String innerAttribute) {
        }
    }

}
