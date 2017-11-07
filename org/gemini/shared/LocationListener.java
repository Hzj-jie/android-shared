package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class LocationListener {
  private static final String TAG = Debugging.createTag("LocationListener");
  private final Event.PromisedRaisable<Location> onLocationChanged;
  private final Event.Raisable<Void> onProviderDisabled;
  private final Event.Raisable<Void> onProviderEnabled;
  private final Event.Raisable<Integer> onStatusChanged;
  private final Listener listener;
  private final Context context;

  public static final class Configuration {
    public Context context = null;
    public int intervalMs = 30000;
    public float distanceMeter = 10;
    public List<String> providers;

    public Configuration() {
      powerSaving();
    }

    public void addPassiveProvider() {
      providers.add(LocationManager.PASSIVE_PROVIDER);
    }

    public void passiveOnly() {
      providers = new ArrayList<>();
      addPassiveProvider();
    }

    public void addGpsProvider() {
      providers.add(LocationManager.GPS_PROVIDER);
    }

    public void gpsOnly() {
      providers = new ArrayList<>();
      addGpsProvider();
    }

    public void addNetworkProvider() {
      providers.add(LocationManager.NETWORK_PROVIDER);
    }

    public void networkOnly() {
      providers = new ArrayList<>();
      addNetworkProvider();
    }

    public void powerSaving() {
      passiveOnly();
      addNetworkProvider();
    }

    public void allProviders() {
      passiveOnly();
      addNetworkProvider();
      addGpsProvider();
    }
  }

  public LocationListener(Configuration config) {
    onLocationChanged = new Event.PromisedRaisable<>();
    onProviderDisabled = new Event.Raisable<>();
    onProviderEnabled = new Event.Raisable<>();
    onStatusChanged = new Event.Raisable<>();
    listener = new Listener(this);

    assert(config != null);
    assert(config.context != null);
    assert(!config.providers.isEmpty());
    context = config.context;

    Location lastBest = null;
    for (String provider : config.providers) {
      try {
        manager().requestLocationUpdates(
            provider, config.intervalMs, config.distanceMeter, listener);
      } catch (Exception e) {
        Log.e(TAG, "Failed to request location updates from provider " +
                   provider + ": " + e.toString());
      }

      Location last = null;
      try {
        last = manager().getLastKnownLocation(provider);
      } catch (Exception e) {
        Log.e(TAG, "Failed to get last known location from provider " +
                   provider + ": " + e.toString());
      }
      if (last != null) {
        // This is an inaccurate sorting.
        if (lastBest == null ||
            lastBest.getTime() < last.getTime() ||
            lastBest.getAccuracy() > last.getAccuracy()) {
          lastBest = last;
        }
      }
    }

    if (lastBest != null) {
      Log.i(TAG, "Get latest location: X: " + lastBest.getLongitude() +
                 ", Y: " + lastBest.getLatitude());
      onLocationChanged.raise(lastBest);
    }
  }

  public void stop() {
    manager().removeUpdates(listener);
  }

  public Event<Location> onLocationChanged() {
    return onLocationChanged;
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
    private final LocationListener owner;

    public Listener(LocationListener owner) {
      assert(owner != null);
      this.owner = owner;
    }

    @Override
    public void onLocationChanged(Location location) {
      if (location != null) {
        Log.i(TAG, "Get location changed event: X: " + location.getLongitude() +
                   ", Y: " + location.getLatitude());
        owner.onLocationChanged.raise(location);
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
