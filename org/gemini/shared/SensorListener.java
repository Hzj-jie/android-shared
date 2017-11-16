package org.gemini.shared;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

public class SensorListener {
  protected final String sensorListenerType;
  private final Context context;
  protected final Sensor sensor;

  public SensorListener(Context context) {
    sensorListenerType = Debugging.classLoggingName(getClass());
    Preconditions.isNotNull(context);
    this.context = context;
    sensor = newSensor();
    Preconditions.isNotNull(sensor);
  }

  public void stop() {}

  protected enum WakeupChoice {
    DEFAULT,  // Use old API and let OS make the decision.
    REQUIRE,  // Use only a wakeup sensor.
    PREFER,   // Prefer wakeup sensor, but can still accept a non-wakeup one.
    DISLIKE,  // Prefer non-wakeup sensor, but can still accept a wakeup one.
    DECLINE,  // Use only a non-wakeup sensor.
  }

  protected final SensorManager manager() {
    return manager(context);
  }

  protected Sensor newSensor() {
    return newSensor(context, sensorType(), wakeup());
  }

  protected int sensorType() {
    return Preconditions.notReachedI();
  }

  protected WakeupChoice wakeup() {
    return WakeupChoice.DISLIKE;
  }

  protected static boolean isSupported(Context context,
                                       int type,
                                       WakeupChoice wakeup) {
    return newSensor(context, type, wakeup) != null;
  }

  protected static SensorManager manager(Context context) {
    return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
  }

  protected static Sensor newSensor(Context context,
                                    int type,
                                    WakeupChoice wakeup) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
        wakeup == WakeupChoice.DEFAULT) {
      return manager(context).getDefaultSensor(type);
    }

    if (wakeup == WakeupChoice.PREFER || wakeup == WakeupChoice.DISLIKE) {
      Sensor sensor = newSensor(context, type, wakeup == WakeupChoice.PREFER);
      if (sensor != null) return sensor;
      return newSensor(context, type, wakeup == WakeupChoice.DISLIKE);
    }

    Preconditions.isTrue(wakeup == WakeupChoice.REQUIRE ||
                         wakeup == WakeupChoice.DECLINE);
    return newSensor(context, type, wakeup == WakeupChoice.REQUIRE);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  protected static Sensor newSensor(Context context, int type, boolean wakeup) {
    return manager(context).getDefaultSensor(type, wakeup);
  }
}
