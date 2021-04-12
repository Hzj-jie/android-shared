package org.gemini.shared;

public final class Ints {
  public static int parseOr(String i, int defaultValue) {
    try {
      return Integer.parseInt(i);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private Ints() {}
}
