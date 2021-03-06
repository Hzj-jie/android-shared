package org.gemini.shared;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;
import java.text.DateFormat;
import java.util.Date;

public final class Debugging {
  private static final String TAG = createTag("Debugging");
  private Debugging() {}

  // According to
  // https://developer.android.com/reference/android/util/Log.html#isLoggable(java.lang.String, int)
  // No limitation to the tag after API 24 (or 23?).
  public static String createTag(String component) {
    final int TAG_LEN = 23;
    String result = "Gemini." + component;
    if (result.length() <= TAG_LEN ||
        Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
      return result;
    }
    return result.substring(0, TAG_LEN);
  }

  public static String createTag(Class<?> clazz) {
    return createTag(clazz.getSimpleName());
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

  public static String classLoggingName(Class<?> clazz) {
    if (clazz == null) return "[class] null";
    return "[class] " + clazz.getCanonicalName();
  }

  public static String toString(Exception e) {
    // TODO: Better string representation.
    if (e == null) return "[exception] null";
    return "[exception] " + e.toString();
  }

  public static String toString(IntentFilter f) {
    if (f == null) return "[IntentFilter] null";
    StringBuilder builder = new StringBuilder();
    builder.append("[IntentFilter ")
           .append(f.countActions())
           .append("] ");
    for (int i = 0; i < f.countActions(); i++) {
      builder.append(f.getAction(i))
             .append(" ");
    }
    builder.delete(builder.length() - 1, builder.length());
    return builder.toString();
  }

  public static boolean isDebugBuild(Context context) {
    Preconditions.isNotNull(context);
    return (context.getApplicationInfo().flags &
            ApplicationInfo.FLAG_DEBUGGABLE) != 0;
  }
}
