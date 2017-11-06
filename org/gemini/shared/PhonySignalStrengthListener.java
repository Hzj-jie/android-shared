package org.gemini.shared;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public final class PhonySignalStrengthListener extends PhoneStateListener {
  public static final int MIN_LEVEL = 0;
  public static final int MAX_LEVEL = 4;
  private static final String TAG = "Gemini.PhonySignal";
  private final Context context;
  private final Event.Raisable<Integer> onSignalStrength;

  @SuppressWarnings("deprecation")
  @TargetApi(7)
  public PhonySignalStrengthListener(Context context) {
    assert(context != null);
    this.context = context;
    onSignalStrength = new Event.Raisable<>();
    manager().listen(this,
                     PhoneStateListener.LISTEN_SIGNAL_STRENGTH |
                     PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
  }

  public TelephonyManager manager() {
    return (TelephonyManager)
        context.getSystemService(Context.TELEPHONY_SERVICE);
  }

  public Event<Integer> onSignalStrength() {
    return onSignalStrength;
  }

  public void stop() {
    manager().listen(this, PhoneStateListener.LISTEN_NONE);
  }

  @Override
  public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    if (signalStrength == null) return;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1) return;
    Log.d(TAG, "Received signal strength in SignalStrength " +
               signalStrength.toString());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      raise(signalStrength.getLevel());
    /*
    } else if (...) {
      if (signalStrength.isGsm()) {
        if (signalStrength.getLteLevel() != 0) {
          raise(signalStrength.getLteLevel());
        } else {
          raise(signalStrength.getGsmLevel());
        }
      } else {
        if (signalStrength.getCdmaLevel() != 0) {
          raise(signalStrength.getCdmaLevel());
        } else {
          raise(signalStrength.getEvdoLevel());
        }
      }
    */
    } else {
      if (signalStrength.isGsm()) {
        raise(asuToLevel(signalStrength.getGsmSignalStrength()));
        /*
        if (signalStrength.getLteSignalStrength() != 0) {
          raise(asuToLevel(signalStrength.getLteSignalStrength()));
        } else {
          raise(asuToLevel(signalStrength.getGsmSignalStrength()));
        }
        */
      } else {
        if (signalStrength.getCdmaDbm() < 0) {
          raise(dbmToLevel(signalStrength.getCdmaDbm()));
        } else {
          raise(dbmToLevel(signalStrength.getEvdoDbm()));
        }
      }
    }
    super.onSignalStrengthsChanged(signalStrength);
  }

  @Override
  @SuppressWarnings("deprecation")
  @TargetApi(7)
  public void onSignalStrengthChanged(int asu) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1) {
      Log.d(TAG, "Received signal strength in asu " + asu);
      raise(asuToLevel(asu));
    }
    super.onSignalStrengthChanged(asu);
  }

  private void raise(int level) {
    onSignalStrength.raise(level);
  }

  private static int asuToLevel(int asu) {
    return (asu <= 2 ? 0 :
           (asu <= 4 ? 1 :
           (asu <= 7 ? 2 :
           (asu <= 11 ? 3 :
           (asu <= 31 ? 4 : 0)
           ))));
  }

  private static int dbmToAsu(int dbm) {
    return (int)Math.floor((dbm + 113) / 2);
  }

  private static int dbmToLevel(int dbm) {
    return asuToLevel(dbmToAsu(dbm));
  }
}
