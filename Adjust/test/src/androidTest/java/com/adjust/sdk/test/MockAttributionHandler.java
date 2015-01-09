package com.adjust.sdk.test;

import com.adjust.sdk.IAttributionHandler;

import org.json.JSONObject;

/**
 * Created by pfms on 09/01/15.
 */
public class MockAttributionHandler implements IAttributionHandler {
    private MockLogger testLogger;
    private String prefix = "AttributionHandler ";
    private MockActivityHandler mockActivityHandler;

    public MockAttributionHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void getAttribution() {
        testLogger.test(prefix +  "getAttribution");
    }

    @Override
    public void checkAttribution(JSONObject jsonResponse) {
        testLogger.test(prefix +  "checkAttribution");
    }

    public void setMockActivityHandler(MockActivityHandler mockActivityHandler) {
        this.mockActivityHandler = mockActivityHandler;
    }
}
