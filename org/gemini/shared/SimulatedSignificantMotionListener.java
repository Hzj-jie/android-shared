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
    public int triggerDistanceMeter = 1000;
  }

  public SimulatedSignificantMotionListener(Configuration config) {
    Preconditions.isNotNull(config);
    onDetected = new Event.Raisable<>();
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
          speedMeterPerSecond += acceleration *
              TimeUnit.MILLISECONDS.toSeconds(eventTimeMs - lastEventTimeMs);
          Log.i(TAG, "Acceleration: " + acceleration + ", " +
                     "Speed: " + speedMeterPerSecond + ", " +
                     "Distance: " + distance);
          if (distance >= triggerDistanceMeter) {
            Log.i(TAG, "Distance over " + triggerDistanceMeter +
                       ", will trigger event.");
            onDetected.raise(null);
            distance %= triggerDistanceMeter;
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
