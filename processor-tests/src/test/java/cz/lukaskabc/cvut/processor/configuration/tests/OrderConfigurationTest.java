package cz.lukaskabc.cvut.processor.configuration.tests;

import cz.lukaskabc.cvut.processor.configuration.tests.properties.order.OrderConfiguration;
import org.junit.jupiter.api.Test;

class OrderConfigurationTest extends AbstractProcessorTest {
    @Override
    protected String getFolderName() {
        return "properties/order";
    }

    @Test
    void Stable_order_output_when_no_order_is_specified() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16);
        // default order is ASC
        validateFiles(OrderConfiguration.class.getSimpleName() + "-asc-order", "html");
    }

    @Test
    void Stable_order_when_none_order_specified() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16, "order=none");
        validateFiles(OrderConfiguration.class.getSimpleName() + "-none-order", "html");
    }

    @Test
    void ASC_order() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16, "order=asc");
        validateFiles(OrderConfiguration.class.getSimpleName() + "-asc-order", "html");
    }

    @Test
    void DESC_order() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16, "order=desc");
        validateFiles(OrderConfiguration.class.getSimpleName() + "-desc-order", "html");
    }

    @Test
    void Prepend_required_order() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16, "prepend_required");
        validateFiles(OrderConfiguration.class.getSimpleName() + "-prepend-required", "html");
    }

    @Test
    void Deprecated_last_order() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16, "deprecated_last");
        validateFiles(OrderConfiguration.class.getSimpleName() + "-deprecated-last", "html");
    }

    @Test
    void Deprecated_last_and_prepend_required_order() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16, "deprecated_last", "prepend_required");
        validateFiles(OrderConfiguration.class.getSimpleName() + "-deprecated-last-prepend-required", "html");
    }

    @Test
    void Deprecated_last_and_prepend_required_and_DESC_order() {
        documentFile(OrderConfiguration.class.getSimpleName(), "html", 16, "deprecated_last", "prepend_required", "order=desc");
        validateFiles(OrderConfiguration.class.getSimpleName() + "-deprecated-last-prepend-required-desc-order", "html");
    }

}
