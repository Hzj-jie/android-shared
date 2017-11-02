package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class LocationListener {
  private final Event.Raisable<Location> onLocationChanged;
  private final Event.Raisable<Void> onProviderDisabled;
  private final Event.Raisable<Void> onProviderEnabled;
  private final Event.Raisable<Integer> onStatusChanged;
  private final Listener listener;

  public class Configuration {
    public Context context = null;
    public int intervalMs = 30000;
    public float distanceMeter = 10;
    public List<String> providers;

    public Configuration() {
      providers = new ArrayList<>();
      providers.add(LocationManager.NETWORK_PROVIDER);
      providers.add(LocationManager.GPS_PROVIDER);
    }
  }

  public LocationListener(Configuration config) {
    onLocationChanged = new Event.Raisable<>();
    onProviderDisabled = new Event.Raisable<>();
    onProviderEnabled = new Event.Raisable<>();
    onStatusChanged = new Event.Raisable<>();
    listener = new Listener(this);

    assert(config != null);
    assert(config.context != null);
    // TODO
  }

  public final Event<Location> onLocationChanged() {
    return onLocationChanged;
  }

  public final Event<Void> onProviderDisabled() {
    return onProviderDisabled;
  }

  public final Event<Void> onProviderEnabled() {
    return onProviderEnabled;
  }

  public final Event<Integer> onStatusChanged() {
    return onStatusChanged;
  }

  private class Listener implements android.location.LocationListener {
    private final LocationListener owner;

    public Listener(LocationListener owner) {
      assert(owner != null);
      this.owner = owner;
    }

    @Override
    public void onLocationChanged(Location location) {
      owner.onLocationChanged.raise(location);
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
