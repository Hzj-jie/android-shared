package org.gemini.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

public abstract class IntentListener {
  private static final String TAG = Debugging.createTag("IntentListener");
  protected final Context context;
  private final BroadcastReceiver listener;

  public IntentListener(Context context, boolean useApplicationContext) {
    Preconditions.isNotNull(context);
    this.context = (useApplicationContext ?
                    context.getApplicationContext() :
                    context);
    listener = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        if (intent == null) return;
        Log.i(TAG, "Receive action " + intent.getAction());
        raise(context, intent);
      }
    };
    start();
  }

  public IntentListener(Context context) {
    this(context, true);
  }

  public final void stop() {
    context.unregisterReceiver(listener);
  }

  protected IntentFilter filter() {
    IntentFilter r = new IntentFilter();
    String[] as = actions();
    Preconditions.isNotNull(as);
    Preconditions.isTrue(as.length > 0);
    for (String action : as) {
      Preconditions.isNotNull(action);
      r.addAction(action);
    }
    return r;
  }

  protected String[] actions() {
    Preconditions.notReached();
    return null;
  }

  protected abstract void raise(Context context, Intent intent);

  private final void start() {
    IntentFilter f = filter();
    try {
      context.registerReceiver(listener, f);
    } catch (Exception ex) {
      Log.e(TAG, "context.registerReceiver throws exception " +
                 Debugging.toString(ex) +
                 " when registering IntentFilter " +
                 Debugging.toString(f));
    }
  }
}
