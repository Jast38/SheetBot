package org.example;

import java.awt.Color;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

//TODO: Iterate through COMMANDS, invoke corresponding Method when Command
// recognized using  String Split and Reflection API
//TODO: add argument to SheetParser to specify Day
public class Listener extends ListenerAdapter {
  private static final ArrayList<String> COMMANDS = new
      ArrayList<>(Arrays.asList(
      "help",
      "print",
      "admin"));
  private static final String MEDTECH =
      "1dAEegeAXQArH_FXoSdETV8XsP4Dt7uAFaEt4-VpP0vY";
  private static final String BENNY =
      "1-xniicYlY5jNJiqv6qGSTVO40W_cpme9qZOorfQb1AE";
  private final DataManager dataManager;

  public Listener(final DataManager manager) {
    this.dataManager = manager;
  }

  /**
   * Triggers event when message ist received in any channel.
   *
   * @param event MessageReceivedEvent
   */
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
      deleteAfterDelay(message);
    } else if (msg.startsWith("!sheet")) {
      message.delete().queue();
      if (msg.contains("help")) {
        reactToHelp(event);
      } else if (msg.contains("admin")) {
        reactToAdmin();
      } else if (msg.contains("print")) {
        reactToSheet(event.getChannel(), author, msg);
      }
    }
  }

  //TODO: add to database, check for Validity of Name, check for guild
  // -> only allow if member of that guild added
  // Database: id(auto) -- name -- spreadsheetId -- author -- guild -- date
  private void reactToSheet(final TextChannel channel,
                            final User author,
                            final String messageContentRaw) {
    SheetParser parser = new SheetParser(dataManager);
    MessageBuilder toPrint = new MessageBuilder()
        .append(author.getAsMention())
        .append("\n\n");
    try {
      if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(
          messageContentRaw, "MedTech")) {
        toPrint.appendCodeBlock(parser.output(MEDTECH).getContentRaw(), "JSON");
      } else if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(
          messageContentRaw, "Benny")) {
        toPrint.appendCodeBlock(parser.output(BENNY).getContentRaw(), "JSON");
      } else {
        toPrint.append("No SpreadsheetId for this name. Typo?");
      }
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }
    if (messageContentRaw.contains("keep")) {
      toPrint.replaceFirst("```JSON", "```JAVA");
    }
    channel.sendMessage(toPrint.build())
        .queue();
  }

  //TODO: implement admin features: kick members, add remove spreadsheet
  // stuff with dataManager
  private void reactToAdmin() {
  }

  private void reactToHelp(final GuildMessageReceivedEvent event) {
    Color lightBlue = new Color(69, 167, 242);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("SheetBot Commands")
        .setColor(lightBlue);
    StringBuilder string = new StringBuilder();
    for (String command
        : COMMANDS) {
      string.append("` !sheet ").append(command).append("`").append("\n\n");
    }
    eb.addField("",
        string.toString(), false);
    event.getChannel()
        .sendMessage(eb.build()).queue();
  }

  private void deleteAfterDelay(Message message) {
    TimerTask delete = new TimerTask() {
      @Override
      public void run() {
        message.delete().queue();
        System.out.println("Message deleted");
      }
    };

    //delete after 2 minutes
    LocalDateTime date = LocalDateTime.now().plusMinutes(2);
    Date delay = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());

    Timer timer = new Timer();
    timer.schedule(delete, delay);
    System.out.println("Deletion scheduled for " + delay);
  }
}


