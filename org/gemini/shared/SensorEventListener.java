package org.gemini.shared;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Build;
import android.util.Log;
import java.util.concurrent.TimeUnit;

public class SensorEventListener extends SensorListener {
  private static final String TAG =
      Debugging.createTag("SensorEventListener");
  private final int intervalUs;
  private final int maxReportLatencyUs;
  private final Event.Raisable<SensorEvent> onDetected;
  private final Event.Raisable<Integer> onAccuracyChanged;
  private final android.hardware.SensorEventListener listener;

  public static class Configuration {
    public Context context = null;
    // See registerListener().
    public int intervalMs = 20;
    // See registerListener().
    public int maxReportLatencyMs = 60000;
  }

  public SensorEventListener(Configuration config) {
    super(config.context);
    Preconditions.isNotNull(config);
    intervalUs = (int) TimeUnit.MILLISECONDS.toMicros(config.intervalMs);
    maxReportLatencyUs =
        (int) TimeUnit.MILLISECONDS.toMicros(config.maxReportLatencyMs);
    onDetected = new Event.Raisable<>();
    onAccuracyChanged = new Event.Raisable<>();
    listener = newListener();
    start();
  }

  public final Event<SensorEvent> onDetected() {
    return onDetected;
  }

  public final Event<Integer> onAccuracyChanged() {
    return onAccuracyChanged;
  }

  public final void start() {
    boolean result = false;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      result = manager().registerListener(
          listener, sensor, intervalUs, maxReportLatencyUs);
    } else {
      result = manager().registerListener(
          listener, sensor, intervalUs);
    }
    if (!result) {
      Log.e(TAG, "Failed to start event sensor " + sensorListenerType);
    }
  }

  public final void stop() {
    manager().unregisterListener(listener, sensor);
  }

  protected static String toString(SensorEvent event) {
    if (event == null) return "[SensorEvent] null";
    StringBuilder result = new StringBuilder();
    result.append("[SensorEvent] ")
          .append("timestamp: ")
          .append(event.timestamp)
          .append("values: ");
    for (float value : event.values) {
      result.append("{ ")
            .append(value)
            .append("} ");
    }
    return result.toString();
  }

  private android.hardware.SensorEventListener newListener() {
    return new android.hardware.SensorEventListener() {
      @Override
      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "Event sensor " + sensorListenerType + " changed accuracy " +
                   "at " + System.currentTimeMillis() + " to " + accuracy);
        onAccuracyChanged.raise(accuracy);
      }

      @Override
      public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, "Event sensor " + sensorListenerType + " detected at " +
                   System.currentTimeMillis() + ": " +
                   SensorEventListener.toString(event));
        if (event == null) return;
        if (event.sensor.getType() != sensorType()) return;
        onDetected.raise(event);
      }
    };
  }
}
