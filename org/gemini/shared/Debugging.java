package org.gemini.shared;

import android.util.Log;
import java.text.DateFormat;
import java.util.Date;

public final class Debugging {
  private static final String TAG = "Gemini.Debugging";
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

  public static String currentTime() {
    return DateFormat.getDateTimeInstance().format(new Date());
  }
}
