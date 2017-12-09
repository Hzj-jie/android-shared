package org.gemini.shared;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class ScreenListener extends IntentListener {
  private static final String TAG = Debugging.createTag("ScreenListener");
  private final Event.Raisable<Void> onScreenOn;
  private final Event.Raisable<Void> onScreenOff;
  private final Event.Raisable<Void> onUserPresent;

  public ScreenListener(Context context) {
    super(context);
    onScreenOn = new Event.Raisable<>();
    onScreenOff = new Event.Raisable<>();
    onUserPresent = new Event.Raisable<>();
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

  protected String[] actions() {
    return new String[] {
      Intent.ACTION_SCREEN_ON,
      Intent.ACTION_SCREEN_OFF,
      Intent.ACTION_USER_PRESENT,
    };
  }

  protected void raise(Context context, Intent intent) {
    Preconditions.isNotNull(intent);
    if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
      onScreenOn.raise(null);
    } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
      onScreenOff.raise(null);
    } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
      onUserPresent.raise(null);
    }
  }
}
