package org.gemini.shared;

import android.content.Context;
import android.hardware.Sensor;
import android.os.Build;

public final class SystemSignificantMotionListener
    extends TriggerSensorListener {
  private static final String TAG =
      Debugging.createTag("SystemSignificantMotionListener");
  private static final int TYPE = Sensor.TYPE_SIGNIFICANT_MOTION;
  private static final WakeupChoice WAKEUP = WakeupChoice.DEFAULT;

  public SystemSignificantMotionListener(Context context) {
    super(context);
  }

  public static boolean isSupported(Context context) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
           isSupported(context, TYPE, WAKEUP);
  }

  protected int sensorType() {
    return TYPE;
  }

  protected WakeupChoice wakeup() {
    return WAKEUP;
  }
}
