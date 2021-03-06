package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

public class SheetParser {
  private final DataManager dataManager;
  private final String range;

  public SheetParser(final DataManager manager, final String range) {
    this.dataManager = manager;
    this.range = range;
  }

  //TODO: give spreadsheetId as argument to Listener, add using constructor

  /**
   * Gets cells of speficied sheetID and formats output.
   *
   * @param sheetId Id of sheet to parse
   * @return Message object to be sent to discord channel
   * @throws GeneralSecurityException when auth fails
   * @throws IOException IO error
   */
  public Message output(final String sheetId) throws GeneralSecurityException,
      IOException {
    MessageBuilder returnMessageBuilder = new MessageBuilder();

    ApiWrapper sheety = new ApiWrapper(
        sheetId,
        range,
        dataManager);

    List<List<Object>> values = sheety.getCells();

    if ((values == null) || values.isEmpty()) {
      return new MessageBuilder()
          .append("No data recognized, is the spreadsheet empty?")
          .build();
    } else {
      for (List row : values) {
        for (int i = 0; i < row.size(); i++) {
          returnMessageBuilder.append(row.get(i)).append(" ");
          if (Objects.equals(row.get(i), "")) {
            returnMessageBuilder.append("|         ");
          }
          if (i == row.size() - 1) {
            returnMessageBuilder.append("|\n");
          }
        }
      }
      returnMessageBuilder.append("\n\n")
          .append("Last updated: ")
          .append(LocalDateTime.now()
              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    return returnMessageBuilder.build();
  }
}
