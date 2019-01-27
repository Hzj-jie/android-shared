package org.gemini.shared;

import android.app.Activity;
import android.os.Process;

public class SelfDestroyActivity extends Activity {
  @Override
  protected final void onDestroy() {
      super.onDestroy();
      Process.killProcess(Process.myPid());
      System.exit(0);
  }
}
