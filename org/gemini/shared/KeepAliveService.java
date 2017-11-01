package org.gemini.shared;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import java.util.ArrayList;
import java.util.List;

public class KeepAliveService extends Service {
  public static final String KEEP_ALIVE = "org.gemini.shared.intent.KEEP_ALIVE";
  private final List<Integer> stickies;

  public KeepAliveService() {
    stickies = new ArrayList<>();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

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

  @SuppressWarnings("deprecation")
  @Override
  public void onStart(Intent intent, int startId) {
    onStartCommand(intent, 0, startId);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent !=null &&
        intent.getAction().equals(KEEP_ALIVE) &&
        stickies.isEmpty()) {
      keepAlive(stickies.size());
      return sticky(startId);
    }
    return discardCommand(startId);
  }

  protected void keepAlive(int index) {}

  protected final int discardCommand(int startId) {
    stopSelf(startId);
    return START_NOT_STICKY;
  }

  protected final int sticky(int startId) {
    stickies.add(startId);
    return START_STICKY;
  }

  protected final void stopStickies() {
    for (Integer i : stickies) {
      stopSelf(i);
    }
  }

  private final void restart() {
    Intent intent = new Intent(getApplicationContext(), this.getClass());
    intent.setAction(KEEP_ALIVE);
    PendingIntent pendingIntent =
        PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
    AlarmManager alarmManager =
        (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC_WAKEUP,
                     SystemClock.elapsedRealtime() + 1000,
                     pendingIntent);
  }
}
