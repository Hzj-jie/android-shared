package org.gemini.shared;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class TriggerSensorListener extends SensorListener {
  private static final String TAG =
      Debugging.createTag("TriggerSensorListener");
  private final Event.Raisable<Void> onDetected;
  private final TriggerEventListener listener;
  private long detectedMs = System.currentTimeMillis();

  public TriggerSensorListener(Context context) {
    super(context);
    onDetected = new Event.Raisable<>();
    listener = newListener();
    start();
  }

  public final Event<Void> onDetected() {
    return onDetected;
  }

  public long detectedMs() {
    return detectedMs;
  }

  public final void start() {
    if (!manager().requestTriggerSensor(listener, sensor)) {
      Log.e(TAG, "Failed to start trigger sensor " + sensorListenerType);
    }
  }

  public final void stop() {
    if (!manager().cancelTriggerSensor(listener, sensor)) {
      Log.e(TAG, "Failed to stop trigger sensor " + sensorListenerType);
    }
  }

  protected static String toString(TriggerEvent event) {
    if (event == null) return "[TriggerEvent] null";
    // TODO: Better string representation.
    return event.toString();
  }

  private TriggerEventListener newListener() {
    return new TriggerEventListener() {
      @Override
      public void onTrigger(TriggerEvent event) {
        if (event == null) return;
        Log.i(TAG, "Trigger sensor " + sensorListenerType + " detected at " +
                   System.currentTimeMillis() + ": " +
                   TriggerSensorListener.toString(event));
        detectedMs = System.currentTimeMillis();
        onDetected.raise(null);
        start();
      }
    };
  }
}
