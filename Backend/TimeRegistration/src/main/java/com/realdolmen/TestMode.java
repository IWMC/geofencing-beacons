package com.realdolmen;

import org.jetbrains.annotations.TestOnly;

/**
 * Global class indicating whether the application is runned in test mode.
 */
public class TestMode {

    private static boolean testMode;

    public static boolean isTestMode() {
        return testMode;
    }

    @TestOnly
    public static void enableTestMode() {
        TestMode.testMode = true;
    }
}
