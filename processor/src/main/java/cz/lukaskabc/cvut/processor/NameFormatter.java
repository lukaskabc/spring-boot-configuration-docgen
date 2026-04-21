package cz.lukaskabc.cvut.processor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper class providing methods for name formatting
 */
public class NameFormatter {

    private NameFormatter() {
    }

    /**
     * @param names
     * @return join names with underscore, protecting from duplicated underscores
     * @see <a href="https://docs.spring.io/spring-boot/docs/3.2.1/reference/htmlsingle/#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables">Spring Boot binding from ENV</a>
     */
    public static String combine(String... names) {
        return Arrays.stream(names)
                .map(s ->
                        s.trim()
                                .replaceAll("\\s+", "") // remove spaces
                                .replace("-", "") // remove hyphens
                                .replace(".", "_") // replace dots with underscores
                                .replaceAll("(^_+)|(_+$)", "") // trim underscores
                                .toUpperCase()
                ).filter(s -> !s.isEmpty())
                .collect(Collectors.joining("_"));
    }

    public static String splitUnderscoreOnCapital(String string) {
        StringBuilder builder = new StringBuilder(string.length());
        boolean lastLowerCase = false;
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            boolean isUpperCase = Character.isUpperCase(c);
            if (isUpperCase && lastLowerCase) {
                builder.append("_");
            }
            lastLowerCase = (Character.isAlphabetic(c) || Character.isDigit(c)) && !isUpperCase;
            if (c == '-') {
                builder.append("_");
            } else {
                builder.append(c);
            }
        }
        return builder.toString().toUpperCase();
    }

    /**
     * @param strings
     * @return First non-empty parameter or empty string
     */
    public static String firstNonEmpty(String... strings) {
        return Arrays.stream(strings).filter(Objects::nonNull).filter(s -> !s.isEmpty())
                .findFirst().orElse("");
    }
}
