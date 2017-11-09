package org.gemini.shared;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class KeepAliveService extends Service {
  public static final String RESTART = "org.gemini.shared.intent.RESTART";
  private static final String TAG = Debugging.createTag("KeepAliveService");
  private static final String QUICK_POWER_ON =
      "android.intent.action.QUICKBOOT_POWERON";
  private final int alarmIntervalMs;
  private int commandCount = 0;
  private boolean stopped = false;

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
      keepalive();
    } else if (RESTART.equals(intent.getAction())) {
      keepalive();
      return START_NOT_STICKY;
    }
    process(intent, commandCount);
    commandCount++;
    if (commandCount == 1) {
      return START_STICKY;
    }
    return START_NOT_STICKY;
  }
  
  protected final void stopSticky() {
    stopped = true;
    stopSelf();
  }

  private final void scheduleRestart(int intervalMs) {
    if (!stopped) {
      Intent intent = new Intent(getApplicationContext(), this.getClass());
      intent.setAction(RESTART);
      PendingIntent pendingIntent = PendingIntent.getService(
          this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
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

  protected void process(Intent intent, int index) {
    process(intent == null ? null : intent.getAction(), index);
  }

  protected void process(String action, int index) {
    process(action);
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
