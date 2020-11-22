package org.example;

import java.io.FileNotFoundException;
import java.io.InputStream;

//TODO: implement database to save Spreadsheet IDs with corresponding names and
// messages/logs
public class DataManager {
  public InputStream getFile(final String resourcePath) throws FileNotFoundException {
    InputStream in = DataManager.class.getResourceAsStream(resourcePath);
    if (in == null) {
      throw new FileNotFoundException("File not found "
      + resourcePath);
    }
    return null;
  }
}

