package org.example;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ApiWrapper {
  private static final String APPLICATION_NAME = "Sheets Bot";
  private static final String CREDENTIALS_FILE_PATH = "/creds_google.json";
  private static final JsonFactory JSON_FACTORY = JacksonFactory
      .getDefaultInstance();

  private final String spreadsheetId;
  private final String range;
  private static DataManager dataManager;

  /**
   * Constructor of class.
   *
   * @param argSpreadsheetId Id of spreadsheet to get data from
   * @param argRange         range of cells to get
   * @param manager          data manager to access local data
   */
  public ApiWrapper(final String argSpreadsheetId, final String argRange,
                    final DataManager manager) {
    this.spreadsheetId = argSpreadsheetId;
    this.range = argRange;
    dataManager = manager;
  }

  private static final List<String> SCOPES =
      Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);


  private static HttpRequestInitializer authExplicit() throws IOException {
    return new HttpCredentialsAdapter(dataManager
        .getCredentials(CREDENTIALS_FILE_PATH, SCOPES));
  }

  /**
   * Get cells specified by range of spreadsheet using spreadsheetId.
   *
   * @return 2x2 Array of rows x columns
   * @throws GeneralSecurityException when authentication
   *                                  fails and http transport doesn't work
   * @throws IOException              IO error
   */
  public List<List<Object>> getCells() throws
      GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport
        .newTrustedTransport();

    Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY,
        authExplicit())
        .setApplicationName(APPLICATION_NAME)
        .build();

    ValueRange response = service.spreadsheets().values()
        .get(spreadsheetId, range)
        .execute();

    return response.getValues();
  }
}
