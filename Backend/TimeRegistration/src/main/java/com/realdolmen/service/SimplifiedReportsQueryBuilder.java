package com.realdolmen.service;

import com.realdolmen.annotations.Simplified;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A {@link ReportsQueryBuilder} decorator that supports a simplified query dialect.
 */
@Stateless
@Simplified
public class SimplifiedReportsQueryBuilder extends ReportsQueryBuilder {

    private static final String TOKEN_REGEXP = "([a-z\\-A-Z]+(?= ?[<=!>]{1,2})|[=!<>]{1,2} ?\"?[0-9\\- a-zA-Z ]+\"?.?|\\|)";

    private ResourceBundle queryBundle;

    @PostConstruct
    private void init() {
        queryBundle = ResourceBundle.getBundle("QueryLanguage");
    }

    @Override
    public ReportsQueryBuilder where(String selection) {
        String translated = selection != null && selection.matches("^[a-zA-Z ]+$") ?
                trySpecialTranslation(selection) :
                getParser().tokenStream(selection, TOKEN_REGEXP)
                        .map(this::tryTranslation)
                        .reduce(new StringBuilder(""), StringBuilder::append, StringBuilder::append).toString();
        return super.where(translated);
    }

    private String convertSimplifiedField(String field) {
        if (!field.matches("[a-zA-Z\\-]+")) {
            return field;
        }

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
        fieldName = fieldName.toLowerCase();
        return getQueryBundle().containsKey(fieldName.trim()) ? getQueryBundle().getString(fieldName.trim()) : fieldName;
    }

    private String trySpecialTranslation(String fieldName) {
        fieldName = fieldName.toLowerCase();
        Map<String, String> replacements = new HashMap<>();
        replacements.put("today", DateTime.now().toString(DateTimeFormat.forPattern("dd-MM-yyyy")));
        replacements.put("month_start", DateTime.now().withDayOfMonth(1).toString(DateTimeFormat.forPattern("dd-MM-yyyy")));
        replacements.put("month_end", DateTime.now().plusMonths(1).withDayOfMonth(1).minusDays(1).toString(DateTimeFormat.forPattern("dd-MM-yyyy")));
        replacements.put("prev_month_start", DateTime.now().withDayOfMonth(1).minusMonths(1).toString(DateTimeFormat.forPattern("dd-MM-yyyy")));
        replacements.put("prev_month_end", DateTime.now().plusMonths(1).withDayOfMonth(1).minusDays(1).minusMonths(1).toString(DateTimeFormat.forPattern("dd-MM-yyyy")));

        String value = getQueryBundle().containsKey(fieldName.trim()) ? getQueryBundle().getString(fieldName.trim()) : fieldName;

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return value;
    }

    private ResourceBundle getQueryBundle() {
        return queryBundle;
    }
}
