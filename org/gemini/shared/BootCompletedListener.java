package org.gemini.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

public final class BootCompletedListener {
  private static final String QUICKBOOT_POWERON =
      "android.intent.action.QUICKBOOT_POWERON";
  private static final String HTC_QUICKBOOT_POWERON =
      "com.htc.intent.action.QUICKBOOT_POWERON";
  private static final String TAG =
      Debugging.createTag("BootCompletedListener");
  private final Context context;
  private final BroadcastReceiver listener;
  private final Event.Raisable<Void> onBootCompleted;
  private final Class<?> serviceClass;

  public static boolean isBootCompleted(String action) {
    return Intent.ACTION_BOOT_COMPLETED.equals(action) ||
           QUICKBOOT_POWERON.equals(action) ||
           HTC_QUICKBOOT_POWERON.equals(action);
  }

  public BootCompletedListener(Context context, Class<?> serviceClass) {
    Preconditions.isNotNull(context);
    Preconditions.isNotNull(serviceClass);
    this.context = context;
    listener = newListener();
    onBootCompleted = new Event.Raisable<>();
    this.serviceClass = serviceClass;
    start();
  }

  public Event<Void> onBootCompleted() {
    return onBootCompleted;
  }

  public void stop() {
    context.unregisterReceiver(listener);
  }

  private void start() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_BOOT_COMPLETED);
    filter.addAction(QUICKBOOT_POWERON);
    filter.addAction(HTC_QUICKBOOT_POWERON);
    context.registerReceiver(listener, filter);
  }

  private BroadcastReceiver newListener() {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        Log.i(TAG, "Receive action " + intent.getAction());
        onBootCompleted.raise(null);
        context.startService(new Intent(Intent.ACTION_BOOT_COMPLETED,
                                        Uri.EMPTY,
                                        context,
                                        serviceClass));
      }
    };
  }
}
