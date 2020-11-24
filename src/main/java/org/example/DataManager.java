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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

//TODO: connect to db only on call, sanitize using prepared statements
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
   * @param scopes       scopes to get credentials for
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
      throws IOException {
    String[] returnString = new String[2];
    DataInputStream dataStream = new DataInputStream(getFile(resourcePath));
    BufferedReader buffer = new BufferedReader(
        new InputStreamReader(dataStream));

    for (int i = 0; i < 2; i++) {
      returnString[i] = buffer.readLine();
    }
    return returnString;
  }

  private Connection connectSqlDatabase() throws IOException {
    String dbResourcePath = "/creds_db.txt";
    String[] credentials = getDbCreds(dbResourcePath);
    try {
      return DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/sheetbot", credentials[0],
          credentials[1]);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  //TODO: implement String verification to prevent SQL Injection
  public String getSingleSqlValue(final String columnId,
                                  final String primaryKey)
      throws SQLException, IOException {
    Connection conn = null;
    PreparedStatement stmt = null;
    String returnString = "";

    try {
      conn = Objects.requireNonNull(connectSqlDatabase());
      stmt = conn.prepareStatement("SELECT " + columnId
          + " from sheets where name = ?");
      stmt.setString(1, primaryKey);
      ResultSet resultSet = stmt.executeQuery();
      if (resultSet.next()) {
        returnString = resultSet.getString(1);
      }
    } finally {
      closeSqlConnection(conn, stmt);
    }
    return returnString;
  }

  public ResultSet getSqlRow(final String primaryKey) throws IOException,
      SQLException {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet returnSet;
    try {
      conn = Objects.requireNonNull(connectSqlDatabase());
      if (primaryKey.equals("all")) {
        returnSet = conn.createStatement()
            .executeQuery("select * from sheets");
      } else {
        stmt = conn.prepareStatement("SELECT * from sheets where name = ?");
        stmt.setString(1, primaryKey);
        returnSet = stmt.executeQuery();
      }
    } finally {
      closeSqlConnection(conn, stmt);
    }
    return returnSet;
  }

  public void insertSqlEntry(final String name, final String spreadsheetId,
                             final String author, final String guild)
      throws SQLException, IOException {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      conn = Objects.requireNonNull(connectSqlDatabase());
      stmt = conn.prepareStatement("insert into sheets "
          + "(name, spreadsheetid, author, guild, date ) "
          + "values (?,?,?,?,NOW())");
      stmt.setString(1, name);
      stmt.setString(2, spreadsheetId);
      stmt.setString(3, author);
      stmt.setString(4, guild);
      stmt.executeUpdate();
    } finally {
      closeSqlConnection(conn, stmt);
    }
  }

  private void closeSqlConnection(final Connection conn,
                                  final PreparedStatement stmt) {
    try {
      if (stmt != null) {
        stmt.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      if (conn != null) {
        conn.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
