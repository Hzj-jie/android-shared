package org.gemini.shared;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;

public final class TsvReader implements AutoCloseable, Closeable {
  private final BufferedReader reader;

  public TsvReader(BufferedReader reader) {
    Preconditions.isNotNull(reader);
    this.reader = reader;
  }

  public TsvReader(String file) {
    this(fromFile(file));
  }

  private static BufferedReader fromFile(String file) {
    try {
      return new BufferedReader(new FileReader(file));
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public String[] readLine() {
    String line;
    while (true) {
      try {
        line = reader.readLine();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      if (line == null) {
        return null;
      }
      line = line.trim();
      if (!line.startsWith("#")) {
        break;
      }
    }
    return line.split("\t");
  }

  @Override
  public void close() {
    try {
      reader.close();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
