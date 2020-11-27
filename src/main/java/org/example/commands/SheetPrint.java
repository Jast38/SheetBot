package org.example.commands;

import com.google.common.collect.ImmutableMap;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.example.DataManager;
import org.example.SheetParser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author Jannick Stuby
 * @version 11/24/20
 */
public class SheetPrint {
  private final DataManager dataManager;
  private final GuildMessageReceivedEvent event;
  private static final Map<String, String> DAYS
      = ImmutableMap.of(
      "montag", "B4:E17",
      "dienstag", "F4:I17",
      "mittwoch","J4:M17",
      "donnerstag", "N4:Q17",
      "freitag", "R4:U17");
  private final String dayToGet;

  public SheetPrint(GuildMessageReceivedEvent event, DataManager dataManager,
                    String day) {
    this.dataManager = dataManager;
    this.event = event;
    this.dayToGet = day;
  }

  public void react() {
    String spreadsheetId = ifAuthorizedReturnId(event);
    String range = computeRange();
    if (spreadsheetId != null) {
      getSpreadsheet(spreadsheetId, range);
    } else {
      System.out.println("something went wrong, spreadsheetId == null");
      event.getChannel().sendMessage(event.getAuthor().getAsMention())
          .append("\n\n")
          .append("Something went wrong. Are you authorised to view this" +
              " spreadsheet?")
          .queue();
    }
  }

  private String computeRange() {
    LocalDate date = LocalDate.now();
    int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    String dayRange = DAYS.get(dayToGet);
    if (dayRange.equals(null)) {
      throw new NoSuchElementException();
    } else {
      return "KW " + week + "!" + dayRange;
    }
  }

  private void getSpreadsheet(String spreadsheetId, String range) {
    SheetParser parser = new SheetParser(dataManager, range);
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
