package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class SystemLocationListener extends LocationListener {
  private static final String TAG =
      Debugging.createTag("SystemLocationListener");
  private final Configuration config;
  private final Event.Raisable<Void> onProviderDisabled;
  private final Event.Raisable<Void> onProviderEnabled;
  private final Event.Raisable<Integer> onStatusChanged;
  private final Listener listener;
  private boolean started = false;

  public static final class Configuration {
    public Context context = null;
    // See requestLocationUpdates.
    public int intervalMs = 30000;
    // See LocationListener.timeoutMs.
    public int timeoutMs = 300000;
    // See requestLocationUpdates.
    public float distanceMeter = 10;
    public String provider;
    // < 0 to disable the limitation of accuracy.
    public float acceptableErrorMeter = 200;
    public boolean autoStart = true;

    public Configuration() {
      network();
    }

    public void gps() {
      provider = LocationManager.GPS_PROVIDER;
    }

    public void network() {
      provider = LocationManager.NETWORK_PROVIDER;
    }

    public void passive() {
      provider = LocationManager.PASSIVE_PROVIDER;
    }
  }

  public SystemLocationListener(Configuration config) {
    super(config.context, config.timeoutMs);
    this.config = config;
    onProviderDisabled = new Event.Raisable<>();
    onProviderEnabled = new Event.Raisable<>();
    onStatusChanged = new Event.Raisable<>();
    listener = new Listener(this);
    if (config.autoStart) {
      start();
    }

    Location last = null;
    try {
      last = manager().getLastKnownLocation(config.provider);
    } catch (Exception e) {
      Log.e(TAG, "Failed to get last known location from provider " +
                 config.provider + ": " + e.toString());
    }
    if (last != null) {
      Log.i(TAG, "Get latest location: " + toString(last));
      onLocationChanged.raise(last);
    }
  }

  public void start() {
    if (started) return;
    try {
      manager().requestLocationUpdates(
          config.provider, config.intervalMs, config.distanceMeter, listener);
      started = true;
    } catch (Exception e) {
      Log.e(TAG, "Failed to request location updates from provider " +
                 config.provider + ": " + e.toString());
    }
  }

  public void stop() {
    if (!started) return;
    manager().removeUpdates(listener);
    started = false;
  }

  public void requestOnce() {
    if (started) return;
    onLocationChanged().addOnce(
        new Event.ParameterRunnable<Location>() {
          @Override
          public void run(Location location) {
            stop();
          }
        });
    start();
  }

  public Event<Void> onProviderDisabled() {
    return onProviderDisabled;
  }

  public Event<Void> onProviderEnabled() {
    return onProviderEnabled;
  }

  public Event<Integer> onStatusChanged() {
    return onStatusChanged;
  }

  public LocationManager manager() {
    return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  private static final class Listener
      implements android.location.LocationListener {
    private final SystemLocationListener owner;

    public Listener(SystemLocationListener owner) {
      Preconditions.isNotNull(owner);
      this.owner = owner;
    }

    @Override
    public void onLocationChanged(Location location) {
      if (location != null) {
        Log.i(TAG, "Get location changed event: " +
                   LocationListener.toString(location));
        if (owner.config.acceptableErrorMeter < 0 ||
            location.getAccuracy() <= owner.config.acceptableErrorMeter) {
          owner.onLocationChanged.raise(location);
        }
      }
    }

    @Override
    public void onProviderDisabled(String provider) {
      owner.onProviderDisabled.raise(null);
    }

    @Override
    public void onProviderEnabled(String provider) {
      owner.onProviderEnabled.raise(null);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      owner.onStatusChanged.raise(status);
    }
  }
}
