package org.gemini.shared;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public final class AccelerometerListener {
  private static final String TAG =
      Debugging.createTag("AccelerometerListener");
  private static final int TYPE = Sensor.TYPE_ACCELEROMETER;
  private final Event.Raisable<SensorEvent> onDetected;

  public static boolean isSupported(Context context) {
    return false;
  }

  public AccelerometerListener(Context context) {
    onDetected = new Event.Raisable<>();
  }
}
