package com.alphalaneous;

import org.java_websocket.handshake.ServerHandshake;

import java.util.Locale;

import static com.alphalaneous.ClientHandler.broadcastGloballyBlockedIDsUpdated;
import static com.alphalaneous.ClientHandler.broadcastGloballyBlockedUsersUpdated;

public class TwitchAccount extends ChatBot {

    private final String channel;
    private final String userID;
    private int clientID = 0;
    private boolean isSuperOP = false;

    public TwitchAccount(String channel, String userID) {
        super(channel);
        this.channel = channel;
        this.userID = userID;
    }

    public void setSuperOP(boolean isSuperOP){
        this.isSuperOP = isSuperOP;
    }


    public void setClientID(int clientID){
        this.clientID = clientID;
    }

    public boolean getSuperOP(){
        return isSuperOP;
    }

    public String getChannel() {
        return channel;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Client " + clientID + ": " + channel + " disconnected.");
    }

    @Override
    public void onMessage(ChatMessage chatMessage) {
        if(clientID != 0) {
            System.out.println(clientID + ":" + channel + " | " + chatMessage.getSender() + ": " + chatMessage.getMessage());
        }

        String command = chatMessage.getMessage().toLowerCase(Locale.ROOT).split(" ")[0];
        String message = chatMessage.getMessage().substring(command.length()).trim();

        String[] superOpList = Utils.getList("/loquibot/officers.txt");
        if(chatMessage.getTag("user-id").equalsIgnoreCase("99550233")){
            switch (command) {
                case "!broadcast" : {
                    for(ClientHandler clientHandler : BotServer.clientsList){
                        clientHandler.sendBroadcast(message);
                    }
                    break;
                }
                case "!addofficer" : {
                    String username = message.split(" ")[0];
                    String userID = APIs.getIDs(username);
                    if(userID != null){
                        if(superOpList != null) {
                            if(!Utils.findStringInArray(superOpList, userID)){
                                Utils.writeToFile("/loquibot/officers.txt", userID + ";");
                                sendMessage("@" + chatMessage.getSender() + ", " + username + " has been added as an officer!");
                            }
                            else{
                                sendMessage("@" + chatMessage.getSender() + ", " + username + " is already an officer!");
                            }
                        }
                    }
                    else{
                        sendMessage("@" + chatMessage.getSender() + ", failed to add " + username + ", user might not exist.");
                    }
                    break;
                }
                case "!removeofficer" : {
                    String username = message.split(" ")[0];
                    String userID = APIs.getIDs(username);
                    if(userID != null) {
                        if (superOpList != null) {
                            if (Utils.findStringInArray(superOpList, userID)) {
                                Utils.clearFile("/loquibot/officers.txt");
                                for(String ID : superOpList){
                                    if(!ID.equalsIgnoreCase(userID)){
                                        Utils.writeToFile("/loquibot/officers.txt", ID + ";");
                                    }
                                }
                                sendMessage("@" + chatMessage.getSender() + ", " + username + " is no longer an officer.");
                            }
                            else{
                                sendMessage("@" + chatMessage.getSender() + ", Couldn't remove, " + username + " is not an officer.");
                            }
                        }
                    }
                    break;
                }
                default: break;
            }
        }
        if(superOpList != null) {
            if(Utils.findStringInArray(superOpList, chatMessage.getTag("user-id"))){
                switch (command){
                    case "!globallyblockid" : {
                        String ID = message.split(" ")[0].trim();
                        String reason = message.substring(ID.length()).trim();
                        long IDl;
                        try{
                            IDl = Long.parseLong(ID);
                        }
                        catch (NumberFormatException e){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally block " + ID + ", invalid ID.");
                            break;
                        }
                        if(reason.equalsIgnoreCase("")){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally block " + ID + ", no reason provided.");
                            break;
                        }
                        if(GloballyBlockedID.get(IDl) != null){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally block " + ID + ", ID already blocked.");

                            break;
                        }

                        new GloballyBlockedID(IDl, reason);
                        sendMessage("@" + chatMessage.getSender() + " " + ID + " has been globally blocked for \"" + reason + "\"");
                        broadcastGloballyBlockedIDsUpdated();
                        break;
                    }
                    case "!globallyunblockid" : {
                        String ID = message.trim();
                        long IDl;
                        try{
                            IDl = Long.parseLong(ID);
                        }
                        catch (NumberFormatException e){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally unblock " + ID + ", invalid ID.");
                            break;
                        }
                        GloballyBlockedID a = GloballyBlockedID.get(IDl);
                        if(a != null) a.remove();
                        else {
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally unblock " + ID + ", ID isn't blocked.");
                            break;
                        }
                        sendMessage("@" + chatMessage.getSender() + " " + ID + " has been globally unblocked.");
                        broadcastGloballyBlockedIDsUpdated();
                        break;
                    }
                    case "!globallyblockuser" : {
                        String name = message.split(" ")[0].trim();
                        String reason = message.substring(name.length()).trim();
                        if(name.equalsIgnoreCase("")){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally block " + name + ", no user provided.");
                            break;
                        }
                        if(reason.equalsIgnoreCase("")){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally block " + name + ", no reason provided.");
                            break;
                        }
                        String userID = APIs.getIDs(name);
                        if(userID == null){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally block " + name + ", couldn't find user.");
                            break;
                        }
                        if(GloballyBlockedUser.get(Long.parseLong(userID)) != null){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally block " + name + ", user already blocked.");

                            break;
                        }

                        new GloballyBlockedUser(Long.parseLong(userID), reason, name);
                        sendMessage("@" + chatMessage.getSender() + " " + name + " has been globally blocked for \"" + reason + "\"");
                        broadcastGloballyBlockedUsersUpdated();
                        break;
                    }
                    case "!globallyunblockuser" : {
                        String name = message.trim();
                        if(name.equalsIgnoreCase("")){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally unblock " + name + ", no user provided.");
                            break;
                        }

                        String userID = APIs.getIDs(name);

                        if(userID == null){
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally unblock " + name + ", couldn't find user.");
                            break;
                        }

                        GloballyBlockedUser a = GloballyBlockedUser.get(Long.parseLong(userID));
                        if(a != null) a.remove();
                        else {
                            sendMessage("@" + chatMessage.getSender() + ", failed to globally unblock " + name + ", user isn't blocked.");
                            break;
                        }
                        sendMessage("@" + chatMessage.getSender() + " " + name + " has been globally unblocked.");
                        broadcastGloballyBlockedUsersUpdated();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onRawMessage(String s) {
        System.out.println(s);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }

    public String getUserID() {
        return userID;
    }

    public int getClientID() {
        return clientID;
    }
}
