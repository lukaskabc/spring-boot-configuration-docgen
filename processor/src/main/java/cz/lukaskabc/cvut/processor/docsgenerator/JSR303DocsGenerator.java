package cz.lukaskabc.cvut.processor.docsgenerator;

import cz.lukaskabc.cvut.processor.DocumentedElement;
import cz.lukaskabc.cvut.processor.Log;
import cz.lukaskabc.cvut.processor.formatter.Formatter;
import jakarta.validation.constraints.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Documentation generator for Jakarta Bean Validation API 3.0.0
 *
 * @see <a href="https://jakarta.ee/specifications/bean-validation/3.0/apidocs/">Jakarta Bean Validation API 3.0.0</a>
 */
public class JSR303DocsGenerator implements DocGenerator {

    private static final String JAKARTA_CONSTRAINTS_PACKAGE = "jakarta.validation.constraints.";

    private static final String JAVAX_CONSTRAINTS_PACKAGE = "javax.validation.constraints.";

    public static List<? extends AnnotationMirror> getAnnotations(Element element) {
        return element.getAnnotationMirrors().stream().filter(annotationMirror -> {
            var type = (TypeElement) annotationMirror.getAnnotationType().asElement();
            var isValidationConstraint = type.getQualifiedName().toString().startsWith(JAKARTA_CONSTRAINTS_PACKAGE);

            if (!isValidationConstraint && type.getQualifiedName().toString().startsWith(JAVAX_CONSTRAINTS_PACKAGE)) {
                isValidationConstraint = true;
                Log.withContext(element).warn("You are using old javax validation constraint " + type.getQualifiedName() + " - use " + JAKARTA_CONSTRAINTS_PACKAGE + "* package when possible");
            }


            return isValidationConstraint;
        }).toList();
    }

    private <T> T castOrNull(Object value, Class<T> resultType) {
        if (value == null)
            return null;

        if (resultType.equals(String.class))
            value = String.valueOf(value);

        try {
            return resultType.cast(value);
        } catch (ClassCastException ignored) {
            Log.instance().debug("Failed cast of " + value.getClass() + " to " + resultType);
            return null;
        }
    }

