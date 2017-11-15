package org.gemini.shared.Notifier;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;

public final class Notifier {
  private Notifier() {}

  private static int notificationId = 0;

  public static final class Configuration {
    public int icon = 0;
    public CharSequence text = "";
    public long whenMs = System.currentTimeMillis();
  }

  // Starts a notification and returns its id. < 0 return value indicates an
  // error. Otherwise, clients can use the return value to cancel the
  // notification.
  public static int notify(Configuration config) {
    return -1;
  }

  public static void cancel(int id) {
  }
}
