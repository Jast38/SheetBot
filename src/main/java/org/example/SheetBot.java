package org.example;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.example.commands.AdminHandler;
import org.example.commands.SheetHandler;

public class SheetBot {
  private static final String CREDS_DISCORD_PATH = "/creds_discord.txt";
  private final String token;
  private final DataManager dataManager;

  //private final static int ONCE_PER_DAY = 1000*60*60*24;
  private final static int SIX_PM = 18;
  private final static int ZERO = 0;

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
      jda.addEventListener(new AdminHandler(jda, dataManager));
      startTask(jda);
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

  private void startTask(JDA jda) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
    ZonedDateTime nextRun = now.withHour(SIX_PM).withMinute(ZERO).withSecond(ZERO);
    if (now.compareTo(nextRun) > 0) {
      nextRun = nextRun.plusDays(1);
    }

    Duration duration = Duration.between(now, nextRun);
    long initialDelay = duration.getSeconds();
    PrintAtSix print = new PrintAtSix(dataManager, "medtech", jda);

    ScheduledExecutorService scheduler = Executors
        .newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(
        print,
        initialDelay,
        TimeUnit.DAYS.toSeconds(1),
        TimeUnit.SECONDS);
  }
}
