package org.gemini.shared;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public final class NetworkListener extends IntentListener {
  private static final String TAG = Debugging.createTag("NetworkListener");

  public static class State {
    protected boolean wifiIsOn = false;
    protected String ssid = "";
    protected boolean wifiIsConnected = false;
    protected boolean mobileDataIsConnected = false;

    public final boolean wifiIsOn() {
      return wifiIsOn;
    }

    public final String ssid() {
      return ssid;
    }

    public final boolean wifiIsConnected() {
      return wifiIsConnected;
    }

    public final boolean mobileDataIsConnected() {
      return mobileDataIsConnected;
    }

    public static final class Settable extends State {
      public Settable setWifiIsOn(boolean v) {
        wifiIsOn = v;
        return this;
      }

      public Settable setSsid(String v) {
        ssid = v;
        return this;
      }

      public Settable setWifiIsConnected(boolean v) {
        wifiIsConnected = v;
        return this;
      }

      public Settable setMobileDataIsConnected(boolean v) {
        mobileDataIsConnected = v;
        return this;
      }
    }
  }

  private final Event.PromisedRaisable<State> onStateChanged;

  public NetworkListener(Context context) {
    super(context);
    onStateChanged = new Event.PromisedRaisable<>();
    raise();
  }

  public Event<State> onStateChanged() {
    return onStateChanged;
  }

  protected String[] actions() {
    return new String[] {
      WifiManager.NETWORK_STATE_CHANGED_ACTION,
      WifiManager.WIFI_STATE_CHANGED_ACTION,
      ConnectivityManager.CONNECTIVITY_ACTION,
    };
  }

  protected void raise(Context context, Intent intent) {
    raise();
  }

  // TODO: Split the logic: retrieving wifi status in CONNECTIVITY_ACTION is not
  // necessary.
  private void raise() {
    State.Settable state = new State.Settable();

    {
      WifiManager wifiMan = (WifiManager)
          context.getSystemService(Context.WIFI_SERVICE);
      state.setWifiIsOn(wifiMan.isWifiEnabled());

      WifiInfo wifiInfo = wifiMan.getConnectionInfo();
      if (wifiInfo != null) {
        state.setSsid(wifiInfo.getSSID());
      }
    }

    {
      ConnectivityManager conMan = (ConnectivityManager)
          context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = conMan.getActiveNetworkInfo();
      if (netInfo != null) {
        state.setWifiIsConnected(
            netInfo.getType() == ConnectivityManager.TYPE_WIFI);
        state.setMobileDataIsConnected(
            netInfo.getType() == ConnectivityManager.TYPE_MOBILE);
      }
    }

    onStateChanged.raise(state);
  }
}
