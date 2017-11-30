package org.gemini.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public final class NetworkListener {
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

  private final Context context;
  private final Listener listener;
  private final Event.PromisedRaisable<State> onStateChanged;

  public NetworkListener(Context context) {
    Preconditions.isNotNull(context);
    this.context = context;
    listener = new Listener(this);
    onStateChanged = new Event.PromisedRaisable<>();
    start();
  }

  public Event<State> onStateChanged() {
    return onStateChanged;
  }

  public void stop() {
    context.unregisterReceiver(listener);
  }

  private void start() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    context.registerReceiver(listener, filter);
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

  private static final class Listener extends BroadcastReceiver {
    private final NetworkListener owner;

    public Listener(NetworkListener owner) {
      Preconditions.isNotNull(owner);
      this.owner = owner;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (context == null) return;
      if (intent == null) return;
      Log.i(TAG, "Receive action " + intent.getAction());
      owner.raise();
    }
  }
}
