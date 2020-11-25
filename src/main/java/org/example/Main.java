package org.example;

import java.io.IOException;

//TODO: try catch for every exception possibility
public class Main {

  /**
   * Main method, start bot.
   *
   * @param args Arguments
   * @throws IOException when Reader cant get file
   */
  public static void main(final String[] args) throws IOException {
    DataManager dataManager = new DataManager();
    SheetBot bot = new SheetBot(dataManager);
    bot.startBot();
  }
}
