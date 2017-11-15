package org.gemini.shared;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public final class FusedLocationListener extends LocationListener {
  private static final String TAG =
      Debugging.createTag("FusedLocationListener");
  private final int gpsPollIntervalMs;
  private final SystemLocationListener gps;
  private final SystemLocationListener network;
  private final SystemLocationListener passive;

  public static final class Configuration
      extends LocationListener.Configuration {
    public Context context = null;
    // See SystemLocationListener.Configuration.intervalMs.
    public int intervalMs = 30000;
    // See SystemLocationListener.Configuration.distanceMeter.
    public float distanceMeter = 10;
    // <=0 to disable GPS. Usually it should be slightly larger than timeoutMs
    // to avoid being called right before the last Location timed out.
    public int gpsPollIntervalMs = 300100;
  }

  private FusedLocationListener(Configuration config) {
    super(config);
    Preconditions.isNotNull(config);

    gpsPollIntervalMs = config.gpsPollIntervalMs;

    gps = new SystemLocationListener(gpsConfig(config));
    network = new SystemLocationListener(networkConfig(config));
    passive = new SystemLocationListener(passiveConfig(config));

    wrap(gps);
    wrap(network);
    wrap(passive);

    if (gpsPollIntervalMs > 0) {
      pollGps();
    }
  }

  public static LocationListener create(Configuration config) {
    return new MotionDetectorLocationListener(
        config.context, new FusedLocationListener(config));
  }

  public void stop() {
    gps.stop();
    network.stop();
    passive.stop();
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
        gpsPollIntervalMs);
  }

  private static void copyConfig(SystemLocationListener.Configuration dst,
                                 Configuration src) {
    dst.copyFrom(src);
    dst.context = src.context;
    dst.intervalMs = src.intervalMs;
    dst.distanceMeter = src.distanceMeter;
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
    return result;
  }

  private static SystemLocationListener.Configuration passiveConfig(
      Configuration config) {
    SystemLocationListener.Configuration result =
        new SystemLocationListener.Configuration();
    copyConfig(result, config);
    result.passive();
    return result;
  }
}
