package org.gemini.shared;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

public final class LogCollector {
  private static final String TAG = Debugging.createTag("LogCollector");
  private static boolean started = false;
  private LogCollector() {}

  public static void startInDebug(Context context) {
    if (Debugging.isDebugBuild(context)) {
      start(context);
    }
  }

  public static void start(Context context) {
    Preconditions.isNotNull(context);
    if (started) return;
    started = true;
    final File dir = context.getExternalFilesDir(null);
    if (dir == null) {
      Log.e(TAG, "No external files directory available.");
      return;
    }

    final File file = new File(dir, "log-collector.txt");

    PrintWriter newWriter = null;
    try {
      newWriter = new PrintWriter(new FileWriter(file, true), true);
    } catch (Exception e) {
      Log.e(TAG, "Failed to create writer of " + file +
                 ": " + Debugging.toString(e));
      return;
    }

    final PrintWriter writer = newWriter;
    new Thread() {
      @Override
      public void run() {
        Process process = null;
        try {
          process = Runtime.getRuntime().exec("logcat");
        } catch (Exception e) {
          Log.e(TAG, "Failed to execute logcat: " + Debugging.toString(e));
          return;
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()), 1);
        try {
          String line = null;
          while ((line = reader.readLine()) != null) {
            writer.println(line);
          }
        } catch (IOException e) {
          Log.e(TAG, "Failed to collect log: " + Debugging.toString(e));
        }
      }
    }.start();
  }
}
