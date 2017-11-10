package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public final class FusedLocationListener extends LocationListener {
  private static final String TAG =
      Debugging.createTag("FusedLocationListener");
  private final SystemLocationListener gps;
  private final SystemLocationListener network;
  private final SystemLocationListener passive;

  public static final class Configuration {
    public Context context = null;
    // See SystemLocationListener.Configuration.intervalMs.
    public int intervalMs = 30000;
    // See LocationListener.timeoutMs and
    // SystemLocationListener.Configuration.timeoutMs.
    public int timeoutMs = 300000;
    // See SystemLocationListener.Configuration.distanceMeter.
    public float distanceMeter = 10;
    // See SystemLocationListener.Configuration.acceptableErrorMeter.
    public float acceptableErrorMeter = 200;
  }

  public FusedLocationListener(Configuration config) {
    super(config.context, config.timeoutMs);

    gps = new SystemLocationListener(gpsConfig(config));
    network = new SystemLocationListener(networkConfig(config));
    passive = new SystemLocationListener(passiveConfig(config));

    listen(gps);
    listen(network);
    listen(passive);

    pollGps();
  }

  public void stop() {
    gps.stop();
    network.stop();
    passive.stop();
  }

  private void listen(SystemLocationListener listener) {
    listener.onLocationChanged().add(new Event.ParameterRunnable<Location>() {
      @Override
      public void run(Location location) {
        onLocationChanged.raise(location);
      }
    });
  }

  private void pollGps() {
    if (latest() == null) {  // Imply mostAccurate() == null.
      Log.w(TAG, "No qualified location updates received, " +
                 "actively request GPS update. Most Accurate: " +
                 toString(mostAccurate()) +
                 ", Latest: " +
                 toString(latest()));
      gps.requestOnce();
    }

    ThisThread.post(
        new Runnable() {
          @Override
          public void run() {
            pollGps();
          }
        },
        timeoutMs);
  }

  private static void copyConfig(SystemLocationListener.Configuration dst,
                                 Configuration src) {
    dst.context = src.context;
    dst.intervalMs = src.intervalMs;
    dst.timeoutMs = src.timeoutMs;
    dst.distanceMeter = src.distanceMeter;
    dst.acceptableErrorMeter = src.acceptableErrorMeter;
  }

  private static SystemLocationListener.Configuration gpsConfig(
      Configuration config) {
    SystemLocationListener.Configuration result =
        new SystemLocationListener.Configuration();
    copyConfig(result, config);
    result.gps();
    result.autoStart = false;
    return result;
  }

  private static SystemLocationListener.Configuration networkConfig(
      Configuration config) {
    SystemLocationListener.Configuration result =
        new SystemLocationListener.Configuration();
    copyConfig(result, config);
    result.network();
    result.autoStart = true;
    return result;
  }

  private static SystemLocationListener.Configuration passiveConfig(
      Configuration config) {
    SystemLocationListener.Configuration result =
        new SystemLocationListener.Configuration();
    copyConfig(result, config);
    result.passive();
    result.autoStart = true;
    return result;
  }
}
