package org.gemini.shared;

import android.os.Build;
import android.util.Log;

public final class Preconditions {
  private static final String TAG = Debugging.createTag("Preconditions");
  private Preconditions() {}

  public static boolean isTrue(boolean v, String msg) {
    if (v) {
      return true;
    }

    Log.e(TAG, "Assertion failed: " + msg);
    throw new AssertionError("Assertion failed: " + msg);
  }

  public static boolean isTrue(boolean v) {
    return isTrue(v, null);
  }

  public static boolean isNull(Object v, String msg) {
    return isTrue(v == null, msg);
  }

  public static boolean isNull(Object v) {
    return isTrue(v == null);
  }

  public static boolean isNotNull(Object v, String msg) {
    return isTrue(v != null, msg);
  }

  public static boolean isNotNull(Object v) {
    return isTrue(v != null);
  }

  public static boolean isOsAtLeast(int version, String msg) {
    return isTrue(Build.VERSION.SDK_INT >= version, msg);
  }

  public static boolean isOsAtLeast(int version) {
    return isTrue(Build.VERSION.SDK_INT >= version);
  }

  public static boolean notReached(String msg) {
    return isTrue(false, msg);
  }

  public static boolean notReached() {
    return isTrue(false);
  }

  public static int notReachedI(String msg) {
    return isTrue(false, msg) ? 0 : 0;
  }

  public static int notReachedI() {
    return isTrue(false) ? 0 : 0;
  }
}
