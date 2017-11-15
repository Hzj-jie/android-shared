package org.gemini.shared;

import android.content.Context;
import android.util.Log;

public final class SignificantMotionListener {
  private static final String TAG =
      Debugging.createTag("SignificantMotionListener");
  private final Event.Raisable<Void> onDetected;
  private final SystemSignificantMotionListener systemListener;
  private final SimulatedSignificantMotionListener simulatedListener;
  private long detectedMs = System.currentTimeMillis();

  public static boolean isSupported(Context context) {
    return SystemSignificantMotionListener.isSupported(context) ||
           SimulatedSignificantMotionListener.isSupported(context);
  }

  public SignificantMotionListener(Context context) {
    this(config(context));
  }

  public SignificantMotionListener(
      SimulatedSignificantMotionListener.Configuration config) {
    Preconditions.isNotNull(config);
    onDetected = new Event.Raisable<>();
    if (SystemSignificantMotionListener.isSupported(config.context)) {
      Log.i(TAG, "Use SystemSignificantMotionListener");
      systemListener = new SystemSignificantMotionListener(config.context);
      systemListener.onDetected().add(new Event.ParameterRunnable<Void>() {
        @Override
        public void run(Void nothing) {
          onTrigger(systemListener.detectedMs());
        }
      });
      simulatedListener = null;
    } else if (SimulatedSignificantMotionListener.isSupported(config.context)) {
      Log.i(TAG, "Use SimulatedSignificantMotionListener");
      systemListener = null;
      simulatedListener = new SimulatedSignificantMotionListener(config);
      simulatedListener.onDetected().add(new Event.ParameterRunnable<Void>() {
        @Override
        public void run(Void nothing) {
          onTrigger();
        }
      });
    } else {
      systemListener = null;
      simulatedListener = null;
      Preconditions.notReached();
    }
  }

  public void stop() {
    if (systemListener != null) systemListener.stop();
    if (simulatedListener != null) simulatedListener.stop();
  }

  public Event<Void> onDetected() {
    return onDetected;
  }

  public long detectedMs() {
    return detectedMs;
  }

  private void onTrigger(long timeMs) {
    detectedMs = timeMs;
    onDetected.raise(null);
  }

  private void onTrigger() {
    onTrigger(System.currentTimeMillis());
  }

  private static SimulatedSignificantMotionListener.Configuration config(
      Context context) {
    SimulatedSignificantMotionListener.Configuration result =
        new SimulatedSignificantMotionListener.Configuration();
    result.context = context;
    return result;
  }
}
