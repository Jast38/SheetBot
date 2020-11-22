package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

//TODO: Iterate through COMMANDS, invoke corresponding Method when Command
// recognized using  String Split and Reflection API
//TODO: add argument to SheetParser to specify Week
public class Listener extends ListenerAdapter {
  private static final ArrayList<String> COMMANDS = new
      ArrayList<>(Arrays.asList(
      "help",
      "print",
      "admin"));
  private static final String ADRIAN =
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
  public void onMessageReceived(final MessageReceivedEvent event) {
    Message message = event.getMessage();
    String msg = message.getContentRaw();
    User author = event.getAuthor();

    System.out.println("We received a message from "
        + author.getName() + ": "
        + message.getContentDisplay()
    );
    if (msg.startsWith("!sheet")) {
      if (msg.contains("help")) {
        reactToHelp(event);
      } else if (msg.contains("admin")) {
        reactToAdmin();
      } else {
        reactToSheet(event, author, msg);
      }
    }
  }

  private void reactToSheet(final MessageReceivedEvent event, final User author,
                            final String messageContentRaw) {

    if (messageContentRaw.contains("print")) {
      //TODO: add to database, check for Validity
      SheetParser parser = new SheetParser(dataManager);
      MessageBuilder toPrint = new MessageBuilder()
          .append(author.getAsMention())
          .append("\n\n");
      try {
        if (messageContentRaw.contains("Adrian")) {
          toPrint.append(parser.output(ADRIAN).getContentRaw());
        } else if (messageContentRaw.contains("Benny")) {
          toPrint.append(parser.output(BENNY).getContentRaw());
        } else {
          toPrint.append("No SpreadsheetId for this name. Typo?");
        }
      } catch (GeneralSecurityException | IOException e) {
        e.printStackTrace();
      }
      event.getChannel().sendMessage(toPrint.build())
          .queue();
    }
  }

  //TODO: implement admin features: kick members, add remove spreadsheet
  // stuff with dataManager
  private void reactToAdmin() {
  }

  private void reactToHelp(final MessageReceivedEvent event) {
    MessageBuilder commandListBuilder = new MessageBuilder();
    commandListBuilder.append("```Here is a list of my commands: \n\n")
        .append("All commands must be preceded by '!sheet ' \n\n");

    for (String command
        : COMMANDS) {
      commandListBuilder.append(command).append("\n");
    }
    commandListBuilder.append("```");
    event.getChannel()
        .sendMessage(commandListBuilder.build()).queue();
  }

}

