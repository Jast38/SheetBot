package org.example;

import java.awt.Color;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

//TODOevtl: Iterate through COMMANDS, invoke corresponding Method when Command
// recognized using  String Split and Reflection API
//TODO: add argument to SheetParser to specify Day
public class Listener extends ListenerAdapter {
  private static final ArrayList<String> COMMANDS = new
      ArrayList<>(Arrays.asList(
      "help",
      "print",
      "admin"));
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
        reactToSheet(event, author, msg);
      }
    }
  }

  //TODO: add to database

  // Database: id(auto) -- name -- spreadsheetId -- author -- guild -- date
  private void reactToSheet(final GuildMessageReceivedEvent event,
                            final User author,
                            final String messageContentRaw) {
    SheetParser parser = new SheetParser(dataManager);
    MessageBuilder toPrint = new MessageBuilder()
        .append(author.getAsMention())
        .append("\n\n");
    String spreadsheetid = ifAuthorizedReturnID(event);
    System.out.println(spreadsheetid == null ? "something went wrong"
        : spreadsheetid);

    try {
      if (spreadsheetid != null) {
        toPrint.appendCodeBlock(parser.output(spreadsheetid).getContentRaw(),
            "JSON");
        if (messageContentRaw.contains("keep")) {
          toPrint.replaceFirst("```JSON", "```JAVA");
        }
      } else {
        toPrint.append("Something went wrong. Are you authorised to view this?");
      }
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }
    event.getChannel().sendMessage(toPrint.build())
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
    String string = COMMANDS.stream()
        .map(command -> "` !sheet " + command + "`" + "\n\n")
        .collect(Collectors.joining());
    eb.addField("",
        string, false);
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

  private String ifAuthorizedReturnID(GuildMessageReceivedEvent event) {
    String author = event.getAuthor().getName();
    String guild = event.getGuild().getName();
    String msg = event.getMessage().getContentRaw();
    String[] splitMessage = msg.split(" ");
    String spreadsheetName = splitMessage[2].toLowerCase();
    String spreadsheetid = null;

    try (ResultSet sqlSet = dataManager.getSqlRow(spreadsheetName)){
            if (sqlSet.next()) {
              if (
              Objects.equals(author, sqlSet.getString("author"))
              || Objects.equals(guild, sqlSet.getString("guild"))
              ) {
                spreadsheetid =  sqlSet.getString("spreadsheetid");
              }
            }
    } catch (SQLException | IOException e) {
      System.out.println(e.getMessage());
      return null;
    }
    return spreadsheetid;
  }
}


