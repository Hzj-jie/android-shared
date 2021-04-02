package org.gemini.shared;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

public final class Notifier {
  private static final String TAG = Debugging.createTag("Notifier");
  private static int notificationId = 0;
  private final Context context;

  public static final class Configuration {
    public int icon = 0;
    public CharSequence text = "";
    public long whenMs = System.currentTimeMillis();

    public static Configuration New() {
      return new Configuration();
    }

    public Configuration withIcon(int icon) {
      this.icon = icon;
      return this;
    }

    public Configuration withText(CharSequence text) {
      this.text = text;
      return this;
    }

    public Configuration withWhenMs(long whenMs) {
      this.whenMs = whenMs;
      return this;
    }

    @Override
    public String toString() {
      return "icon: " + icon + ", text: " + text + ", whenMs: " + whenMs;
    }
  }

  public Notifier(Context context) {
    Preconditions.isNotNull(context);
    this.context = context;
  }

  // Starts a notification and returns its id. < 0 return value indicates an
  // error. Otherwise, clients can use the return value to cancel the
  // notification.
  @SuppressWarnings("deprecation")
  public int notify(Configuration config) {
    if (config == null) return -1;
    int id = notificationId;
    notificationId++;
    Notification notification =
        new Notification(config.icon, config.text, config.whenMs);
    Log.w(TAG, config + ": " + notification + "@" + id);
    manager().notify(id, notification);
    return id;
  }

  public static int notify(Context context, Configuration config) {
    return (new Notifier(context)).notify(config);
  }

  public void cancel(int id) {
    manager().cancel(id);
  }

  public static void cancel(Context context, int id) {
    (new Notifier(context)).cancel(id);
  }

  public NotificationManager manager() {
    return (NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);
  }
}
