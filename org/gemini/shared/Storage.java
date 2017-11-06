package org.gemini.shared;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import java.io.File;

public final class Storage {
  private final Context context;

  public Storage(Context context) {
    assert(context != null);
    this.context = context;
  }

  public static boolean isEmulated(File f) {
    if (f == null) return false;
    String path = f.getAbsolutePath();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
        Environment.isExternalStorageEmulated(f)) {
      return true;
    }

    // We can only guess.
    if (path.indexOf("/emulated/") != -1) return true;

    return false;
  }

  // A.K.A. the "internal" "external storage", or emulated storage, backed by
  // onboard storage chips. This function never returns null or empty string,
  // unless OS APIs return null or empty string.
  public String buildInSharedStoragePath() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      File[] candidates = context.getExternalFilesDirs(null);
      for (File f : candidates) {
        if (!isEmulated(f)) continue;

        String path = f.getAbsolutePath();
        int endIndex = path.indexOf("/Android/data");
        if (endIndex == -1) continue;

        // Include the last '/'.
        return path.substring(0, endIndex + 1);
      }
    }

    String p = Environment.getExternalStorageDirectory().getAbsolutePath();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      if (Environment.isExternalStorageEmulated()) return p;
    }

    String e = System.getenv("EXTERNAL_STORAGE");
    if (e != null) return e;
    return p;
  }

  // A.K.A. the "external" "external storage", or real sd card, backed by sd
  // card slot and sd card. This function returns null if sd card is not present
  // on the system.
  public String externalSharedStoragePath() {
    String e = System.getenv("SECONDARY_STORAGE");
    if (e != null) return e;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      File[] candidates = context.getExternalFilesDirs(null);
      for (File f : candidates) {
        if (isEmulated(f)) continue;

        String path = f.getAbsolutePath();
        int endIndex = path.indexOf("/Android/data");
        if (endIndex == -1) continue;

        // Include the last '/'.
        return path.substring(0, endIndex + 1);
      }
    }
    return null;
  }
}
