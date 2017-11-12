package org.gemini.shared;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import java.util.concurrent.TimeUnit;

public class KeepAliveService extends Service {
  public static final String RESTART = "org.gemini.shared.intent.RESTART";
  private static final String TAG = Debugging.createTag("KeepAliveService");
  private static final String QUICK_POWER_ON =
      "android.intent.action.QUICKBOOT_POWERON";
  private final int alarmIntervalMs;
  private int startId;
  private int commandCount = 0;
  private int stopAt = commandCount;

  public KeepAliveService() {
    this(60000);
  }

  public KeepAliveService(int alarmIntervalMs) {
    this.alarmIntervalMs = alarmIntervalMs;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    Debugging.printStackTrace();
    restart();
    super.onDestroy();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    Debugging.printStackTrace();
    restart();
    super.onTaskRemoved(rootIntent);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onStart(Intent intent, int startId) {
    onStartCommand(intent, 0, startId);
  }

  @Override
  public final int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "Receive command " +
               (intent == null ? "[null intent]" : intent.getAction()));
    this.startId = startId;
    if (commandCount == 0) {
      if (intent == null) {
        onSystemRestart();
        onRestart();
      } else if (RESTART.equals(intent.getAction())) {
        onKeepAliveRestart();
        onRestart();
      } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                 QUICK_POWER_ON.equals(intent.getAction())) {
        onBootCompleted();
      }
    } else if (RESTART.equals(intent.getAction())) {
      keepalive();
      return START_NOT_STICKY;
    }
    process(intent);
    commandCount++;
    // First intent after stopSticky().
    if (stopAt == commandCount - 1) {
      keepalive();
      return START_STICKY;
    }
    return START_NOT_STICKY;
  }
  
  protected final int startId() {
    return startId;
  }

  protected final int commandCount() {
    return commandCount;
  }

  protected final void stopSticky() {
    stopAt = commandCount + 1;
    stopSelf(startId);
  }

  private final void scheduleRestart(int intervalMs) {
    if (stopAt < commandCount) {
      Intent intent = new Intent(
          RESTART, Uri.EMPTY, this, getClass());
      intent.setPackage(getPackageName());
      PendingIntent pendingIntent = PendingIntent.getService(
          this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
      AlarmManager alarmManager =
          (AlarmManager) getSystemService(Context.ALARM_SERVICE);
      alarmManager.set(AlarmManager.RTC,
                       System.currentTimeMillis() + intervalMs,
                       pendingIntent);
    }
  }

  private final void restart() {
    scheduleRestart(1000);
  }

  private final void keepalive() {
    if (alarmIntervalMs > 0) {
      scheduleRestart(alarmIntervalMs);
    }
  }

  protected void process(Intent intent) {
    process(intent == null ? null : intent.getAction());
  }

  protected void process(String action) {
    process();
  }

  protected void process() {}

  protected void onBootCompleted() {}
  protected void onRestart() {}
  protected void onSystemRestart() {}
  protected void onKeepAliveRestart() {}
}
