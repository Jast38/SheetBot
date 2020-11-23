package org.example;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

//TODO: implement database to save Spreadsheet IDs with corresponding names and
// messages/logs
public class DataManager {
  private final String tableFormat =
      "(name, spreadsheetid, author, guild, date ) ";
  private final Statement stmt;

  public DataManager() throws IOException {
    stmt = connectSqlDatabase();
  }

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

  private String[] getDbCreds(final String resourcePath)
      throws IOException{
    String[] returnString = new String[2];
    DataInputStream dataStream = new DataInputStream(getFile(resourcePath));
    BufferedReader buffer = new BufferedReader(new InputStreamReader(dataStream));

    for (int i = 0; i < 2; i++) {
      returnString[i] = buffer.readLine();
    }
    return returnString;
  }

  private Statement connectSqlDatabase() throws IOException {
    String dbResourcePath = "/creds_db.txt";
    String[] credentials = getDbCreds(dbResourcePath);
    try {
      Connection conn = DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/sheetbot", credentials[0],
          credentials[1]);
      return conn.createStatement();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  //TODO: implement String verification to prevent SQL Injection
  public String getSingleSqlValue(String columnId, String primaryKey)
      throws SQLException {
    String query = "select " + columnId + " from sheets where name = '"
        + primaryKey + "'";
    String returnString = "";
    ResultSet resultSet = stmt.executeQuery(query);
    if (resultSet.next()) {
      returnString = resultSet.getString(1);
    }
    return returnString;
  }

  public ResultSet getSqlValues(String columnName) throws SQLException {
    if (columnName.equals("all")) {
      return stmt.executeQuery("select * from sheet");

    } else {
      return stmt.executeQuery("select " + columnName + " from sheet");
    }
  }

  public void insertSqlEntry(String name, String spreadsheetId, String author,
                             String guild) throws SQLException {
    String insert = "insert into sheets " + tableFormat + "values " + "('"
        + name + "', '" + spreadsheetId + "', '" + author + "', '" + guild
        +"', NOW())";

    stmt.executeQuery(insert);

  }
}

