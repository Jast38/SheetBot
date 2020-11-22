package org.example;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class SheetBot {
  private final String token;

  public SheetBot(String token) {
    this.token = token;
    startBot();
  }

  private void startBot() {
    JDABuilder builder = JDABuilder.createDefault(token);
    Listener listen = new Listener();
    JDA jda = null;

    builder.addEventListeners(listen);
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
