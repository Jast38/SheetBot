package org.example.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.DataManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
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
    if (msg.contains("```JSON") && Objects.equals(member,
        guild.getSelfMember())) {
      changeAfterDelay(message, 1, "edit");
    }else if (Objects.equals(member, guild.getSelfMember()) &&
        !msg.contains("```JAVA")) {
      changeAfterDelay(message, 1, "delete");
    } else if (msg.startsWith("!sheet")) {
      changeAfterDelay(message, 0, "delete");
      String[] msgArray = msg.split(" ");
      if (msgArray.length == 1) {
        message.getChannel().sendMessage("You have to specify what to do")
            .queue();
      } else {
        if (msgArray[1].equalsIgnoreCase("help")) {
          SheetHelp helper = new SheetHelp(event);
          helper.react();
        } else if (msgArray[1].equalsIgnoreCase("print")) {
          SheetPrint printer;
          if ((msgArray.length >= 4) && !(msgArray[3]
              .equalsIgnoreCase("keep"))) {
            printer = new SheetPrint(event,
                dataManager, msgArray[3].toLowerCase());
          } else {
            LocalDate date = LocalDate.now();
            String weekDayGerman = date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.GERMAN);
            printer = new SheetPrint(event, dataManager,
                weekDayGerman.toLowerCase());
          }
          printer.react();
        }
      }
    }
  }
  private void changeAfterDelay(final Message message, final int delayTime,
                                String operation) {
    TimerTask delete = new TimerTask() {
      @Override
      public void run() {
        message.delete().queue();
        System.out.println("Message deleted");
      }
    };
    TimerTask edit = new TimerTask() {
      @Override
      public void run() {
        Message ms = new MessageBuilder()
            .append("Hier stand ein Stundenplan und wurde planmäßig gelöscht")
            .build();
        message.editMessage(ms).queue();
        System.out.println("message edited");
      }
    };
    //delete after 2 minutes
    Timer timer = new Timer();
    if (delayTime > 0){
      LocalDateTime date = LocalDateTime.now().plusMinutes(delayTime);
      Date delay = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
      if (operation.equals("edit")) {
        timer.schedule(edit, delay);
      } else {
        timer.schedule(delete, delay);
      }
      System.out.println("Deletion scheduled for " + delay);
    } else {
      timer.schedule(delete, 1500L);
    }
  }
}
