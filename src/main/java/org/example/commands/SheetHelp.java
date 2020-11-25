package org.example.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Jannick Stuby
 * @version 11/24/20
 */
public class SheetHelp {
  private final GuildMessageReceivedEvent event;
  private static final ArrayList<String> COMMANDS = new
      ArrayList<>(Arrays.asList(
      "help",
      "print",
      "admin"));
  public SheetHelp(GuildMessageReceivedEvent event) {
    this.event = event;
  }

  public void react() {
    String message = event.getMessage().getContentRaw();
    String[] messageParts = message.split(" ");

    if (messageParts.length < 3) {
      printStandardHelp();
    } else {
      reactToSpecificHelp(messageParts[3]);
    }
  }

  private void reactToSpecificHelp(String messagePart) {

  }

  private void printStandardHelp() {
    Color lightBlue = new Color(69, 167, 242);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("SheetBot Commands")
        .setColor(lightBlue);
    String string = COMMANDS.stream()
        .map(command -> "` !sheet " + command + "`" + "\n\n")
        .collect(Collectors.joining());
    eb.addField("", string, false);
    event.getChannel().sendMessage(eb.build()).queue();
  }
}
