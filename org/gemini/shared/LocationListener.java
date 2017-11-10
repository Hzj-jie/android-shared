package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class LocationListener {
  private static final String TAG = Debugging.createTag("LocationListener");
  protected final Event.PromisedRaisable<Location> onLocationChanged;
  protected final Context context;
  // <= 0 to disable timeout.
  protected final int timeoutMs;
  private Location latest;
  private Location mostAccurate;

  public LocationListener(Context context, int timeoutMs) {
    Preconditions.isNotNull(context);
    this.context = context;
    this.timeoutMs = timeoutMs;
    onLocationChanged = new Event.PromisedRaisable<>();
    onLocationChanged().add(
        new Event.ParameterRunnable<Location>() {
          @Override
          public void run(Location location) {
            if (location == null) return;
            latest = location;
            // Clear mostAccurate if it's timed out.
            if (mostAccurate() == null ||
                mostAccurate.getAccuracy() >= location.getAccuracy()) {
              mostAccurate = location;
              return;
            }
          }
        });
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

  protected static String toString(Location location) {
    if (location == null) return "[Location] null";
    // TODO: Better string representation.
    return location.toString();
  }

  protected boolean timedOut(Location location) {
    return location == null ||
           (timeoutMs > 0 &&
            location.getTime() < System.currentTimeMillis() - timeoutMs);
  }
}
