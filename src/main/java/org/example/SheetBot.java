package org.example;

import java.io.IOException;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.example.commands.SheetHandler;

public class SheetBot {
  private static final String CREDS_DISCORD_PATH = "/creds_discord.txt";
  private final String token;
  private final DataManager dataManager;

  /**
   * Constructor of class.
   *
   * @param manager dataManager to access local Storage
   * @throws IOException if IO fails
   */
  public SheetBot(final DataManager manager) throws IOException {
    this.dataManager = manager;
    this.token = dataManager.discordToken(CREDS_DISCORD_PATH);
  }

  public void startBot() {
    JDABuilder builder = JDABuilder.createDefault(token);
//    Listener listen = new Listener(dataManager);
    SheetHandler sheetHandler = new SheetHandler(dataManager);
    JDA jda = null;

    //builder.addEventListeners(new Command())
    builder.addEventListeners(sheetHandler);
    builder.setActivity(Activity.listening("!sheet help"));
    try {
      jda = builder.build();
    } catch (LoginException e) {
      e.printStackTrace();
    }
    try {
      assert jda != null;
      jda.awaitReady();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
