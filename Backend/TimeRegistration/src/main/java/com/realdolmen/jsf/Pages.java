package com.realdolmen.jsf;

import org.primefaces.util.HTML;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class containing constants to all JSF-pages.
 */
public class Pages {

    public static Page index() {
        return new Page("index.xhtml");
    }

    public static Page login() {
        return new Page("login.xhtml");
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