    private <T> Optional<T> getAnnotationValue(AnnotationMirror annotationMirror, String value, Class<T> resultType) {
        var elementValues = annotationMirror.getElementValues();
        for (var entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(value) && entry.getValue() != null) {
                var result = castOrNull(entry.getValue().getValue(), resultType);
                return Optional.ofNullable(result);
            }
        }
        return Optional.empty();
    }

    private <T> Optional<T> getAnnotationValue(AnnotationMirror annotationMirror, Class<T> resultType) {
        return getAnnotationValue(annotationMirror, "value", resultType);
    }

    String getAnnotationBaseName(AnnotationMirror annotationMirror) {
        var element = annotationMirror.getAnnotationType().asElement();
        var name = element.getSimpleName();
        if (name.toString().equals("List"))
            return element.getEnclosingElement().getSimpleName().toString();
        return name.toString();
    }

    Optional<Method> getMethodForAnnotation(AnnotationMirror annotationMirror) {
        var element = annotationMirror.getAnnotationType().asElement();
        var methodName = element.getSimpleName().toString();

        if (methodName.equals("List")) {
            methodName = element.getEnclosingElement().getSimpleName().toString() + methodName;
        }

        try {
            return Optional.of(JSR303DocsGenerator.class
                    .getMethod("constraint" + methodName,
                            Formatter.class,
                            AnnotationMirror.class,
                            DocumentedElement.class));

        } catch (NoSuchMethodException e) {
            Log.instance().debug("Failed to document annotation constraint " + methodName);
            return Optional.empty();
        }
    }

    @Override
    public boolean generate(Formatter formatter, DocumentedElement element) {
        var annotations = getAnnotations(element.getDecorator().getElement());
        boolean first = true;
        for (var annotation : annotations) {
            var method = getMethodForAnnotation(annotation);

            if (method.isEmpty())
                continue;

            try {
                var localFormatter = formatter.emptyClone();
                Object result = method.get().invoke(this, localFormatter, annotation, element);

                // ensures that when method fails it won't append invalid (incomplete) output
                if ((Boolean) result) {
                    if (!first) {
                        formatter.newline();
                    } else first = false;
                    formatter.append(localFormatter.toString());
                } else {
                    Log.withContext(element.getDecorator().getElement()).error("Failed to document annotation constraint " + getAnnotationBaseName(annotation));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        }

        return true;
    }

    private void markRequired(DocumentedElement decorator) {
        decorator.setRequired();
    }

    private boolean end(Formatter formatter) {
        formatter.newline();
        return true;
    }

    private boolean appendAnnotationValue(AnnotationMirror annotationMirror, Formatter formatter, String valueName) {
        var message = getAnnotationValue(annotationMirror, valueName, String.class);
        if (message.isPresent() && !message.get().isBlank()) {
            formatter.append(" ").append(message.get());
            return true;
        }
        return false;
    }

    private boolean appendAnnotationMessageValue(AnnotationMirror annotationMirror, Formatter formatter) {
        return appendAnnotationValue(annotationMirror, formatter, "message");
    }

    private boolean appendAnnotationValueFromList(List<Object> list, Formatter formatter, String valueName) {
        for (var el : list) {
            var mirror = (AnnotationMirror) el;
            if (appendAnnotationValue(mirror, formatter, valueName)) {
                formatter.newline();
            }
        }
        return true;
    }

    boolean appendAnnotationMessageValueFromList(List<Object> list, Formatter formatter) {
        formatter.newline();
        return appendAnnotationValueFromList(list, formatter, "message");
    }

    /**
     * The value has to be false
     *
     * @param formatter
     * @param annotationMirror {@link AssertFalse}
     * @return true on success
     */
    public boolean constraintAssertFalse(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("has to be ")
                .code("false");

        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Multiple @AssertFalse, prints "has to be false" and append messages from all annotations
     *
     * @param formatter
     * @param annotationMirror {@link AssertFalse}[]
     * @return true on success
     */
    public boolean constraintAssertFalseList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("has to be ")
                .code("false");

        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }

        return end(formatter);
    }

    /**
     * The value has to be true
     *
     * @param formatter
     * @param annotationMirror {@link AssertTrue}
     * @return true on success
     */
    public boolean constraintAssertTrue(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.rawAppend("has to be ")
                .code("true");

        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Multiple @AssertTrue, prints "has to be true" and append messages from all annotations
     *
     * @param formatter
     * @param annotationMirror {@link AssertTrue}[]
     * @return true on success
     */
    public boolean constraintAssertTrueList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.rawAppend("has to be ")
                .code("true");

        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }

        return end(formatter);
    }

    /**
     * Finds @{@link DecimalMin} or @{@link Min} annotation with maximum/minimum value
     *
     * @param annotations
     * @return {@link AnnotationMirror}
     */
    private Optional<AnnotationMirror> findAnnotationWithComparedValue(List<AnnotationMirror> annotations, boolean searchMax) {
        BigDecimal maximum = null;
        AnnotationMirror maximumAnn = null;
        boolean maximumInclusive = false;

        Comparator<Integer> comparator = searchMax ? Comparator.naturalOrder() : Comparator.reverseOrder();

        for (var mirror : annotations) { // for each annotation from the list
            var value = getAnnotationValue(mirror, "value", String.class); // obtain value
            if (value.isEmpty())
                continue; // if empty, then skip

            try {
                var dec = new BigDecimal(value.get()); // try to convert it to bigdecimal
                int comparsion = -1;
                if (maximum != null) { // when we can compare
                    comparsion = dec.compareTo(maximum);
                }
                if (maximum == null || comparator.compare(comparsion, 0) > 0) { // if first run - as no maximum yet, or dec > maximum
                    maximum = dec;
                    maximumAnn = mirror;
                    maximumInclusive = getAnnotationValue(mirror, "inclusive", Boolean.class).orElse(true);
                } else if (comparsion == 0) { // dec == maximum
                    var inclusive = getAnnotationValue(mirror, "inclusive", Boolean.class).orElse(true);
                    if (maximumInclusive && !inclusive) { // if the maximum is inclusive and dec is not,
                        // then dec > maximum
                        maximum = dec;
                        maximumAnn = mirror;
                        maximumInclusive = inclusive;
                    }
                }
            } catch (NumberFormatException ignored) {
                Log.instance().error("Invalid parameter of " + mirror + " annotation: \"" + value.get() + "\" - invalid number format");
            }
        }

        return Optional.ofNullable(maximumAnn);
    }

    /**
     * Prints "value <= maximum" with regard to the inclusive flag<br>
     * Does not perform any validation of annotation value
     *
     * @param formatter
     * @param annotationMirror {@link DecimalMax} or {@link Max}
     * @return true on success
     */
    public boolean constraintDecimalMax(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        boolean inclusive = getAnnotationValue(annotationMirror, "inclusive", Boolean.class).orElse(true);
        formatter.append("value <");
        if (inclusive)
            formatter.append("=");
        formatter.append(" ");
        var limit = getAnnotationValue(annotationMirror, "value", String.class);
        if (limit.isEmpty())
            return false;
        formatter.code(limit.get());

        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * From an array of @{@link DecimalMax} or @{@link Max} annotations selects the one with minimum value
     * and non-inclusive flag and passes it to constraintDecimalMax<br>
     * Performs cast of the value to BigDecimal, no output on error
     *
     * @param formatter
     * @param annotationMirror DecimalMax[] or Max[]
     * @return true on success
     */
    public boolean constraintDecimalMaxList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {

        var annotations = getAnnotationValue(annotationMirror, List.class);

        if (annotations.isEmpty())
            return false;

        @SuppressWarnings("unchecked")
        var minimumAnn = findAnnotationWithComparedValue((List<AnnotationMirror>) annotations.get(), false);


        if (minimumAnn.isEmpty()) // if no valid minimum was found, just return
            return false;

        return constraintDecimalMax(formatter, minimumAnn.get(), decorator);
    }

    /**
     * Prints "value >= minimum" with regard to the inclusive flag<br>
     * Does not perform any validation of annotation value
     *
     * @param formatter
     * @param annotationMirror {@link DecimalMin} or {@link Min}
     * @return true on success
     */
    public boolean constraintDecimalMin(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        boolean inclusive = getAnnotationValue(annotationMirror, "inclusive", Boolean.class).orElse(true);
        formatter.append("value >");
        if (inclusive)
            formatter.append("=");
        formatter.append(" ");
        var limit = getAnnotationValue(annotationMirror, "value", String.class);
        if (limit.isEmpty())
            return false;
        formatter.code(limit.get());

        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * From an array of @{@link DecimalMin} or @{@link Min} annotations selects the one with maximum value
     * and non-inclusive flag and passes it to constraintDecimalMin<br>
     * Performs cast of the value to BigDecimal, no output on error
     *
     * @param formatter
     * @param annotationMirror {@link DecimalMin}[] or {@link Min}[]
     * @return true on success
     */
    public boolean constraintDecimalMinList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {

        var annotations = getAnnotationValue(annotationMirror, List.class);

        if (annotations.isEmpty())
            return false;

        @SuppressWarnings("unchecked")
        var maximumAnn = findAnnotationWithComparedValue((List<AnnotationMirror>) annotations.get(), true);

        if (maximumAnn.isEmpty()) // if no valid maximum was found, just return
            return false;

        return constraintDecimalMin(formatter, maximumAnn.get(), decorator);
    }

    /**
     * Value can have a limited number of digits before and after the decimal point
     *
     * @param formatter
     * @param annotationMirror {@link Digits}
     * @return true on success
     */
    public boolean constraintDigits(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        var fraction = getAnnotationValue(annotationMirror, "fraction", Integer.class)
                .orElseThrow(() -> new IllegalStateException("Digits annotation is missing required parameter fraction"));

        var integer = getAnnotationValue(annotationMirror, "integer", Integer.class)
                .orElseThrow(() -> new IllegalStateException("Digits annotation is missing required parameter integer"));

        formatter.rawAppend("maximum of ")
                .code(integer.toString())
                .rawAppend(" digits before the decimal point and ")
                .code(fraction.toString())
                .rawAppend(" digits after it");

        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * From all annotations, pick a minimum fraction and minimum integer
     *
     * @param formatter
     * @param annotationMirror {@link Digits}[]
     * @return true on success
     */
    public boolean constraintDigitsList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        Integer fraction = null;
        Integer integer = null;

        var localFormatter = formatter.emptyClone();

        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            for (var el : annotations.get()) { // for each annotation from the list
                var mirror = (AnnotationMirror) el;
                var frac = getAnnotationValue(mirror, "fraction", Integer.class).orElse(fraction);
                var integ = getAnnotationValue(mirror, "integer", Integer.class).orElse(integer);
                if (fraction == null || frac < fraction)
                    fraction = frac;
                if (integer == null || integ < integer)
                    integer = integ;
                localFormatter.newline();
                appendAnnotationMessageValue(mirror, localFormatter);
            }
        }

        if (fraction == null || integer == null)
            return false;

        formatter.rawAppend("maximum of ")
                .code(integer.toString())
                .rawAppend(" digits before the decimal point and ")
                .code(fraction.toString())
                .rawAppend(" digits after it");

        var localFormatterString = formatter.removeDoubleSpaces(localFormatter.toString());
        if (!localFormatterString.isBlank()) {
            formatter.rawAppend("; ")
                    .append(localFormatterString);
        }

        return end(formatter);
    }

    /**
     * Value has to be a valid email address
     *
     * @param formatter
     * @param annotationMirror {@link Email}
     * @return true on success
     */
    public boolean constraintEmail(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("valid email");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Valid email address and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Email}[]
     * @return true on success
     */
    public boolean constraintEmailList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("valid email");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * Value must be time in the future
     *
     * @param formatter
     * @param annotationMirror {@link Future}
     * @return true on success
     */
    public boolean constraintFuture(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in future");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Time in future and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Future}[]
     * @return true on success
     */
    public boolean constraintFutureList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in future");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * Value must be time in the present or in the future
     *
     * @param formatter
     * @param annotationMirror {@link Future}
     * @return true on success
     */
    public boolean constraintFutureOrPresent(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in the present or in the future");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Time in the present or in the future and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Future}[]
     * @return true on success
     */
    public boolean constraintFutureOrPresentList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in the present or in the future");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * Passed to {@link JSR303DocsGenerator#constraintDecimalMax}
     *
     * @param formatter
     * @param annotationMirror {@link Max}
     * @return true on success
     */
    public boolean constraintMax(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        return constraintDecimalMax(formatter, annotationMirror, decorator);
    }

    /**
     * Passed to {@link JSR303DocsGenerator#constraintDecimalMaxList}
     *
     * @param formatter
     * @param annotationMirror {@link Max}[]
     * @return true on success
     */
    public boolean constraintMaxList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        return constraintDecimalMaxList(formatter, annotationMirror, decorator);
    }

    /**
     * Passed to {@link JSR303DocsGenerator#constraintDecimalMin}
     *
     * @param formatter
     * @param annotationMirror {@link Min}
     * @return true on success
     */
    public boolean constraintMin(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        return constraintDecimalMin(formatter, annotationMirror, decorator);
    }

    /**
     * Passed to {@link JSR303DocsGenerator#constraintDecimalMinList}
     *
     * @param formatter
     * @param annotationMirror {@link Min}[]
     * @return true on success
     */
    public boolean constraintMinList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        return constraintDecimalMinList(formatter, annotationMirror, decorator);
    }

    /**
     * The value must be less than zero<br>
     * Prints "value < 0"
     *
     * @param formatter
     * @param annotationMirror {@link Negative}
     * @return true on success
     */
    public boolean constraintNegative(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value < 0");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Prints "value < 0" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Negative}[]
     * @return true on success
     */
    public boolean constraintNegativeList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value < 0");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * The value must be less or equal to zero
     *
     * @param formatter
     * @param annotationMirror {@link NegativeOrZero}
     * @return true on success
     */
    public boolean constraintNegativeOrZero(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value <= 0");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Prints "value <= 0" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link NegativeOrZero}[]
     * @return true on success
     */
    public boolean constraintNegativeOrZeroList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value <= 0");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * The value must be present and contain at least one non-whitespace character
     *
     * @param formatter
     * @param annotationMirror {@link NotBlank}
     * @return true on success
     */
    public boolean constraintNotBlank(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value must be present and contain at least one non-whitespace character");
        appendAnnotationMessageValue(annotationMirror, formatter);
        markRequired(decorator);
        return end(formatter);
    }

    /**
     * Prints "value must be present and contain at least one non-whitespace character" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link NotBlank}[]
     * @return true on success
     */
    public boolean constraintNotBlankList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value must be present and contain at least one non-whitespace character");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        markRequired(decorator);
        return end(formatter);
    }

    /**
     * Value must be present and not empty
     *
     * @param formatter
     * @param annotationMirror {@link NotEmpty}
     * @return true on success
     */
    public boolean constraintNotEmpty(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value must be present and not empty");
        appendAnnotationMessageValue(annotationMirror, formatter);
        markRequired(decorator);
        return end(formatter);
    }

    /**
     * Prints "value must be present and not empty" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link NotEmpty}[]
     * @return true on success
     */
    public boolean constraintNotEmptyList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value must be present and not empty");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        markRequired(decorator);
        return end(formatter);
    }

    /**
     * The value must be present
     *
     * @param formatter
     * @param annotationMirror {@link NotNull}
     * @return true on success
     */
    public boolean constraintNotNull(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value must be present");
        appendAnnotationMessageValue(annotationMirror, formatter);
        markRequired(decorator);
        return end(formatter);
    }

    /**
     * Prints "value must be present" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link NotNull}[]
     * @return true on success
     */
    public boolean constraintNotNullList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("value must be present");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        markRequired(decorator);
        return end(formatter);
    }

    /**
     * Value must be null
     *
     * @param formatter
     * @param annotationMirror {@link Null}
     * @return true on success
     */
    public boolean constraintNull(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("no value accepted, leave blank");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Prints "no value accepted, leave blank" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Null}[]
     * @return true on success
     */
    public boolean constraintNullList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("no value accepted, leave blank");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * The value must be time in the past
     *
     * @param formatter
     * @param annotationMirror {@link Past}
     * @return true on success
     */
    public boolean constraintPast(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in the past");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Prints "time in the past" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Past}[]
     * @return true on success
     */
    public boolean constraintPastList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in the past");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * The value must be time in the past or present
     *
     * @param formatter
     * @param annotationMirror {@link PastOrPresent}
     * @return true on success
     */
    public boolean constraintPastOrPresent(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in the past or present");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Prints "time in the past or present" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link PastOrPresent}[]
     * @return true on success
     */
    public boolean constraintPastOrPresentList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("time in the past or present");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    public boolean constraintPattern(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        var regexp = getAnnotationValue(annotationMirror, "regexp", String.class);
        if (regexp.isPresent()) {
            formatter.rawAppend("value must match regular expression ")
                    .code(regexp.get());
            appendAnnotationMessageValue(annotationMirror, formatter);
            return end(formatter);
        }
        return false;
    }

    /**
     * Prints "value must match regular expressions: " and appends all expressions and messages
     *
     * @param formatter
     * @param annotationMirror {@link Pattern}[]
     * @return true on success
     */
    public boolean constraintPatternList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            formatter.append("value must match regular expressions:").newline();
            Formatter exp;
            for (var ann : annotations.get()) {
                exp = formatter.emptyClone();
                var annotation = (AnnotationMirror) ann;
                appendAnnotationValue(annotation, exp, "regexp");

                formatter.code(exp.toString().trim());
                appendAnnotationMessageValue(annotation, formatter);
                formatter.newline();
            }
            return end(formatter);
        }
        return false;
    }

    /**
     * The value must be greater than zero
     *
     * @param formatter
     * @param annotationMirror {@link Positive}
     * @return true on success
     */
    public boolean constraintPositive(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("0 < value");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Prints "0 < value" and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Positive}[]
     * @return true on success
     */
    public boolean constraintPositiveList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("0 < value");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * The value must be greater than or equal to zero
     *
     * @param formatter
     * @param annotationMirror {@link PositiveOrZero}
     * @return true on success
     */
    public boolean constraintPositiveOrZero(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("0 <= value");
        appendAnnotationMessageValue(annotationMirror, formatter);
        return end(formatter);
    }

    /**
     * Prints "value >= 0" and appends all messages.
     *
     * @param formatter
     * @param annotationMirror {@link PositiveOrZero}[]
     * @return true on success
     */
    public boolean constraintPositiveOrZeroList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        formatter.append("0 <= value");
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent()) {
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
        }
        return end(formatter);
    }

    /**
     * The value must be in the specified range
     *
     * @param formatter
     * @param annotationMirror {@link Size}
     * @return true on success
     */
    public boolean constraintSize(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        var min = getAnnotationValue(annotationMirror, "min", Integer.class);
        var max = getAnnotationValue(annotationMirror, "max", Integer.class);
        if (min.isPresent() && max.isPresent()) {
            formatter.code(min.get().toString())
                    .rawAppend(" <= value length/size <= ")
                    .code(max.get().toString());
            appendAnnotationMessageValue(annotationMirror, formatter);
            return end(formatter);
        }
        return false;
    }

    /**
     * The value must be in the specified range (picks the first annotation and uses that range) and appends all messages
     *
     * @param formatter
     * @param annotationMirror {@link Size}[]
     * @return true on success
     */
    public boolean constraintSizeList(Formatter formatter, AnnotationMirror annotationMirror, DocumentedElement decorator) {
        var annotations = getAnnotationValue(annotationMirror, List.class);
        if (annotations.isPresent() && !annotations.get().isEmpty()) {
            Integer max = null;
            Integer min = null;
            for (var ann : annotations.get()) {
                var annotation = (AnnotationMirror) ann;
                var aMin = getAnnotationValue(annotation, "min", Integer.class);
                var aMax = getAnnotationValue(annotation, "max", Integer.class);
                if (aMax.isPresent() && (max == null || max > aMax.get()))
                    max = aMax.get();

                if (aMin.isPresent() && (min == null || min < aMin.get()))
                    min = aMin.get();
            }

            if (min != null && max != null) {
                formatter.code(min.toString())
                        .rawAppend(" <= value length/size <= ")
                        .code(max.toString());
            }
            appendAnnotationMessageValueFromList(annotations.get(), formatter);
            return end(formatter);
        }
        return false;
    }


}
