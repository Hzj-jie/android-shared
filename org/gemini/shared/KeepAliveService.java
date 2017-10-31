package org.gemini.shared;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class KeepAliveService extends Service {
  public static final String KEEP_ALIVE = "org.gemini.shared.intent.KEEP_ALIVE";

  @Override
  public void onDestroy() {
    restart();
    super.onDestroy();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    restart();
    super.onTaskRemoved(rootIntent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent !=null && intent.getAction().equals(KEEP_ALIVE)) {
      return START_STICKY;
    }
    return discardServiceRequest(startId);
  }

  protected int discardServiceRequest(int startId) {
    stopSelf(startId);
    return START_NOT_STICKY;
  }

  private void restart(Class<?> cls) {
    Intent intent = new Intent(getApplicationContext(), cls);
    intent.setActio(KEEP_ALIVE);
    PendingIntent pendingIntent =
        PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
    AlarmManager alarmManager =
        (AlarmManager) getSystemService(Context.ALARM_MANAGER);
    alarmManager.set(AlarmManager.RTC_WAKEUP,
                     SystemClock.elapsedRealtime() + 1000,
                     pendingIntent);
  }
}
