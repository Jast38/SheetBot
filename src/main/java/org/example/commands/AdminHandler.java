package org.example.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.DataManager;

import java.util.List;

/**
 * //TODO: add admin commands
 * @author Jannick Stuby
 * @version 11/24/20
 */
public class AdminHandler extends ListenerAdapter {
  private final JDA jda;
  private final DataManager dataManager;
  public AdminHandler(final JDA jda, DataManager dataManager) {
    this.jda = jda;
    this.dataManager = dataManager;
  }
  @Override
  public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
    Message message = event.getMessage();
    String msg = message.getContentRaw();

    if (msg.startsWith("!admin")) {
      String[] msgArray = msg.split(" ");
      if (msgArray.length == 1) {
        message.getChannel().sendMessage("What to administrate?").queue();
      } else {
        if (msgArray[1].equalsIgnoreCase("printGuilds")) {
          printGuilds();
        }
      }
    }
  }

  private void printGuilds() {
    List<Guild> guilds = jda.getGuilds();

    for (Guild g:
         guilds) {
      List<GuildChannel> channels = g.getChannels();
      System.out.println(
          "\n\nname : "
          + g.getName() + " \n"
          + "id : "
          + g.getId() + " \n"
          + "channels: \n"
      );
      for (GuildChannel ch:
           channels) {
        System.out.println(ch.getName() + " " + ch.getId());
      }
    }
  }
}
