package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class LocationListener {
  private static final String TAG = Debugging.createTag("LocationListener");
  private final int timeoutMs;
  private final float acceptableErrorMeter;
  private final Event.PromisedRaisable<Location> onLocationChanged;
  private Location latest;
  private Location mostAccurate;

  public static class Configuration {
    // <= 0 to disable timeout.
    public int timeoutMs = 300000;
    // < 0 to disable the limitation of accuracy.
    public float acceptableErrorMeter = 200;

    public void copyFrom(Configuration other) {
      Preconditions.isNotNull(other);
      timeoutMs = other.timeoutMs;
      acceptableErrorMeter = other.acceptableErrorMeter;
    }
  }

  public LocationListener(Configuration config) {
    Preconditions.isNotNull(config);
    timeoutMs = config.timeoutMs;
    acceptableErrorMeter = config.acceptableErrorMeter;
    onLocationChanged = new Event.PromisedRaisable<>();
  }

  public void stop() {}

  public final Event<Location> onLocationChanged() {
    return onLocationChanged;
  }

  public final Location latest() {
    if (timedOut(latest)) {
      latest = null;
    }
    return latest;
  }

  public final Location mostAccurate() {
    if (timedOut(mostAccurate)) {
      mostAccurate = null;
    }
    return mostAccurate;
  }

  protected final void clearMostAccurate() {
    mostAccurate = null;
  }

  protected final void clear() {
    mostAccurate = null;
    latest = null;
  }

  protected final void newLocationReceived(Location location) {
	Log.i(TAG, "Get location changed event: " + toString(location));
    if (location == null) return;
    if (acceptableErrorMeter >= 0 &&
        location.getAccuracy() > acceptableErrorMeter) return;
    if (timedOut(location)) return;

    // Clear latest if it's timed out.
    if (latest() == null || latest.getTime() < location.getTime()) {
      latest = location;
    }
    // Clear mostAccurate if it's timed out.
    if (mostAccurate() == null ||
        mostAccurate.getAccuracy() >= location.getAccuracy()) {
      mostAccurate = location;
    }
  }

  protected static String toString(Location location) {
    if (location == null) return "[Location] null";
    // TODO: Better string representation.
    return location.toString();
  }

  private final boolean timedOut(Location location) {
    return location == null ||
           (timeoutMs > 0 &&
            location.getTime() < System.currentTimeMillis() - timeoutMs);
  }
}
