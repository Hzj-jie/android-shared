package org.gemini.shared;

import android.content.Context;
import android.location.Location;

public final class FusedLocationListener extends LocationListener {
  private static final String TAG =
      Debugging.createTag("FusedLocationListener");
  private final SystemLocationListener gps;
  private final SystemLocationListener network;
  private final SystemLocationListener passive;

  public static final class Configuration {
    public Context context = null;
    public int intervalMs = 30000;
    public int timeoutMs = 300000;
    public float distanceMeter = 10;
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

    pollGps(config.timeoutMs, config.acceptableErrorMeter);
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

  private void pollGps(final int timeoutMs, final float acceptableErrorMeter) {
    if (mostAccurate() == null ||
        mostAccurate().getAccuracy() > acceptableErrorMeter ||
        System.currentTimeMillis() - mostAccurate().getTime() > timeoutMs) {
      gps.requestOnce();
    }

    final FusedLocationListener me = this;
    ThisThread.post(
        new Runnable() {
          @Override
          public void run() {
            me.pollGps(timeoutMs, acceptableErrorMeter);
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
