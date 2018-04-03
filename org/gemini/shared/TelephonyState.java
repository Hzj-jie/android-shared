package org.gemini.shared;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.lang.reflect.Method;

public final class TelephonyState {
  // Use ints to make the value of each network class more meaningful.
  public static final int NETWORK_CLASS_UNKNOWN = 0;
  public static final int NETWORK_CLASS_2G = 2;
  public static final int NETWORK_CLASS_3G = 3;
  public static final int NETWORK_CLASS_4G = 4;

  private static final String TAG = Debugging.createTag("TelephonyState");
  private final Context context;

  public TelephonyState(Context context) {
    assert(context != null);
    this.context = context;
  }

  // Copy from https://android.googlesource.com/platform/frameworks/base/+/master/telephony/java/android/telephony/TelephonyManager.java#2076.
  // TODO: Update the SDK to 25 or upper to support IWLAN and LTE_CA natively.
  public static int networkClass(int networkType) {
    switch (networkType) {
      case TelephonyManager.NETWORK_TYPE_GPRS:
      case TelephonyManager.NETWORK_TYPE_EDGE:
      case TelephonyManager.NETWORK_TYPE_CDMA:
      case TelephonyManager.NETWORK_TYPE_1xRTT:
      case TelephonyManager.NETWORK_TYPE_IDEN:
        return NETWORK_CLASS_2G;
      case TelephonyManager.NETWORK_TYPE_UMTS:
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
      case TelephonyManager.NETWORK_TYPE_HSDPA:
      case TelephonyManager.NETWORK_TYPE_HSUPA:
      case TelephonyManager.NETWORK_TYPE_HSPA:
      case TelephonyManager.NETWORK_TYPE_EVDO_B:
      case TelephonyManager.NETWORK_TYPE_EHRPD:
      case TelephonyManager.NETWORK_TYPE_HSPAP:
        return NETWORK_CLASS_3G;
      case TelephonyManager.NETWORK_TYPE_LTE:
      case 18: /* TelephonyManager.NETWORK_TYPE_IWLAN: */
      case 19: /* TelephonyManager.NETWORK_TYPE_LTE_CA: */
        return NETWORK_CLASS_4G;
      default:
        return NETWORK_CLASS_UNKNOWN;
    }
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
    } catch (Exception e) {
      Log.e(TAG, "Failed to execute getSubId: " + e.toString());
      // SubscriptionManager.INVALID_SUBSCRIPTION_ID;
      return -1;
    }
  }

  public int networkType() {
    return manager().getNetworkType();
  }

  public int dataNetworkType() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return manager().getDataNetworkType();
    }
    return networkType();
  }

  public int voiceNetworkType() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return manager().getVoiceNetworkType();
    }
    return networkType();
  }

  public int networkClass() {
    return networkClass(networkType());
  }

  public int dataNetworkClass() {
    return networkClass(dataNetworkType());
  }

  public int voiceNetworkClass() {
    return networkClass(voiceNetworkType());
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
    } catch (Exception e) {
      Log.e(TAG,
          "Failed to execute getPreferredNetworkType (1): " + e.toString());
      return -1;
    }
  }

  private int preferredNetworkType2() {
    try {
      Method method = TelephonyManager.class.getMethod(
          "getPreferredNetworkType");
      method.setAccessible(true);
      return (Integer)method.invoke(manager());
    } catch (Exception e) {
      Log.e(TAG,
          "Failed to execute getPreferredNetworkType (2): " + e.toString());
      return -1;
    }
  }

  private boolean setPreferredNetworkType1(int subId, int type) {
    try {
      Method method = TelephonyManager.class.getMethod(
          "setPreferredNetworkType", int.class, int.class);
      method.setAccessible(true);
      return (Boolean)method.invoke(manager(), subId, type);
    } catch (Exception e) {
      Log.e(TAG,
          "Failed to execute setPreferredNetworkType (1): " + e.toString());
      return false;
    }
  }

  private boolean setPreferredNetworkType2(int type) {
    try {
      Method method = TelephonyManager.class.getMethod(
          "setPreferredNetworkType", int.class);
      method.setAccessible(true);
      return (Boolean)method.invoke(manager(), type);
    } catch (Exception e) {
      Log.e(TAG,
          "Failed to execute setPreferredNetworkType (2): " + e.toString());
      return false;
    }
  }
}
