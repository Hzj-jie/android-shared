package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class LocationListener {
  private static final String TAG = Debugging.createTag("LocationListener");
  protected final String listenerType;
  private final int timeoutMs;
  private final float acceptableErrorMeter;
  private final Event.PromisedRaisable<Location> onLocationChanged;
  private final Event.Raisable<Location> onLocationReceived;
  private final ArrayList<LocationListener> wrappedListeners;
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
    listenerType = getClass().getName();
    Preconditions.isNotNull(config);
    timeoutMs = config.timeoutMs;
    acceptableErrorMeter = config.acceptableErrorMeter;
    onLocationChanged = new Event.PromisedRaisable<>();
    onLocationReceived = new Event.Raisable<>();
    wrappedListeners = new ArrayList<>();
  }

  public void stop() {}

  public final Event<Location> onLocationChanged() {
    return onLocationChanged;
  }

  public final Event<Location> onLocationReceived() {
    return onLocationReceived;
  }

  public final Location latest() {
    if (timedOut(latest)) {
      latest = null;
      return null;
    }
    return new Location(latest);
  }

  public final Location mostAccurate() {
    if (timedOut(mostAccurate)) {
      mostAccurate = null;
      return null;
    }
    return new Location(mostAccurate);
  }

  public final Configuration config() {
    Configuration r = new Configuration();
    r.timeoutMs = timeoutMs;
    r.acceptableErrorMeter = acceptableErrorMeter;
    return r;
  }

  protected final void clearMostAccurate() {
    mostAccurate = null;
    for (LocationListener listener : wrappedListeners) {
      listener.clearMostAccurate();
    }
  }

  protected final void clear() {
    mostAccurate = null;
    latest = null;
    for (LocationListener listener : wrappedListeners) {
      listener.clear();
    }
  }

  protected final void newLocationReceived(Location location) {
	Log.i(TAG, "Get location changed event: " + toString(location));
    if (location == null) return;
    if (acceptableErrorMeter >= 0 &&
        location.getAccuracy() > acceptableErrorMeter) return;
    if (timedOut(location)) return;

    boolean newLocation = false;
    // Clear latest if it's timed out.
    if (latest() == null || latest.getTime() < location.getTime()) {
      newLocation = true;
      latest = location;
    }
    // Clear mostAccurate if it's timed out.
    if (mostAccurate() == null ||
        mostAccurate.getAccuracy() >= location.getAccuracy()) {
      newLocation = true;
      mostAccurate = location;
    }
    if (newLocation) {
      onLocationChanged.raise(location);
    }
    onLocationReceived.raise(location);
  }

  protected final void keepLatest() {
    Log.i(TAG, listenerType + " kept the latest location.");
    updateToNow(latest);
    for (LocationListener listener : wrappedListeners) {
      listener.keepLatest();
    }
  }

  protected final void keepMostAccurate() {
    Log.i(TAG, listenerType + " kept the most accurate location.");
    updateToNow(mostAccurate);
    for (LocationListener listener : wrappedListeners) {
      listener.keepMostAccurate();
    }
  }

  protected final void wrap(LocationListener listener) {
    Preconditions.isNotNull(listener);
    wrappedListeners.add(listener);
    listener.onLocationReceived().add(new Event.ParameterRunnable<Location>() {
      @Override
      public void run(Location location) {
        newLocationReceived(location);
      }
    });
  }

  protected static String toString(Location location) {
    if (location == null) return "[Location] null";
    // TODO: Better string representation.
    StringBuilder builder = new StringBuilder();
    builder.append("[Location ")
           .append(location.getProvider())
           .append("] X: ")
           .append(location.getLongitude())
           .append(" Y: ")
           .append(location.getLatitude())
           .append(" Z: ");
    if (location.hasAltitude()) {
      builder.append(location.getAltitude());
    } else {
      builder.append("N/A");
    }
    builder.append(" Accuracy: ")
           .append(location.getAccuracy())
           .append(" Age (seconds): ")
           .append(TimeUnit.MILLISECONDS.toSeconds(
                 System.currentTimeMillis() - location.getTime()))
           .append(" Bearing: ");
    if (location.hasBearing()) {
      builder.append(location.getBearing());
    } else {
      builder.append("N/A");
    }
    builder.append(" Speed: ");
    if (location.hasSpeed()) {
      builder.append(location.getSpeed());
    } else {
      builder.append("N/A");
    }
    return builder.toString();
  }

  protected static void updateToNow(Location location) {
    Preconditions.isNotNull(location);
    location.setTime(System.currentTimeMillis());
  }

  private final boolean timedOut(Location location) {
    return location == null ||
           (timeoutMs > 0 &&
            location.getTime() < System.currentTimeMillis() - timeoutMs);
  }
}
