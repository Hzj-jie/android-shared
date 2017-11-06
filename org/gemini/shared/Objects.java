package org.gemini.shared;

public final class Objects {
  private Objects() {}

  public static boolean equals(Object first, Object second) {
    if (first == null && second == null) return true;
    if (first == null || second == null) return false;
    return first.equals(second);
  }
}
