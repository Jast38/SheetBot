package org.example;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main{
    //Discord token
    private static final String TOKEN = "Nzc5MDUwMTAyNjkzNjkxNDYz.X7a48g.h1R-sd_QqLZsG5tbCOF6IqVGaVs";

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        SheetBot bot = new SheetBot(TOKEN);
    }
}
