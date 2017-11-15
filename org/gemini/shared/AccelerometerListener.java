package org.gemini.shared;

import android.content.Context;
import android.hardware.Sensor;

public final class AccelerometerListener extends SensorEventListener {
  private static final String TAG =
      Debugging.createTag("AccelerometerListener");
  private static final int TYPE = Sensor.TYPE_ACCELEROMETER;
  private static final WakeupChoice WAKEUP = WakeupChoice.DISLIKE;

  public static boolean isSupported(Context context) {
    return isSupported(context, TYPE, WAKEUP);
  }

  public AccelerometerListener(Configuration config) {
    super(config);
  }

  protected int sensorType() {
    return TYPE;
  }

  protected WakeupChoice wakeup() {
    return WAKEUP;
  }
}
