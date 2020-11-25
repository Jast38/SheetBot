package org.example.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.DataManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Jannick Stuby
 * @version 11/24/20
 */
public class SheetHandler extends ListenerAdapter {
  private final DataManager dataManager;

  public SheetHandler(final DataManager manager) {
    this.dataManager = manager;
  }

  @Override
  public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
    Message message = event.getMessage();
    Guild guild = event.getGuild();
    String msg = message.getContentRaw();
    User author = event.getAuthor();
    Member member = event.getMember();

    System.out.println("We received a message from "
        + author.getName() + ": "
        + message.getContentDisplay()
        + " in guild "
        + guild.getName()
    );
    if (!msg.contains("```JAVA") && Objects.equals(member,
        guild.getSelfMember())) {
      deleteAfterDelay(message, 1);
    } else if (msg.startsWith("!sheet")) {
      deleteAfterDelay(message, 0);
      if (msg.contains("help")) {
        SheetHelp helper = new SheetHelp(event);
        helper.react();
      } else if (msg.contains("print")) {
        SheetPrint printer = new SheetPrint(event, dataManager);
        printer.react();
      }
    }
  }
  private void deleteAfterDelay(final Message message, final int delayTime) {
    TimerTask delete = new TimerTask() {
      @Override
      public void run() {
        message.delete().queue();
        System.out.println("Message deleted");
      }
    };
    //delete after 2 minutes
    Timer timer = new Timer();
    if (delayTime > 0){
      LocalDateTime date = LocalDateTime.now().plusMinutes(delayTime);
      Date delay = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
      timer.schedule(delete, delay);
      System.out.println("Deletion scheduled for " + delay);
    } else {
      timer.schedule(delete, 2500L);
    }
  }
}
