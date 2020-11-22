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
import com.google.auth.oauth2.GoogleCredentials;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ApiWrapper {
    private static final String APPLICATION_NAME = "Sheets Bot";
    private static final String CREDENTIALS_FILE_PATH = "/creds_service.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory
        .getDefaultInstance();

    private final String spreadsheetId;
    private final String range;

    public ApiWrapper(String spreadsheetId, String range) {
        this.spreadsheetId = spreadsheetId;
        this.range = range;
    }

    private static final List<String> SCOPES =
        Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);



    private static HttpRequestInitializer authExplicit() throws IOException {
        InputStream in = ApiWrapper.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: "
                + CREDENTIALS_FILE_PATH);
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
            .createScoped(SCOPES);

        return new HttpCredentialsAdapter(credentials);
    }

    public List<List<Object>> getCells() throws
        GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport
            .newTrustedTransport();

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY,
            authExplicit())
            .setApplicationName(APPLICATION_NAME)
            .build();

        ValueRange response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute();

        return response.getValues();
    }
}