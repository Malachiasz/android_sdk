package com.adjust.sdk.test;

import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Attribution;
import com.adjust.sdk.Constants;
import com.adjust.sdk.OnFinishedListener;

import static com.adjust.sdk.Logger.LogLevel.*;


public class TestActivityHandler extends ActivityInstrumentationTestCase2<UnitTestActivity> {
    protected MockLogger mockLogger;
    protected MockPackageHandler mockPackageHandler;
    protected MockAttributionHandler mockAttributionHandler;
    protected UnitTestActivity activity;
    protected Context context;
    protected AssertUtil assertUtil;

    public TestActivityHandler(){
        super(UnitTestActivity.class);
    }

    public TestActivityHandler(Class<UnitTestActivity> mainActivity){
        super(mainActivity);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLogger = new MockLogger();
        mockPackageHandler = new MockPackageHandler(mockLogger);
        mockAttributionHandler = new MockAttributionHandler(mockLogger);
        assertUtil = new AssertUtil(mockLogger);

        AdjustFactory.setLogger(mockLogger);
        AdjustFactory.setPackageHandler(mockPackageHandler);
        AdjustFactory.setAttributionHandler(mockAttributionHandler);

        activity = getActivity();
        context = activity.getApplicationContext();

        // deleting the activity state file to simulate a first session
        mockLogger.test("Was AdjustActivityState deleted? " + ActivityHandler.deleteActivityState(context));

        // deleting the attribution file to simulate a first session
        mockLogger.test("Was Attribution deleted? " + ActivityHandler.deleteAttribution(context));

        // check the server url
        assertEquals(Constants.BASE_URL, "https://app.adjust.com");
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();

        AdjustFactory.setPackageHandler(null);
        AdjustFactory.setAttributionHandler(null);
        AdjustFactory.setLogger(null);
        AdjustFactory.setTimerInterval(-1);
        AdjustFactory.setSessionInterval(-1);
        AdjustFactory.setSubsessionInterval(-1);

        activity = null;
        context = null;
    }

    private void checkLaunch(String logLevel, String environment) {
        if (environment == "sandbox") {
            // check environment level
            assertUtil.Assert("SANDBOX: Adjust is running in Sandbox mode. Use this setting for testing. Don't forget to set the environment to `production` before publishing!");
        }

        // check log level
        assertUtil.test("MockLogger setLogLevel: " + logLevel);

        // check Google play is not set
        assertUtil.info("Unable to get Google Play Services Advertising ID at start time");

        //  test that the attribution file did not exist in the first run of the application
        assertUtil.verbose("Attribution file not found");

        //  test that the activity state file did not exist in the first run of the application
        assertUtil.verbose("Activity state file not found");
    }

    public void testFirstSession() {
        // assert test name to read better in logcat
        mockLogger.Assert("TestActivityHandler testFirstSession");
        // create the config to start the session
        AdjustConfig config = AdjustConfig.getInstance(context,"123456789012", AdjustConfig.SANDBOX_ENVIRONMENT);

        //  set the delegate that to be called at after sending the package
        config.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onFinishedTracking(Attribution attribution) {
                mockLogger.test("onFinishedTracking, " + attribution);
            }
        });

        //  create handler and start the first session
        ActivityHandler activityHandler = new ActivityHandler(config);

        // it's necessary to sleep the activity for a while after each handler call
        // to let the internal queue act
        SystemClock.sleep(2000);

        // default log level and sandbox environment
        checkLaunch("INFO", AdjustConfig.SANDBOX_ENVIRONMENT);

        // when a session package is being sent the package handler should resume sending
        assertUtil.test("PackageHandler resumeSending");

        // if the package was build, it was sent to the Package Handler
        assertUtil.test("PackageHandler addPackage");

        // after adding, the activity handler ping the Package handler to send the package
        assertUtil.test("PackageHandler sendFirstPackage");

        // checking the default values of the first session package
        // should only have one package
        assertEquals(1, mockPackageHandler.queue.size());

        ActivityPackage activityPackage = mockPackageHandler.queue.get(0);

        // create activity package test
        TestActivityPackage testActivityPackage = new TestActivityPackage(activityPackage);

        // set delegate check test
        testActivityPackage.needsAttributionData = true;

        // set first session
        testActivityPackage.testSessionPackage(1);
    }
}
