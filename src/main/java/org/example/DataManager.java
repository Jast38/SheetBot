package org.example;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

//TODO: implement database to save Spreadsheet IDs with corresponding names and
// messages/logs
public class DataManager {
  private InputStream getFile(final String resourcePath)
      throws FileNotFoundException {
    InputStream in = DataManager.class.getResourceAsStream(resourcePath);
    if (in == null) {
      throw new FileNotFoundException("File not found "
      + resourcePath);
    }
    return in;
  }

  /**
   * Get discord token from local file.
   *
   * @param resourcePath path to credential file in resource folder
   * @return discord Token as String
   * @throws IOException if IO fails
   */
  public String discordToken(final String resourcePath)
      throws IOException {

    return new BufferedReader(
        new InputStreamReader(Objects.requireNonNull(getFile(resourcePath)),
            StandardCharsets.UTF_8))
        .readLine();
  }

  /**
   * Get Google credentials from local file.
   *
   * @param resourcePath path to credential file in resource folder
   * @param scopes scopes to get credentials for
   * @return Google credentials for HttpCredentialsAdapter
   * @throws IOException if IO fails
   */
  public GoogleCredentials getCredentials(final String resourcePath,
                                          final List<String> scopes)
      throws IOException {
    return GoogleCredentials
        .fromStream(Objects.requireNonNull(getFile(resourcePath)))
        .createScoped(scopes);
  }
}

