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
  private boolean started = false;

  public TriggerSensorListener(Context context) {
    super(context);
    onDetected = new Event.Raisable<>();
    listener = newListener();
    start();
  }

  public final Event<Void> onDetected() {
    return onDetected;
  }

  public final long detectedMs() {
    return detectedMs;
  }

  public final void start() {
    if (started) return;
    if (!manager().requestTriggerSensor(listener, sensor)) {
      Log.e(TAG, "Failed to start trigger sensor " + sensorListenerType);
      return;
    }
    started = true;
  }

  public final void stop() {
    if (!started) return;
    if (!manager().cancelTriggerSensor(listener, sensor)) {
      Log.e(TAG, "Failed to stop trigger sensor " + sensorListenerType);
      return;
    }
    started = false;
  }

  protected static String toString(TriggerEvent event) {
    if (event == null) return "[TriggerEvent] null";
    // TODO: Better string representation.
    return event.toString();
  }

  private TriggerEventListener newListener() {
    final TriggerSensorListener me = this;
    return new TriggerEventListener() {
      @Override
      public void onTrigger(TriggerEvent event) {
        if (event == null) return;
        Log.i(TAG, "Trigger sensor " + sensorListenerType + " detected at " +
                   System.currentTimeMillis() + ": " +
                   TriggerSensorListener.toString(event));
        detectedMs = System.currentTimeMillis();
        onDetected.raise(null);

        started = false;
        start();
      }
    };
  }
}
