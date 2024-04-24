package cz.lukaskabc.cvut.processor.docsgenerator;

import freemarker.core.CommonTemplateMarkupOutputModel;

public class TemplateMDOutputModel extends CommonTemplateMarkupOutputModel<TemplateMDOutputModel> {

    protected TemplateMDOutputModel(String plainTextContent, String markupContent) {
        super(plainTextContent, markupContent);
    }

    @Override
    public MDTemplateFormat getOutputFormat() {
        return MDTemplateFormat.INSTANCE;
    }


}
