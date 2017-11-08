package org.gemini.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public final class ScreenListener {
  private static final String TAG = Debugging.createTag("ScreenListener");
  private final Context context;
  private final Listener listener;
  private final Event.Raisable<Void> onScreenOn;
  private final Event.Raisable<Void> onScreenOff;
  private final Event.Raisable<Void> onUserPresent;

  public ScreenListener(Context context) {
    Preconditions.isNotNull(context);
    this.context = context;
    listener = new Listener(this);
    onScreenOn = new Event.Raisable<>();
    onScreenOff = new Event.Raisable<>();
    onUserPresent = new Event.Raisable<>();
    start();
  }

  public Event<Void> onScreenOn() {
    return onScreenOn;
  }

  public Event<Void> onScreenOff() {
    return onScreenOff;
  }

  public Event<Void> onUserPresent() {
    return onUserPresent;
  }

  public void stop() {
    context.unregisterReceiver(listener);
  }

  private void start() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SCREEN_ON);
    filter.addAction(Intent.ACTION_SCREEN_OFF);
    filter.addAction(Intent.ACTION_USER_PRESENT);
    context.registerReceiver(listener, filter);
  }

  private static final class Listener extends BroadcastReceiver {
    private final ScreenListener owner;

    public Listener(ScreenListener owner) {
      Preconditions.isNotNull(owner);
      this.owner = owner;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent == null) return;
      Log.d(TAG, "Receive action " + intent.getAction());
      if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
        owner.onScreenOn.raise(null);
      } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
        owner.onScreenOff.raise(null);
      } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
        owner.onUserPresent.raise(null);
      }
    }
  }
}
