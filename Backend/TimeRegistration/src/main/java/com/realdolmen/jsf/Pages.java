package com.realdolmen.jsf;

import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.Project;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class containing constants to all JSF-pages.
 */
@Named
@RequestScoped
public class Pages {

    public static Page index() {
        return new Page("/index.xhtml");
    }

    public static Page login() {
        return new Page("/login.xhtml");
    }

    public static Page searchEmployee() { return new Page("/employees/search-employees.xhtml"); }

    public static Page detailsEmployee() { return new Page("/employees/employee-details.xhtml"); }

    public static Page editEmployee() { return new Page("/employees/employee-edit.xhtml"); }

    public static Page addEmployee() { return new Page("/employees/employee-add.xhtml"); }

    public static Page searchOccupation() { return new Page("/occupations/search-occupations.xhtml"); }

    public static Page addOccupation() { return new Page("/occupations/occupation-add.xhtml"); }

    public static Page detailsOccupation() { return new Page("/occupations/occupation-details.xhtml"); }

    public static Page addProject() { return new Page("/occupations/project-add.xhtml"); }

    public static Page detailsProject() { return new Page("/occupations/project-details.xhtml"); }

    public static Page occupationDetailsFrom(Occupation occupation) {
        if (occupation instanceof Project) {
            return Pages.detailsProject();
        } else {
            return Pages.detailsOccupation();
        }
    }

    public static class Page {

        private String baseUrl;
        private Map<String, String> params = new HashMap<>();
        private static final String URL_ENCODING = "UTF-8";

        private Page(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Page param(String key, String value) {
            params.put(key, value);
            return this;
        }

        public String jsf() {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("."));
            return noRedirect();
        }

        public String noRedirect() {
            StringBuilder builder = new StringBuilder(baseUrl);
            if (!params.isEmpty()) {
                builder.append("?");
                params.forEach((k, v) -> {
                    try {
                        builder.append(k).append("=").append(URLEncoder.encode(v, URL_ENCODING)).append("&");
                    } catch (UnsupportedEncodingException e) {
                    }
                });

                return builder.substring(0, builder.length() - 1);
            }

            return builder.toString();
        }

        public String redirect() {
            StringBuilder builder = new StringBuilder(noRedirect());
            builder.append(params.isEmpty() ? "?" : "&").append("faces-redirect=true");
            return builder.toString();
        }
    }

}
