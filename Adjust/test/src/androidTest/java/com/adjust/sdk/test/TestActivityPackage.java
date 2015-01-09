package com.adjust.sdk.test;

import com.adjust.sdk.ActivityKind;
import com.adjust.sdk.ActivityPackage;

import java.util.Map;

import junit.framework.Assert;

/**
 * Created by pfms on 09/01/15.
 */
public class TestActivityPackage {

    private ActivityPackage activityPackage;
    private Map<String, String> parameters;
    public String appToken;
    public String environment;
    public String clientSdk;
    public Boolean deviceKnow;
    public Boolean needsAttributionData;
    public Integer session_count;
    public boolean playServices;

    public TestActivityPackage (ActivityPackage activityPackage) {
        this.activityPackage = activityPackage;
        parameters = activityPackage.getParameters();

        // default values
        appToken = "123456789012";
        environment = "sandbox";
        clientSdk = "android4.0.0";
        deviceKnow = null;
        needsAttributionData = false;
        session_count = null;
        playServices = false;
    }

    public void testSessionPackage(int session_count) {
        // check the Sdk version is being tested
        assertEquals(activityPackage.getClientSdk(), clientSdk);
        // check the path
        assertEquals(activityPackage.getPath(), "/session");
        // check the activity kind
        assertEquals(activityPackage.getActivityKind(), ActivityKind.SESSION);
        // check suffix
        assertEquals(activityPackage.getSuffix(), "");
        // set the session count
        this.session_count = session_count;

        testDefaultParameters();
    }

    public void testDefaultParameters() {
        testDeviceInfo();
        testConfig();
        testActivityState();
    }

    public void testDeviceInfo() {
        // play services
        if (playServices) {
            // mac_sha1
            assertParameterNull("mac_sha1");
            // mac_md5
            assertParameterNull("mac_md5");
        } else {
            // mac_sha1
            assertParameterNotNull("mac_sha1");
            // mac_md5
            assertParameterNotNull("mac_md5");
        }
        // android_id
        assertParameterNotNull("android_id");
        // fb_id
        assertParameterNotNull("fb_id");
        // package_name
        assertParameterNotNull("package_name");
        // app_version
        // device_type
        assertParameterNotNull("device_type");
        // device_name
        assertParameterNotNull("device_name");
        // device_manufacturer
        assertParameterNotNull("device_manufacturer");
        // os_name
        assertParameterEquals("os_name", "android");
        // os_version
        assertParameterNotNull("os_version");
        // language
        assertParameterNotNull("language");
        // country
        assertParameterNotNull("country");
        // screen_size
        assertParameterNotNull("screen_size");
        // screen_format
        assertParameterNotNull("screen_format");
        // screen_density
        assertParameterNotNull("screen_density");
        // display_width
        assertParameterNotNull("display_width");
        // display_height
        assertParameterNotNull("display_height");
    }

    public void testConfig() {
        // app_token
        assertParameterEquals("app_token", appToken);
        // environment
        assertParameterEquals("environment", environment);
        // device_known
        testParameterBoolean("device_known", deviceKnow);
        // needs_attribution_data
        testParameterBoolean("needs_attribution_data", needsAttributionData);
        // play services
        if (playServices) {
            // gps_adid
            assertParameterNotNull("gps_adid");
            // tracking_enabled
            assertParameterNotNull("tracking_enabled");
        } else {
            // gps_adid
            assertParameterNull("gps_adid");
            // tracking_enabled
            assertParameterNull("tracking_enabled");
        }
    }

    public void testActivityState() {
        // created_at
        assertParameterNotNull("created_at");
        // android_uuid
        assertParameterNotNull("android_uuid");
        // session_count
        assertParameterEquals("session_count", session_count);
        // first session
        if (session_count == 1) {
            // subsession_count
            assertParameterNull("subsession_count");
            // session_length
            assertParameterNull("session_length");
            // time_spent
            assertParameterNull("time_spent");
        } else {
            // subsession_count
            assertParameterNotNull("subsession_count");
            // session_length
            assertParameterNotNull("session_length");
            // time_spent
            assertParameterNotNull("time_spent");
        }
    }

    private void assertParameterNotNull(String parameterName) {
        Assert.assertNotNull(activityPackage.getExtendedString(),
                parameters.get(parameterName));
    }

    private void assertParameterNull(String parameterName) {
        Assert.assertNull(activityPackage.getExtendedString(),
                parameters.get(parameterName));
    }

    private void assertParameterEquals(String parameterName, String value) {
        Assert.assertEquals(activityPackage.getExtendedString(),
                value, parameters.get(parameterName));
    }

    private void assertParameterEquals(String parameterName, int value) {
        Assert.assertEquals(activityPackage.getExtendedString(),
                value, Integer.parseInt(parameters.get(parameterName)));
    }


    private void assertEquals(String field, String value) {
        Assert.assertEquals(activityPackage.getExtendedString(),
                value, field);
    }

    private void assertEquals(Object field, Object value) {
        Assert.assertEquals(activityPackage.getExtendedString(),
                value, field);
    }

    private void testParameterBoolean(String parameterName, Boolean value) {
        if (value == null) {
            assertParameterNull(parameterName);
        } else if (value) {
            assertParameterEquals(parameterName, "1");
        } else {
            assertParameterEquals(parameterName, "0");
        }
    }
}
