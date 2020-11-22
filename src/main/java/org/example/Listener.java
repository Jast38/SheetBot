package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
  private static final ArrayList<String> COMMANDS = new
      ArrayList<>(Arrays.asList(
      "help",
      "print"));
  private static final String ADRIAN = "1dAEegeAXQArH_FXoSdETV8XsP4Dt7uAFaEt4-VpP0vY";
  private static final String BENNY = "1-xniicYlY5jNJiqv6qGSTVO40W_cpme9qZOorfQb1AE";
  private final DataManager dataManager;

  public Listener(DataManager manager) {
    this.dataManager = manager;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    System.out.println("We received a message from "
        + event.getAuthor().getName() + ": "
        + event.getMessage().getContentDisplay()
    );

    //TODO: Iterate through COMMANDS, invoke corresponding Method when Command
    // recognized using  String Split and Reflection API
    //TODO: add argument to SheetParser to specify Week
    String[] splitString = event.getMessage().getContentRaw().split(" ");
    if (splitString[0].equals("!sheet")) {
      if (splitString[1].equals(COMMANDS.get(0))) {
        MessageBuilder commandListBuilder = new MessageBuilder();
        commandListBuilder.append("```Here is a list of my commands: \n\n")
            .append("All commands must be preceded by '!sheet ' \n\n");

        for (String command :
            COMMANDS) {
          commandListBuilder.append(command).append("\n");
        }
        commandListBuilder.append("```");
        event.getChannel()
            .sendMessage(commandListBuilder.build()).queue();
      } else if (splitString[1].equals(COMMANDS.get(1))) {
        //TODO: add to database, check for Validity
        SheetParser parser = new SheetParser(dataManager);
        Message toPrint = null;
        try {
          if (splitString[2].equals("Adrian")) {
            toPrint = parser.output(ADRIAN);
          } else if (splitString[2].equals("Benny")) {
            toPrint = parser.output(BENNY);
          } else {
            event.getChannel()
                .sendMessage("No spreadsheet Id for this name. Typo?")
                .queue();
            return;
          }
        } catch (GeneralSecurityException | IOException e) {
          e.printStackTrace();
        }
        if (toPrint != null) {
          event.getChannel().sendMessage(toPrint).queue();
        } else {
          event.getChannel()
              .sendMessage("Something went wrong. Message Object is null")
              .queue();
        }
      }
    }
  }
}
