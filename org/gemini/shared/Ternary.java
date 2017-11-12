package org.gemini.shared;

public final class Ternary {
  public static final int UNKNOWN = 0;
  public static final int TRUE = 1;
  public static final int FALSE = 2;
  private int value = UNKNOWN;

  public Ternary() {
    this(UNKNOWN);
  }

  public Ternary(boolean v) {
    set(v);
  }

  private Ternary(int value) {
    this.value = value;
  }

  public static Ternary True() {
    return new Ternary(TRUE);
  }

  public static Ternary False() {
    return new Ternary(FALSE);
  }

  public static Ternary Unknown() {
    return new Ternary(UNKNOWN);
  }

  public boolean isTrue() {
    return value == TRUE;
  }

  public boolean isFalse() {
    return value == FALSE;
  }

  public boolean isUnknown() {
    return value == UNKNOWN;
  }

  public void set(boolean v) {
    value = (v ? TRUE : FALSE);
  }
}
