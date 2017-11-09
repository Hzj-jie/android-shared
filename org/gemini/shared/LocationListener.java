package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class LocationListener {
  private static final String TAG = Debugging.createTag("LocationListener");
  protected final Event.PromisedRaisable<Location> onLocationChanged;
  protected final Context context;
  private Location latest;
  private Location mostAccurate;

  public LocationListener(Context context, final int timeoutMs) {
    Preconditions.isNotNull(context);
    this.context = context;
    onLocationChanged = new Event.PromisedRaisable<>();
    onLocationChanged().add(
        new Event.ParameterRunnable<Location>() {
          @Override
          public void run(Location location) {
            if (location == null) return;
            latest = location;
            if (mostAccurate == null ||
                mostAccurate.getAccuracy() >= location.getAccuracy() ||
                (timeoutMs > 0 &&
                 location.getTime() - mostAccurate.getTime() >= timeoutMs)) {
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
    return latest;
  }

  public final Location mostAccurate() {
    return mostAccurate;
  }

  protected static String toString(Location location) {
    if (location == null) return "[Location] null";
    // TODO: Better string representation.
    return location.toString();
  }
}
