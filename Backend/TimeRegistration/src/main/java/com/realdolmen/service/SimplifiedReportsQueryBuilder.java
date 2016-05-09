package com.realdolmen.service;

import com.realdolmen.annotations.Simplified;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.ResourceBundle;

/**
 * A {@link ReportsQueryBuilder} decorator that supports a simplified query dialect.
 */
@Stateless
@Simplified
public class SimplifiedReportsQueryBuilder extends ReportsQueryBuilder {

    private static final String FIELD_REGEXP = "([a-zA-Z]+-?[a-zA-Z]+|[^a-zA-Z]+)";
    private static final String TOKEN_REGEXP = "(\\w+|[^\\w]+)";

    private ResourceBundle queryBundle;

    @PostConstruct
    private void init() {
        queryBundle = ResourceBundle.getBundle("QueryLanguage");
    }

    @Override
    public ReportsQueryBuilder where(String selection) {
        String translated = getParser().tokenStream(selection, TOKEN_REGEXP)
                .map(this::tryTranslation)
                .reduce(new StringBuilder(""), StringBuilder::append, StringBuilder::append).toString();
        translated = getParser().tokenStream(translated, FIELD_REGEXP)
                .map(this::convertSimplifiedField)
                .reduce(new StringBuilder(""), StringBuilder::append, StringBuilder::append).toString();
        return super.where(translated);
    }

    private String convertSimplifiedField(String field) {
        String[] tokens = field.split("-");
        if (tokens.length > 1) {
            StringBuilder builder = new StringBuilder();
            for (int i = tokens.length - 1; i >= 0; i--) {
                builder.append(tokens[i]).append(".");
            }

            return builder.substring(0, builder.length() - 1);
        }

        return field;
    }

    private String tryTranslation(String fieldName) {
        return getQueryBundle().containsKey(fieldName) ? getQueryBundle().getString(fieldName) : fieldName;
    }

    private ResourceBundle getQueryBundle() {
        return queryBundle;
    }
}
