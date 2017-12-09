package org.gemini.shared;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public final class BatteryListener extends IntentListener {
  private static final String TAG = Debugging.createTag("BatteryListener");

  public static class State {
    protected boolean powerConnected = false;
    // [0, 100]
    protected int level = 0;
    // Whether this State is fired by a BATTERY_LOW action.
    protected boolean levelLow = false;
    // Charging by USB.
    protected boolean usbCharging = false;
    // Charging by AC.
    protected boolean acCharging = false;
    // Charging by wireless.
    protected boolean wirelessCharging = false;

    public final boolean powerConnected() {
      return powerConnected;
    }

    public final int level() {
      return level;
    }

    public final boolean levelLow() {
      return levelLow;
    }

    public final boolean usbCharging() {
      return usbCharging;
    }

    public final boolean acCharging() {
      return acCharging;
    }

    public final boolean wirelessCharging() {
      return wirelessCharging;
    }

    public static final class Settable extends State {
      public Settable setPowerConnected(boolean v) {
        powerConnected = v;
        return this;
      }

      public Settable setLevel(int v) {
        level = v;
        return this;
      }

      public Settable setLevelLow(boolean v) {
        levelLow = v;
        return this;
      }

      public Settable setUsbCharging(boolean v) {
        usbCharging = v;
        return this;
      }

      public Settable setAcCharging(boolean v) {
        acCharging = v;
        return this;
      }

      public Settable setWirelessCharging(boolean v) {
        wirelessCharging = v;
        return this;
      }
    }
  }

  private final Event.PromisedRaisable<State> onStateChanged;

  public BatteryListener(Context context) {
    super(context);
    onStateChanged = new Event.PromisedRaisable<>();
    raise(false);
  }

  public Event<State> onStateChanged() {
    return onStateChanged;
  }

  protected String[] actions() {
    return new String[] {
      // Do not listen to ACTION_BATTERY_CHANGED to avoid battery drain.
      // filter.addAction(Intent.ACTION_BATTERY_CHANGED);
      Intent.ACTION_POWER_CONNECTED,
      Intent.ACTION_POWER_DISCONNECTED,
      Intent.ACTION_BATTERY_LOW,
      Intent.ACTION_BATTERY_OKAY,
    };
  }

  protected void raise(Context context, Intent intent) {
    Preconditions.isNotNull(intent);
    raise(intent.getAction() == Intent.ACTION_BATTERY_LOW);
  }

  private static boolean powerConnected(Intent batteryStatus) {
    Preconditions.isNotNull(batteryStatus);
    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    return status == BatteryManager.BATTERY_STATUS_CHARGING ||
           status == BatteryManager.BATTERY_STATUS_FULL;
  }

  private static int level(Intent batteryStatus) {
    Preconditions.isNotNull(batteryStatus);
	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    return level * 100 / scale;
  }

  private static int plugged(Intent batteryStatus) {
    Preconditions.isNotNull(batteryStatus);
    return batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
  }

  private static boolean usbCharging(Intent batteryStatus) {
    return plugged(batteryStatus) == BatteryManager.BATTERY_PLUGGED_USB;
  }

  private static boolean acCharging(Intent batteryStatus) {
    return plugged(batteryStatus) == BatteryManager.BATTERY_PLUGGED_AC;
  }

  private static boolean wirelessCharging(Intent batteryStatus) {
    return plugged(batteryStatus) == BatteryManager.BATTERY_PLUGGED_WIRELESS;
  }

  private void raise(boolean levelLow) {
    Intent batteryStatus = context.registerReceiver(
        null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    // What's wrong?
    if (batteryStatus == null) return;
    onStateChanged.raise((new State.Settable())
        .setPowerConnected(powerConnected(batteryStatus))
        .setLevel(level(batteryStatus))
        .setLevelLow(levelLow)
        .setUsbCharging(usbCharging(batteryStatus))
        .setAcCharging(acCharging(batteryStatus))
        .setWirelessCharging(wirelessCharging(batteryStatus)));
  }
}
