package org.example.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.example.DataManager;
import org.example.SheetParser;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author Jannick Stuby
 * @version 11/24/20
 */
public class SheetPrint {
  private final DataManager dataManager;
  private final GuildMessageReceivedEvent event;

  public SheetPrint(GuildMessageReceivedEvent event, DataManager dataManager) {
    this.dataManager = dataManager;
    this.event = event;
  }

  public void react() {
    String spreadsheetId = ifAuthorizedReturnId(event);
    if (spreadsheetId != null) {
      getSpreadsheet(spreadsheetId);
    } else{
      System.out.println("something went wrong, spreadsheetId == null");
      event.getChannel().sendMessage(event.getAuthor().getAsMention())
          .append("\n\n")
          .append("Something went wrong. Are you authorised to view this" +
              " spreadsheet?")
          .queue();
    }
  }

  private void getSpreadsheet(String spreadsheetId) {
    SheetParser parser = new SheetParser(dataManager);
    MessageBuilder toPrint = new MessageBuilder()
        .append(event.getAuthor().getAsMention())
        .append("\n\n");

    try {
      toPrint.appendCodeBlock(parser.output(spreadsheetId).getContentRaw(),
          "JSON");
      if (event.getMessage().getContentRaw().contains("keep")) {
        toPrint.replaceFirst("```JSON", "```JAVA");
      }
      event.getChannel().sendMessage(toPrint.build()).queue();
    } catch (GeneralSecurityException | IOException e) {
      event.getChannel().sendMessage("Sorry, something broke").queue();
      e.printStackTrace();
    }
  }

  private String ifAuthorizedReturnId(final GuildMessageReceivedEvent event) {
    String author = event.getAuthor().getName();
    String guild = event.getGuild().getName();
    String msg = event.getMessage().getContentRaw();
    String[] splitMessage = msg.split(" ");
    String spreadsheetName = splitMessage[2].toLowerCase();
    String spreadsheetid = null;

    try (ResultSet sqlSet = dataManager.getSqlRow(spreadsheetName)) {
      if (sqlSet.next()) {
        if (
            Objects.equals(author, sqlSet.getString("author"))
                || Objects.equals(guild, sqlSet.getString("guild"))
        ) {
          spreadsheetid = sqlSet.getString("spreadsheetid");
        }
      }
    } catch (SQLException | IOException e) {
      System.out.println(e.getMessage());
      return null;
    }
    return spreadsheetid;
  }
}
