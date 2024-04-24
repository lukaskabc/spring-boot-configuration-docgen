package cz.lukaskabc.cvut.processor.configuration.termit;

/**
 * Original file at https://github.com/kbss-cvut/termit/blob/master/src/main/java/cz/cvut/kbss/termit/util/Configuration.java
 * Authors: Martin Ledvinka, Petr Křemen, Michal Med, Filip Kopecký
 * Modified by Lukáš Kaňka as configuration example
 */

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * Represents application-wide configuration.
 * <p>
 * The runtime configuration consists of predefined default values
 * and configuration loaded from config files on
 * classpath. Values from config files supersede the default values.
 * <p>
 * The configuration can be also set via
 * <a href="https://docs.spring.io/">OS environment variables</a>.
 * These override any statically configured values.
 */
@ConfigurationProperties("termit")
@Validated
public class TermitConfiguration {
    /**
     * TermIt frontend URL.
     * <p>
     * It is used, for example, for links in emails sent to users.
     *
     * @default https://localhost:3000/
     */
    public String url = "https://localhost:3000/";

    @Valid
    private Repository repository = new Repository();

    public String getUrl() {
        return url;
    }
    // getter & setter

    public void setUrl(String url) {
        this.url = url;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public static class Repository {
        /**
         * URL of the main application repository.
         */
        @NotNull
        String url;
        /**
         * Public URL of the main application repository.
         * <p>
         * Can be used to provide read-only no authorization
         * access to the underlying data.
         */
        Optional<String> publicUrl = Optional.empty();

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Optional<String> getPublicUrl() {
            return publicUrl;
        }

        public void setPublicUrl(Optional<String> publicUrl) {
            this.publicUrl = publicUrl;
        }

        // getter & setter
    }
}