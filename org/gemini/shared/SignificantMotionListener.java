package org.gemini.shared;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public final class SignificantMotionListener {
  private static final String TAG =
      Debugging.createTag("SignificantMotionListener");
  private final Context context;
  private final Event.Raisable<Void> onMotionDetected;
  private final Sensor sensor;
  private final TriggerEventListener listener;
  private long motionDetectedMs = System.currentTimeMillis();

  public SignificantMotionListener(Context context) {
    Preconditions.isTrue(isSupported(context));
    Preconditions.isNotNull(context);
    this.context = context;
    onMotionDetected = new Event.Raisable<>();
    sensor = sensor();
    Preconditions.isNotNull(sensor);
    listener = newListener();
    manager().requestTriggerSensor(listener, sensor);
  }

  public static boolean isSupported(Context context) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
           sensor(context) != null;
  }

  private static SensorManager manager(Context context) {
    return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
  }

  private static Sensor sensor(Context context) {
    return manager(context).getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
  }

  public SensorManager manager() {
    return manager(context);
  }

  private Sensor sensor() {
    return sensor(context);
  }

  public Event<Void> onMotionDetected() {
    return onMotionDetected;
  }

  public long motionDetectedMs() {
    return motionDetectedMs;
  }

  public void stop() {
    manager().cancelTriggerSensor(listener, sensor);
  }

  private TriggerEventListener newListener() {
    return new TriggerEventListener() {
      @Override
      public void onTrigger(TriggerEvent event) {
        if (event == null) return;
        Log.i(TAG, "Motion detected at " + System.currentTimeMillis() + ": " +
                   event.toString());
        motionDetectedMs = System.currentTimeMillis();
        onMotionDetected.raise(null);
      }
    };
  }
}
