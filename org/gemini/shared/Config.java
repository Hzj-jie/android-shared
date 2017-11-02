package org.gemini.shared;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public final class Config {
  private static final String SEPARATOR = ":";
  private final Context context;
  private final String filename;
  private final Map<String, String> fields;

  // Returns null if no file with |name| is found or the its content is not
  // valid.
  public static Config open(Context context, String filename) {
    Map<String, String> fields = new HashMap<>();
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(context.openFileInput(filename)));
      String line = null;
      line = reader.readLine();
      while (line != null) {
        int index = line.indexOf(SEPARATOR);
        if (index == -1) {
          continue;
        }
        String key = line.substring(0, index);
        String value = line.substring(index + 1);
        if (!isValidKey(key) || !isValidValue(value)) {
          continue;
        }
        fields.put(key, value);
        line = reader.readLine();
      }
      reader.close();
      return new Config(context, filename, fields);
    } catch (Exception e) {
      // TODO
      e.printStackTrace();
      assert(false);
      return null;
    }
  }

  private Config(Context context, String filename, Map<String, String> fields) {
    assert(context != null);
    assert(filename != null && filename.length() > 0);
    assert(fields != null);
    this.context = context;
    this.filename = filename;
    this.fields = fields;
  }

  private static boolean isValidKey(String key) {
    return isValidValue(key) && !key.contains(SEPARATOR);
  }

  private static boolean isValidValue(String value) {
    return value != null &&
           value.length() > 0 &&
           !value.contains(Constants.lineSeparator());
  }

  // Returns null if |key| does not exist in |fields|.
  public String get(String key) {
    assert(isValidKey(key));
    return fields.get(key);
  }

  // Always overwrites existing value.
  public String put(String key, String value) {
    assert(isValidKey(key));
    assert(isValidValue(value));
    return fields.put(key, value);
  }

  // Flushes the content into storage.
  public void flush() {
    try {
      OutputStreamWriter writer =
          new OutputStreamWriter(context.openFileOutput(filename, 0));
      for (Map.Entry<String, String> entry : fields.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        assert(isValidKey(key));
        assert(isValidValue(value));
        String line = key + SEPARATOR + value + Constants.lineSeparator();
        writer.write(line, 0, line.length());
      }
      writer.flush();
      writer.close();
    } catch (Exception e) {
      // TODO
      e.printStackTrace();
      assert(false);
    }
  }
}
