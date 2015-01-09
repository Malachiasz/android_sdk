//
//  PackageBuilder.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class PackageBuilder {
    private AdjustConfig adjustConfig;
    private DeviceInfo deviceInfo;
    private ActivityState activityState;

    // reattributions
    Map<String, String> deepLinkParameters;

    public PackageBuilder(AdjustConfig adjustConfig, DeviceInfo deviceInfo, ActivityState activityState) {
        this.adjustConfig = adjustConfig;
        this.deviceInfo = deviceInfo;
        this.activityState = activityState.clone();
    }

    public ActivityPackage buildSessionPackage() {
        Map<String, String> parameters = getDefaultParameters();
        addDuration(parameters, "last_interval", activityState.lastInterval);
        addString(parameters, "default_tracker", adjustConfig.defaultTracker);

        ActivityPackage sessionPackage = getDefaultActivityPackage();
        sessionPackage.setPath("/session");
        sessionPackage.setActivityKind(ActivityKind.SESSION);
        sessionPackage.setSuffix("");
        sessionPackage.setParameters(parameters);

        return sessionPackage;
    }

    public ActivityPackage buildEventPackage(Event event) {
        Map<String, String> parameters = getDefaultParameters();
        addInt(parameters, "event_count", activityState.eventCount);
        addString(parameters, "event_token", event.eventToken);
        addDouble(parameters, "revenue", event.revenue);
        addString(parameters, "currency", event.currency);
        addMapBase64(parameters, "callback_params", event.callbackParameters);
        addMapBase64(parameters, "partner_params", event.partnerParameters);

        ActivityPackage eventPackage = getDefaultActivityPackage();
        eventPackage.setPath("/event");
        eventPackage.setActivityKind(ActivityKind.EVENT);
        eventPackage.setSuffix(getEventSuffix(event));
        eventPackage.setParameters(parameters);

        return eventPackage;
    }

    public ActivityPackage buildClickPackage(String source) {
        Map<String, String> parameters = getDefaultParameters();
        addString(parameters, "source", source);

        addString(parameters, "referrer", adjustConfig.referrer);
        addMapJson(parameters, "params", deepLinkParameters);

        ActivityPackage clickPackage = getDefaultActivityPackage();
        clickPackage.setPath("/sdk_click");
        clickPackage.setActivityKind(ActivityKind.CLICK);
        clickPackage.setSuffix("");
        clickPackage.setParameters(parameters);

        return clickPackage;
    }

    public ActivityPackage buildAttributionPackage() {
        Map<String, String> parameters = new HashMap<String, String>();

        // device info fields
        addString(parameters, "mac_sha1", deviceInfo.macSha1);
        addString(parameters, "mac_md5", deviceInfo.macShortMd5);
        addString(parameters, "android_id", deviceInfo.androidId);
        // config fields
        addString(parameters, "app_token", adjustConfig.appToken);
        addString(parameters, "environment", adjustConfig.environment);
        addBoolean(parameters, "needs_attribution_data", adjustConfig.hasDelegate());
        String playAdId = Util.getPlayAdId(adjustConfig.context);
        addString(parameters, "gps_adid", playAdId);
        // activity state fields
        addString(parameters, "android_uuid", activityState.uuid);

        ActivityPackage attributionPackage = getDefaultActivityPackage();
        attributionPackage.setPath("attribution"); // does not contain '/' because of Uri.Builder.appendPath
        attributionPackage.setParameters(parameters);

        return attributionPackage;
    }

    private ActivityPackage getDefaultActivityPackage() {
        ActivityPackage activityPackage = new ActivityPackage();
        activityPackage.setClientSdk(deviceInfo.clientSdk);
        return activityPackage;
    }

    private Map<String, String> getDefaultParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        injectDeviceInfo(parameters);
        injectConfig(parameters);
        injectActivityState(parameters);

        // general
        checkDeviceIds(parameters);

        return parameters;
    }

    private void injectDeviceInfo(Map<String, String> parameters) {
        addString(parameters, "mac_sha1", deviceInfo.macSha1);
        addString(parameters, "mac_md5", deviceInfo.macShortMd5);
        addString(parameters, "android_id", deviceInfo.androidId);
        addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        addString(parameters, "package_name", deviceInfo.packageName);
        addString(parameters, "app_version", deviceInfo.appVersion);
        addString(parameters, "device_type", deviceInfo.deviceType);
        addString(parameters, "device_name", deviceInfo.deviceName);
        addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        addString(parameters, "os_name", deviceInfo.osName);
        addString(parameters, "os_version", deviceInfo.osVersion);
        addString(parameters, "language", deviceInfo.language);
        addString(parameters, "country", deviceInfo.country);
        addString(parameters, "screen_size", deviceInfo.screenSize);
        addString(parameters, "screen_format", deviceInfo.screenFormat);
        addString(parameters, "screen_density", deviceInfo.screenDensity);
        addString(parameters, "display_width", deviceInfo.displayWidth);
        addString(parameters, "display_height", deviceInfo.displayHeight);
        fillPluginKeys(parameters);
    }

    private void injectConfig(Map<String, String> parameters) {
        addString(parameters, "app_token", adjustConfig.appToken);
        addString(parameters, "environment", adjustConfig.environment);
        addBoolean(parameters, "device_known", adjustConfig.knowDevice);
        addBoolean(parameters, "needs_attribution_data", adjustConfig.hasDelegate());

        String playAdId = Util.getPlayAdId(adjustConfig.context);
        addString(parameters, "gps_adid", playAdId);
        Boolean isTrackingEnabled = Util.isPlayTrackingEnabled(adjustConfig.context);
        addBoolean(parameters, "tracking_enabled", isTrackingEnabled);
    }

    private void injectActivityState(Map<String, String> parameters) {
        addDate(parameters, "created_at", activityState.createdAt);
        addString(parameters, "android_uuid", activityState.uuid);
        addInt(parameters, "session_count", activityState.sessionCount);
        addInt(parameters, "subsession_count", activityState.subsessionCount);
        addDuration(parameters, "session_length", activityState.sessionLength);
        addDuration(parameters, "time_spent", activityState.timeSpent);
    }

    private void checkDeviceIds(Map<String, String> parameters) {
        if (!parameters.containsKey("mac_sha1")
                && !parameters.containsKey("mac_md5")
                && !parameters.containsKey("android_id")
                && !parameters.containsKey("gps_adid")) {
            Logger logger = AdjustFactory.getLogger();
            logger.error("Missing device id's. Please check if Proguard is correctly set with Adjust SDK");
        }
    }

    private void fillPluginKeys(Map<String, String> parameters) {
        if (deviceInfo.pluginKeys == null) {
            return;
        }

        for (Map.Entry<String, String> pluginEntry : deviceInfo.pluginKeys.entrySet()) {
            addString(parameters, pluginEntry.getKey(), pluginEntry.getValue());
        }
    }

    private String getEventSuffix(Event event) {
        if (event.revenue == null) {
            return String.format(" '%s'", event.eventToken);
        } else {
            return String.format(Locale.US, " (%.4f cent, '%s')", event.revenue, event.eventToken);
        }
    }

    private void addString(Map<String, String> parameters, String key, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }

        parameters.put(key, value);
    }

    private void addInt(Map<String, String> parameters, String key, long value) {
        if (value < 0) {
            return;
        }

        String valueString = Long.toString(value);
        addString(parameters, key, valueString);
    }

    private void addDate(Map<String, String> parameters, String key, long value) {
        if (value < 0) {
            return;
        }

        String dateString = Util.dateFormat(value);
        addString(parameters, key, dateString);
    }

    private void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        if (durationInMilliSeconds < 0) {
            return;
        }

        long durationInSeconds = (durationInMilliSeconds + 500) / 1000;
        addInt(parameters, key, durationInSeconds);
    }

    private void addMapBase64(Map<String, String> parameters, String key, Map<String, String> map) {
        if (null == map) {
            return;
        }

        JSONObject jsonObject = new JSONObject(map);
        byte[] jsonBytes = jsonObject.toString().getBytes();
        String encodedMap = Base64.encodeToString(jsonBytes, Base64.NO_WRAP);

        addString(parameters, key, encodedMap);
    }

    private void addMapJson(Map<String, String> parameters, String key, Map<String, String> map) {
        if (null == map) {
            return;
        }

        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();

        addString(parameters, key, jsonString);
    }

    private void addBoolean(Map<String, String> parameters, String key, Boolean value) {
        if (value == null) {
            return;
        }

        int intValue = value ? 1 : 0;

        addInt(parameters, key, intValue);
    }

    private void addDouble(Map<String, String> parameters, String key, Double value) {
        if (value == null) return;

        String doubleString = value.toString();

        addString(parameters, key, doubleString);
    }
}
