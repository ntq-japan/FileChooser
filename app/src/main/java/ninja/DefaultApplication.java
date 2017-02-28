package ninja;

import android.app.Application;

import ninja.lbs.filechooser.BuildConfig;
import ninja.lbs.utils.NinjaLog;

/**
 * Default application
 */
public class DefaultApplication extends Application {
    private static final String TAG = DefaultApplication.class.getName();

    private static DefaultApplication mDefaultApplication;

    public static Application getContext() {
        if (BuildConfig.DEBUG) {
            final String METHOD_NAME = "getContext()";
            NinjaLog.enter(TAG, METHOD_NAME);
            NinjaLog.exit(TAG, METHOD_NAME);
        }

        return mDefaultApplication;
    }

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            NinjaLog.enter(TAG, "onCreate()");
        }

        super.onCreate();
        mDefaultApplication = this;

        // Log all build data
        String infoBuilder = "::APPLICATION INFORMATION::\n"
                + "Application debugging: " + BuildConfig.DEBUG + "\n"
                + "Application id: " + BuildConfig.APPLICATION_ID + "\n"
                + "Build type: " + BuildConfig.BUILD_TYPE + "\n"
                + "Vendor: " + BuildConfig.FLAVOR + "\n"
                + "Version code: " + BuildConfig.VERSION_CODE + "\n"
                + "Version name: " + BuildConfig.VERSION_NAME;
        NinjaLog.w(TAG, "onCreate()", infoBuilder);

        if (BuildConfig.DEBUG) {
            NinjaLog.exit(TAG, "onCreate()");
        }
    }
}
