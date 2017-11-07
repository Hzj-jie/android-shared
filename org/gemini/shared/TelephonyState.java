package org.gemini.shared;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.lang.reflect.Method;

public final class TelephonyState {
  private static final String TAG = Debugging.createTag("TelephonyState");
  private final Context context;

  public TelephonyState(Context context) {
    assert(context != null);
    this.context = context;
  }

  public TelephonyManager manager() {
    return (TelephonyManager)
        context.getSystemService(Context.TELEPHONY_SERVICE);
  }

  public String carrier() {
    return manager().getNetworkOperatorName();
  }

  public int subId() {
    Method method = null;
    try {
      method = TelephonyManager.class.getDeclaredMethod("getSubId");
      method.setAccessible(true);
      return (Integer)method.invoke(manager());
    } catch (Exception ex) {
      Log.e(TAG, "Failed to execute getSubId: " + ex.toString());
      // SubscriptionManager.INVALID_SUBSCRIPTION_ID;
      return -1;
    }
  }

  public int preferredNetworkType(int subId) {
    int result = preferredNetworkType1(subId);
    if (result != -1) {
      return result;
    }
    return preferredNetworkType2();
  }

  public int preferredNetworkType() {
    return preferredNetworkType(subId());
  }

  public boolean setPreferredNetworkType(int subId, int type) {
    if (preferredNetworkType(subId) == type) {
      return true;
    }
    return setPreferredNetworkType1(subId, type) ||
           setPreferredNetworkType2(type);
  }

  public boolean setPreferredNetworkType(int type) {
    return setPreferredNetworkType(subId(), type);
  }

  private int preferredNetworkType1(int subId) {
    try {
      Method method = TelephonyManager.class.getMethod(
          "getPreferredNetworkType", int.class);
      method.setAccessible(true);
      return (Integer)method.invoke(manager(), subId);
    } catch (Exception ex) {
      Log.e(TAG,
          "Failed to execute getPreferredNetworkType (1): " + ex.toString());
      return -1;
    }
  }

  private int preferredNetworkType2() {
    try {
      Method method = TelephonyManager.class.getMethod(
          "getPreferredNetworkType");
      method.setAccessible(true);
      return (Integer)method.invoke(manager());
    } catch (Exception ex) {
      Log.e(TAG,
          "Failed to execute getPreferredNetworkType (2): " + ex.toString());
      return -1;
    }
  }

  private boolean setPreferredNetworkType1(int subId, int type) {
    try {
      Method method = TelephonyManager.class.getMethod(
          "setPreferredNetworkType", int.class, int.class);
      method.setAccessible(true);
      return (Boolean)method.invoke(manager(), subId, type);
    } catch (Exception ex) {
      Log.e(TAG,
          "Failed to execute setPreferredNetworkType (1): " + ex.toString());
      return false;
    }
  }

  private boolean setPreferredNetworkType2(int type) {
    try {
      Method method = TelephonyManager.class.getMethod(
          "setPreferredNetworkType", int.class);
      method.setAccessible(true);
      return (Boolean)method.invoke(manager(), type);
    } catch (Exception ex) {
      Log.e(TAG,
          "Failed to execute setPreferredNetworkType (2): " + ex.toString());
      return false;
    }
  }
}
