package org.gemini.shared;

import android.util.Log;

public final class Debugging {
  private static final String TAG = "[Debugging]";
  private Debugging() {}

  public static String stackTrace() {
    return Log.getStackTraceString(new Exception());
  }

  public static void printStackTrace(String tag) {
    Log.d(tag, stackTrace());
  }

  public static void printStackTrace() {
    printStackTrace(TAG);
  }
}
