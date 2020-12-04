package org.example;

import com.google.common.collect.ImmutableMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;


public class PrintAtSix extends TimerTask {
  private final DataManager dataManager;
  private final String spreadSheetName;
  private final JDA jda;
  private static final Map<String, String> DAYS
      = ImmutableMap.of(
      "montag", "B4:E17",
      "dienstag", "F4:I17",
      "mittwoch","J4:M17",
      "donnerstag", "N4:Q17",
      "freitag", "R4:U17");

  private String tomorrow;
  private final static String GUILD_ID = "777564013797310464";
  private final static String CHANNEL_ID = "777567950193688588";

  public PrintAtSix(final DataManager dataManager, String sheetName, JDA jda) {
    this.dataManager = dataManager;
    this.spreadSheetName = sheetName.toLowerCase();
    this.jda = jda;
  }

  private void getSpreadsheet(String spreadsheetId, String range) {
    SheetParser parser = new SheetParser(dataManager, range);
    Guild guild = jda.getGuildById(GUILD_ID);
    MessageChannel channel = guild.getTextChannelById(CHANNEL_ID);
    MessageBuilder toPrint = new MessageBuilder()
        .append("Stundenplan für " + tomorrow)
        .append("\n\n");
    try {
      toPrint.appendCodeBlock(parser.output(spreadsheetId).getContentRaw(),
          "JAVA");
      channel.sendMessage(toPrint.build()).queue();
    } catch (GeneralSecurityException | IOException e) {
      channel.sendMessage("Sorry, something broke").queue();
      e.printStackTrace();
    }
  }

  private void print() {
    String spreadsheetId = getSpreadSheetId();
    String range = computeRangeForTomorrow();
    if (spreadsheetId != null) {
      System.out.println("got id");
      getSpreadsheet(spreadsheetId, range);
    } else {
      System.out.println("something went wrong, spreadsheetId == null");
      jda.getGuildById(GUILD_ID)
          .getTextChannelById(CHANNEL_ID)
          .sendMessage("Something went wrong. Are you authorised to view this" +
              " spreadsheet?")
          .queue();
    }
  }

  private String computeRangeForTomorrow() {
    LocalDate date = LocalDate.now();
    if (date.getDayOfWeek().getValue() == 5) {
      date = date.plusDays(3);
    } else if (date.getDayOfWeek().getValue() == 6) {
      date = date.plusDays(2);
    } else {
      date = date.plusDays(1);
    }
    int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    tomorrow = date.getDayOfWeek()
        .getDisplayName(TextStyle.FULL, Locale.GERMAN);
    String dayRange = DAYS.get(tomorrow.toLowerCase());
    return "KW " + week + "!" + dayRange;
  }

  private String getSpreadSheetId() {
    String spreadsheetId = null;

    try (ResultSet sqlSet = dataManager.getSqlRow(spreadSheetName)) {
      if (sqlSet.next()) {
        spreadsheetId = sqlSet.getString("spreadsheetid");
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
      return null;
    }
    return spreadsheetId;
  }

  @Override
  public void run() {
    System.out.println("ran daily task");
    print();
  }
}

