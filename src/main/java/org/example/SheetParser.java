package org.example;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class SheetParser {

    //TODO: give spreadsheetId as argument to Listener, add using constructor
    public Message output(String sheetId) throws GeneralSecurityException, IOException {
        MessageBuilder returnMessageBuilder = new MessageBuilder();
        //Adrian privat
        //"1qgJGlq0nU3WNMgpm9RtnUJopTEWHPQd6ACr4-IteuQY",
        //Benny
        //"1-xniicYlY5jNJiqv6qGSTVO40W_cpme9qZOorfQb1AE"

        ApiWrapper sheety = new ApiWrapper(
            sheetId,
            "KW 48!B4:E17");

        List<List<Object>> values = sheety.getCells();

        if (values == null || values.isEmpty()){
            return new MessageBuilder()
                .append("No data recognized, is the spreadsheet empty?")
                .build();
        } else {
            for (List row : values) {
                for (int i = 0; i < row.size(); i++) {
                    //System.out.print(row.get(i) + " ");
                    returnMessageBuilder.append(row.get(i)).append(" ");
                    if (Objects.equals(row.get(i), "")) {
                        //System.out.print("|         ");
                        returnMessageBuilder.append("|         ");
                    }
                    if (i == row.size() - 1) {
                        //System.out.print("|\n");
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
