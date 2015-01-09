package com.adjust.sdk.test;

import static com.adjust.sdk.Logger.LogLevel.*;

import junit.framework.Assert;

/**
 * Created by pfms on 09/01/15.
 */
public class AssertUtil {
    private MockLogger mockLogger;

    public AssertUtil(MockLogger mockLogger) {
        this.mockLogger = mockLogger;
    }

    public void test(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsTestMessage(message));
    }

    public void verbose(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(VERBOSE, message));
    }

    public void debug(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(DEBUG, message));
    }

    public void info(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(INFO, message));
    }

    public void warn(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(WARN, message));
    }

    public void error(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(ERROR, message));
    }

    public void Assert(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(ASSERT, message));
    }

}
