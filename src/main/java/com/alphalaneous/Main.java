package com.alphalaneous;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        GloballyBlockedID.load();
        System.out.println("> GloballyBlockedIDs Loaded");
        ReportedID.load();
        System.out.println("> ReportedIDs Loaded");
        GloballyBlockedUser.load();
        System.out.println("> GloballyBlockedUsers Loaded");
        try {
            YTMessaging.setCredential();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BotServer.start();
    }
}
