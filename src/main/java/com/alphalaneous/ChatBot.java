package com.alphalaneous;

/*
    From a Twitch chat library I made but lost the original source too
 */

import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public abstract class ChatBot {
    private final String channel;
    private WebSocketClient chatReader;

    public ChatBot(final String channel) {
        this.channel = channel;

        try {
            this.chatReader = new WebSocketClient(new URI("wss://irc-ws.chat.twitch.tv:443")) {
                public void onOpen(ServerHandshake serverHandshake) {
                    ChatBot.this.onOpen(serverHandshake);
                }

                public void onMessage(String message) {
                    ChatBot.this.onRawMessage(message);
                    message = message.replaceAll("\n", "").replaceAll("\r", "");
                    if (message.contains("PRIVMSG")) {
                        String tagsPrefix = message.split("@", 3)[1];
                        String channelPrefix = message.split("@", 3)[2].replace("\r", "");
                        String sentMessage = channelPrefix.split(channel + " :", 2)[1].replace("\n", "").replace("\r", "").trim();
                        String sender = channelPrefix.split(".tmi.twitch.tv")[0];
                        String[] badges = new String[0];
                        boolean isMod = false;
                        boolean isSub = false;
                        int cheerCount = 0;
                        String[] tags = tagsPrefix.split(";");
                        String[] var11 = tags;
                        int var12 = tags.length;
                        int var13;
                        String badgeA;
                        for(var13 = 0; var13 < var12; ++var13) {
                            badgeA = var11[var13];
                            if (badgeA.split("=", 2)[0].equals("badges")) {
                                badges = badgeA.split("=", 2)[1].split(",");
                            }

                            if (badgeA.split("=", 2)[0].equals("bits")) {
                                cheerCount = Integer.parseInt(badgeA.split("=", 2)[1]);
                            }

                            if (badgeA.split("=", 2)[0].equals("display-name")) {
                                sender = badgeA.split("=", 2)[1];
                            }
                        }

                        var11 = badges;
                        var12 = badges.length;

                        for(var13 = 0; var13 < var12; ++var13) {
                            badgeA = var11[var13];
                            if (badgeA.split("/", 2)[0].equals("broadcaster") || badgeA.split("/", 2)[0].equals("moderator")) {
                                isMod = true;
                            }

                            if (badgeA.split("/", 2)[0].equals("subscriber") || badgeA.split("/", 2)[0].equals("founder")) {
                                isSub = true;
                            }
                        }

                        ChatBot.this.onMessage(new ChatMessage(tags, sender, sentMessage, badges, isMod, isSub, cheerCount));
                    }

                    if (message.equalsIgnoreCase("PING :tmi.twitch.tv")) {
                        this.send("PONG :tmi.twitch.tv");
                    }

                }

                public void onClose(int code, String reason, boolean remote) {
                    ChatBot.this.onClose(code, reason, remote);
                }

                public void onError(Exception e) {
                    ChatBot.this.onError(e);
                }
            };
        } catch (URISyntaxException ignored) {
        }

    }

    public boolean connect(String oauth, String nick) {
        try {
            this.chatReader.connectBlocking();
            while(!chatReader.isOpen()){
                Thread.sleep(10);
            }
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }
        try {
            this.chatReader.send("CAP REQ :twitch.tv/tags");
            this.chatReader.send("CAP REQ :twitch.tv/commands");
            this.chatReader.send("CAP REQ :twitch.tv/membership");
            this.chatReader.send("PASS oauth:" + oauth);
            this.chatReader.send("NICK " + nick);
            this.chatReader.send("JOIN #" + this.channel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void sendMessage(String message) {
        this.chatReader.send("PRIVMSG #" + this.channel + " :" + message);
    }

    public void sendRawMessage(String message) {
        this.chatReader.send(message);
    }

    public void disconnect() {
        try {
            this.chatReader.closeBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isOpen() {
        return this.chatReader.isOpen();
    }

    public abstract void onOpen(ServerHandshake var1);

    public abstract void onClose(int var1, String var2, boolean var3);

    public abstract void onMessage(ChatMessage var1);

    public abstract void onRawMessage(String var1);

    public abstract void onError(Exception var1);
}
