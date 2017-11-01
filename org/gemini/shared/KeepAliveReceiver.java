package org.gemini.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class KeepAliveReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    context.startService(serviceIntent(context));
  }

  protected Intent serviceIntent(Context context) {
    return new Intent(serviceAction(),
                      serviceUri(),
                      context,
                      serviceClass());
  }

  protected String serviceAction() {
    return KeepAliveService.KEEP_ALIVE;
  }

  protected Uri serviceUri() {
    return Uri.EMPTY;
  }

  protected Class<?> serviceClass() {
    assert(false);
    return null;
  }
}
