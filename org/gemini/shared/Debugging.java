package org.gemini.shared;

import android.os.Build;
import android.util.Log;
import java.text.DateFormat;
import java.util.Date;

public final class Debugging {
  private static final String TAG = createTag("Debugging");
  private Debugging() {}

  // According to
  // https://developer.android.com/reference/android/util/Log.html#isLoggable(java.lang.String, int)
  // No limitation to the tag after API 24.
  public static String createTag(String component) {
    final int TAG_LEN = 23;
    String result = "Gemini." + component;
    if (result.length() <= TAG_LEN ||
        Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
      return result;
    }
    return result.substring(0, TAG_LEN);
  }

  public static String stackTrace() {
    return Log.getStackTraceString(new Exception());
  }

  public static void printStackTrace(String tag) {
    Log.i(tag, stackTrace());
  }

  public static void printStackTrace() {
    printStackTrace(TAG);
  }

  public static String currentTime() {
    return DateFormat.getDateTimeInstance().format(new Date());
  }

  private static final class UncaughtExceptionHandlerSetter {
    private UncaughtExceptionHandlerSetter() {}
    public static void set() {}

    static {
      Thread.setDefaultUncaughtExceptionHandler(
          new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
              Log.e(TAG,
                    "Unhandled exception @ thread " + t.getId() +
                    ": " + e.toString(),
                    e);
            }
          });
    }
  }

  public static void catchUnhandledExceptions() {
    UncaughtExceptionHandlerSetter.set();
  }
}
