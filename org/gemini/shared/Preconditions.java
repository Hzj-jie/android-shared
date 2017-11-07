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
}
