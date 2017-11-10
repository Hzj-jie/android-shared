package org.gemini.shared;

import android.os.Handler;
import android.os.Looper;

public final class ThisThread {
  private ThisThread() {}

  public static boolean postable() {
    return Looper.myLooper() != null;
  }

  public static void post(Runnable r) {
    Preconditions.isTrue(postable());
    Preconditions.isNotNull(r);
    (new Handler()).post(r);
  }

  public static void post(Runnable r, long delayMs) {
    Preconditions.isTrue(postable());
    Preconditions.isNotNull(r);
    Preconditions.isTrue(delayMs > 0);
    (new Handler()).postDelayed(r, delayMs);
  }
}
