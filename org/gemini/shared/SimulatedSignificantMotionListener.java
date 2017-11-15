package org.gemini.shared;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.concurrent.TimeUnit;

// Simulates Significant Motion sensor by using Accelerometer sensor.
// Clients should always use SignificantMotionListener.
public final class SimulatedSignificantMotionListener {
  private static final String TAG =
      Debugging.createTag("SimulatedSignificantMotionListener");
  private final Event.Raisable<Void> onDetected;
  private final int triggerSpeedMeterPerSecond;
  private final int triggerDistanceMeter;
  private final AccelerometerListener listener;
  private long lastEventTimeMs = 0;
  private float speedMeterPerSecond = 0;
  private float distance = 0;

  public static boolean isSupported(Context context) {
    return AccelerometerListener.isSupported(context);
  }

  public static class Configuration
      extends AccelerometerListener.Configuration {
    public int triggerSpeedMeterPerSecond = 20;
    public int triggerDistanceMeter = 10000;

    public Configuration() {
      intervalMs = 1000;
    }
  }

  public SimulatedSignificantMotionListener(Configuration config) {
    Preconditions.isNotNull(config);
    onDetected = new Event.Raisable<>();
    triggerSpeedMeterPerSecond = config.triggerSpeedMeterPerSecond;
    triggerDistanceMeter = config.triggerDistanceMeter;
    listener = new AccelerometerListener(config);
    listener.onDetected().add(new Event.ParameterRunnable<SensorEvent>() {
      @Override
      public void run(SensorEvent event) {
        Preconditions.isNotNull(event);
        if (event.values.length != 3) return;
        long eventTimeMs = TimeUnit.NANOSECONDS.toMillis(event.timestamp);
        if (lastEventTimeMs != 0) {
          distance += speedMeterPerSecond *
              TimeUnit.MILLISECONDS.toSeconds(eventTimeMs - lastEventTimeMs);
          float acceleration = (float) Math.sqrt(
              Math.pow(event.values[0], 2) +
              Math.pow(event.values[1], 2) +
              Math.pow(event.values[2], 2) -
              Math.pow(SensorManager.GRAVITY_EARTH, 2));
          if (Float.isNaN(acceleration)) {
            acceleration = 0;
          }
          speedMeterPerSecond += acceleration *
              TimeUnit.MILLISECONDS.toSeconds(eventTimeMs - lastEventTimeMs);
          Log.i(TAG, "Acceleration: " + acceleration +
                     ", Speed: " + speedMeterPerSecond +
                     ", Distance: " + distance);
          boolean trigger = false;
          if (distance >= triggerDistanceMeter) {
            Log.i(TAG, "Distance over " + triggerDistanceMeter +
                       ", will trigger event.");
            trigger = true;
          }
          if (speedMeterPerSecond >= triggerSpeedMeterPerSecond) {
            Log.i(TAG, "Speed over " + triggerSpeedMeterPerSecond +
                       ", will trigger event.");
            trigger = true;
          }
          if (trigger) {
            onDetected.raise(null);
            distance = 0;
            speedMeterPerSecond = 0;
          }
        }

        lastEventTimeMs = eventTimeMs;
      }
    });
  }

  public Event<Void> onDetected() {
    return onDetected;
  }

  public void stop() {
    listener.stop();
  }
}
