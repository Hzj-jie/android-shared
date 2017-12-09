package org.gemini.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

public final class BootCompletedListener extends IntentListener {
  private static final String QUICKBOOT_POWERON =
      "android.intent.action.QUICKBOOT_POWERON";
  private static final String HTC_QUICKBOOT_POWERON =
      "com.htc.intent.action.QUICKBOOT_POWERON";
  private static final String TAG =
      Debugging.createTag("BootCompletedListener");
  private final Event.Raisable<Void> onBootCompleted;
  private final Class<?> serviceClass;

  public static boolean isBootCompleted(String action) {
    return Intent.ACTION_BOOT_COMPLETED.equals(action) ||
           QUICKBOOT_POWERON.equals(action) ||
           HTC_QUICKBOOT_POWERON.equals(action);
  }

  public BootCompletedListener(Context context, Class<?> serviceClass) {
    super(context);
    this.serviceClass = serviceClass;
    onBootCompleted = new Event.Raisable<>();
  }

  public Event<Void> onBootCompleted() {
    return onBootCompleted;
  }

  protected String[] actions() {
    return new String[] {
      Intent.ACTION_BOOT_COMPLETED,
      QUICKBOOT_POWERON,
      HTC_QUICKBOOT_POWERON,
    };
  }

  protected void raise(Context context, Intent intent) {
    onBootCompleted.raise(null);
    context.startService(new Intent(Intent.ACTION_BOOT_COMPLETED,
                                    Uri.EMPTY,
                                    context,
                                    serviceClass));
  }
}
