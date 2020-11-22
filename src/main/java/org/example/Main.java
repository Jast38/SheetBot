package org.example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Main {
  private static final String CREDS_DISCORD_PATH = "/creds_discord.txt";

  /**
   * Main method, start bot.
   *
   * @param args Arguments
   * @throws IOException when Reader cant get file
   */
  public static void main(String[] args) throws IOException {
    InputStream in = Main.class.getResourceAsStream(CREDS_DISCORD_PATH);
    if (in == null) {
      throw new FileNotFoundException("File not found "
          + CREDS_DISCORD_PATH);
    }
    String token = new BufferedReader(
        new InputStreamReader(in, StandardCharsets.UTF_8))
        .readLine();
    SheetBot bot = new SheetBot(token);
  }
}