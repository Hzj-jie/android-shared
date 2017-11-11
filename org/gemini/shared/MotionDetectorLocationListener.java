package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import java.util.concurrent.TimeUnit;

public final class MotionDetectorLocationListener extends LocationListener {
  private static final String TAG =
      Debugging.createTag("MotionDetectorLocationListener");
  private static final int MOTION_LOCATION_INTERVAL_MS =
      (int) TimeUnit.SECONDS.toMillis(10);
  private final LocationListener listener;
  private final SignificantMotionListener motionDetector;
  private final int pollIntervalMs;

  public MotionDetectorLocationListener(Context context,
                                        LocationListener listener) {
    super(listener.config());
    this.listener = listener;
    wrap(listener);
    pollIntervalMs = pollIntervalMs(config().timeoutMs);
    if (SignificantMotionListener.isSupported(context)) {
      Log.i(TAG, "SignificantMotionListener is supported.");
      motionDetector = new SignificantMotionListener(context);
      poll();
    } else {
      Log.w(TAG, "SignificantMotionListener is not supported by the system, " +
                 "MotionDetectorLocationListener takes no effect.");
      motionDetector = null;
    }
  }

  public void stop() {
    listener.stop();
    if (motionDetector != null) {
      motionDetector.stop();
    }
  }

  private void poll() {
    ThisThread.post(new Runnable() {
      @Override
      public void run() {
        if (isQualified(mostAccurate())) {
          keepMostAccurate();
        }
        if (isQualified(latest())) {
          keepLatest();
        }

        poll();
      }
    },
    pollIntervalMs);
  }

  private boolean isQualified(Location location) {
    return location != null &&
           motionDetector.motionDetectedMs() <
           location.getTime() - MOTION_LOCATION_INTERVAL_MS;
  }

  private static int pollIntervalMs(int i) {
    if (i < 10) {
      return i / 2;
    }
    if (i < 50) {
      return i - 10;
    }
    return i - 20;
  }
}
