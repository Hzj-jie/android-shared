package org.gemini.shared;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;

public final class Formatter {
  public static String envs(Map<String, String> envs, String format, String... args) {
    Preconditions.isNotNull(format);
    if (args == null || args.length == 0) {
      return format;
    }
    Preconditions.isNotNull(envs);
    ArrayList<String> envArgs = new ArrayList<>();
    for (String arg : args) {
      String env = envs.get(arg);
      Preconditions.isNotNull(env, "unknown environment " + arg);
      envArgs.add(env);
    }
    return String.format(format, envArgs.toArray(new Object[0]));
  }

  public static String csvEnvs(Map<String, String> envs, String line) {
    Preconditions.isNotNull(line);
    String[] args = line.split(",");
    return envs(envs, args[0], Arrays.copyOfRange(args, 1, args.length));
  }

  private Formatter() {}
}
