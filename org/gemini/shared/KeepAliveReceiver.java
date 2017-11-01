package org.gemini.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class KeepAliveReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null) {
      context.startService(serviceIntent(context, intent));
    }
  }

  protected Intent serviceIntent(Context context, Intent intent) {
    return new Intent(serviceAction(intent),
                      serviceUri(intent),
                      context,
                      serviceClass(intent));
  }

  protected String serviceAction(Intent intent) {
    String action = serviceAction();
    if (action == null) {
      action = intent.getAction();
    }
    return action;
  }

  protected String serviceAction() {
    return null;
  }

  protected Uri serviceUri(Intent intent) {
    return serviceUri();
  }

  protected Uri serviceUri() {
    return Uri.EMPTY;
  }

  protected Class<?> serviceClass(Intent intent) {
    return serviceClass();
  }

  protected Class<?> serviceClass() {
    assert(false);
    return null;
  }
}
