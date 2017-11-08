package org.gemini.shared;

import android.util.Log;

public final class Preconditions {
  private static final String TAG = Debugging.createTag("Preconditions");
  private Preconditions() {}

  public static boolean isTrue(boolean v, String msg) {
    if (v) {
      return true;
    }

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
}
