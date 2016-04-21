package com.realdolmen.json;

/**
 * Util class for common domain-specific JSON-functions.
 */
public class Json {

    public static String error(String message) {
        return "{\"type\": \"error\", \"message\": \"" + message + "\"}";
    }
}
