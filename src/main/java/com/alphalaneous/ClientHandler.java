package com.alphalaneous;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ClientHandler extends Thread {

    private final Socket clientSocket;

    private TwitchAccount twitchAccount;

    private String oauth;
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public PrintWriter out;

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            BotServer.clients++;
            System.out.println("Client " + BotServer.clients + " connected.");
            String inputLine;
            reader:
            while ((inputLine = in.readLine()) != null) {

                JSONObject data = new JSONObject(inputLine);

                String requestType = safeGetString(data, "request_type");
                if(requestType.equalsIgnoreCase("")) continue;
                JSONObject response = new JSONObject();

                switch (requestType) {

                    case "connect": {
                        String oauth = safeGetString(data, "oauth");
                        this.oauth = oauth;
                        twitchAccount = APIs.authenticate(oauth);
                        if (twitchAccount != null) {
                            twitchAccount.setClientID(BotServer.clients);
                            System.out.println(twitchAccount.getChannel() + " connected.");
                            response.put("event", "connected");
                            response.put("username", twitchAccount.getChannel());
                            String[] superOpList = Utils.getList("/loquibot/officers.txt");
                            if (superOpList != null) {
                                if (Utils.findStringInArray(superOpList, twitchAccount.getUserID())) {
                                    response.put("is_officer", true);
                                    twitchAccount.setSuperOP(true);
                                }
                            }
                            out.println(response);

                            JSONObject blockedIDs = new JSONObject();
                            blockedIDs.put("event", "blocked_ids_updated");
                            blockedIDs.put("ids", GloballyBlockedID.getJSON());

                            JSONObject blockedUsers = new JSONObject();
                            blockedIDs.put("event", "blocked_users_updated");
                            blockedIDs.put("users", GloballyBlockedUser.getJSON());

                            if(twitchAccount.getSuperOP()) {
                                JSONObject reportedIDs = new JSONObject();
                                blockedIDs.put("event", "reported_ids_updated");
                                blockedIDs.put("ids", ReportedID.getJSON());
                                out.println(reportedIDs);
                            }


                            out.println(blockedIDs);
                            out.println(blockedUsers);

                            try {
                                new Thread(() -> {
                                    twitchAccount.connect(Utils.getOauth(), "loquibot");
                                }).start();
                            } catch (Exception e) {
                                break reader;
                            }
                        } else {
                            response.put("event", "invalid_oauth");
                            out.println(response);
                            break reader;
                        }
                        break;
                    }
                    case "send_message": {
                        if (twitchAccount.isOpen()) {
                            String message = safeGetString(data,"message").trim().toLowerCase(Locale.ROOT);
                            String messageStart = message.split(" ")[0].trim().toLowerCase(Locale.ROOT);
                            boolean sendMessage = true;
                            if (message.startsWith("/") || message.startsWith(".")) {
                                sendMessage = false;
                                switch (messageStart) {
                                    case "/block":
                                    case "/unblock":
                                    case "/color":
                                    case "/w":
                                        break;
                                    default: {
                                        sendMessage = true;
                                        break;
                                    }
                                }
                            }
                            if (sendMessage) {
                                if (twitchAccount.isOpen()) {
                                    System.out.println(twitchAccount.getClientID() + ":" + twitchAccount.getChannel() + " | loquibot: " + data.getString("message"));
                                    twitchAccount.sendMessage(data.getString("message"));
                                }
                            }
                        }
                        break;
                    }

                    case "send_yt_message":{
                        try {
                            System.out.println("YT: " + safeGetString(data, "liveChatId") + " | loquibot: " + safeGetString(data, "message"));

                            Iterable<String> result = Splitter.fixedLength(200).split(safeGetString(data, "message"));
                            String[] parts = Iterables.toArray(result, String.class);
                            for(String s : parts) {
                                YTMessaging.sendMessage(s, safeGetString(data, "liveChatId"));
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            break;
                        }
                        break;
                    }

                    case "get_blocked_ids": {
                        response.put("event", "blocked_ids_updated");
                        response.put("ids", GloballyBlockedID.getJSON());
                        out.println(response);
                        break;
                    }
                    case "get_blocked_users": {
                        response.put("event", "blocked_users_updated");
                        response.put("users", GloballyBlockedUser.getJSON());
                        out.println(response);
                    }
                    case "get_reported_ids": {
                        response.put("event", "reported_ids_updated");
                        response.put("ids", ReportedID.getJSON());
                        out.println(response);
                    }
                    case "report_id":{


                        String ID = safeGetString(data, "id").trim();
                        String reason = safeGetString(data, "reason").trim();
                        String username = safeGetString(data, "username").trim().toLowerCase();
                        String userID = safeGetString(data, "userID").trim();

                        if(reason.equalsIgnoreCase("")) reason = "No reason provided";

                        new ReportedID(ID, reason, username, userID);
                        broadcastReportedIDsUpdated();

                        break;
                    }
                    default:
                        break;
                }
                if(twitchAccount != null){
                if (twitchAccount.getSuperOP()) {
                    switch (requestType) {
                        case "reset_oauth": {
                            if(twitchAccount.getUserID().equalsIgnoreCase("99550233")){

                                String oauth = safeGetString(data, "oauth").trim();
                                Path path = Paths.get("/loquibot/oauth.txt");
                                try {
                                    Files.write(path, oauth.getBytes());
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                                BotServer.restart();
                            }
                            break;
                        }
                        case "restart": {
                            if(twitchAccount.getUserID().equalsIgnoreCase("99550233")){
                                BotServer.restart();
                            }
                            break;
                        }

                        case "globally_block_id": {

                            String ID = safeGetString(data, "id").trim();
                            String reason = safeGetString(data, "reason").trim();
                            long IDl;
                            try {
                                IDl = Long.parseLong(ID);
                            } catch (Exception e) {
                                response.put("event", "error");
                                response.put("error", "invalid_blocked_ID");
                                out.println(response);
                                break;
                            }
                            if (IDl == -1) {
                                response.put("event", "error");
                                response.put("error", "invalid_blocked_ID");
                                out.println(response);
                                break;
                            }
                            if (reason.equalsIgnoreCase("")) {
                                response.put("event", "error");
                                response.put("error", "no_id_block_reason_given");
                                out.println(response);
                                break;
                            }
                            if (GloballyBlockedID.get(IDl) != null) {
                                response.put("event", "error");
                                response.put("error", "id_already_blocked");
                                out.println(response);
                                break;
                            }

                            new GloballyBlockedID(IDl, reason);
                            broadcastGloballyBlockedIDsUpdated();

                            break;

                        }
                        case "globally_unblock_id": {
                            String ID = safeGetString(data, "id").trim().toLowerCase();
                            long IDl;
                            try {
                                IDl = Long.parseLong(ID);
                            } catch (Exception e) {
                                response.put("event", "error");
                                response.put("error", "invalid_unblocked_ID");
                                out.println(response);
                                break;
                            }
                            GloballyBlockedID a = GloballyBlockedID.get(IDl);
                            if (a != null) a.remove();
                            else {
                                response.put("event", "error");
                                response.put("error", "id_not_blocked");
                                break;
                            }
                            broadcastGloballyBlockedIDsUpdated();

                            break;
                        }
                        case "globally_block_user": {
                            String name = safeGetString(data, "user").trim().toLowerCase();
                            String reason = safeGetString(data, "reason").trim();

                            if (name.equalsIgnoreCase("")) {
                                response.put("event", "error");
                                response.put("error", "invalid_blocked_user");
                                out.println(response);
                                break;
                            }
                            if (reason.equalsIgnoreCase("")) {
                                response.put("event", "error");
                                response.put("error", "no_user_block_reason_given");
                                out.println(response);
                                break;
                            }
                            String userID = APIs.getIDs(name);
                            if(userID == null){
                                response.put("event", "error");
                                response.put("error", "user_doesnt_exist");
                                out.println(response);
                                break;
                            }
                            if(GloballyBlockedUser.get(Long.parseLong(userID)) != null){
                                response.put("event", "error");
                                response.put("error", "user_already_blocked");
                                out.println(response);
                                break;
                            }

                            new GloballyBlockedUser(Long.parseLong(userID), reason, name);

                            broadcastGloballyBlockedUsersUpdated();

                            break;
                        }
                        case "globally_unblock_user": {
                            String name = safeGetString(data, "user").trim().toLowerCase();

                            if(name.equalsIgnoreCase("")){
                                response.put("event", "error");
                                response.put("error", "no_user_provided");
                                out.println(response);
                                break;
                            }

                            String userID = APIs.getIDs(name);

                            if(userID == null){
                                response.put("event", "error");
                                response.put("error", "couldnt_find_user");
                                out.println(response);
                                break;
                            }

                            GloballyBlockedUser a = GloballyBlockedUser.get(Long.parseLong(userID));
                            if(a != null) a.remove();
                            else {
                                response.put("event", "error");
                                response.put("error", "user_isnt_blocked");
                                out.println(response);
                                break;
                            }


                            broadcastGloballyBlockedUsersUpdated();
                            break;
                        }
                        case "get_current_streamers": {
                            JSONArray clients = new JSONArray();
                            for (ClientHandler clientHandler : BotServer.clientsList) {
                                clients.put(clientHandler.twitchAccount.getChannel());
                            }
                            response.put("event", "clients");
                            response.put("clients", clients);
                            out.println(response);
                        }
                        default:
                            break;
                    }
                }
                }
            }
            in.close();
            out.close();
            clientSocket.close();
            if(twitchAccount != null) {
                twitchAccount.disconnect();
            }
        } catch (SocketException e){
            if(twitchAccount != null) {
                twitchAccount.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(twitchAccount != null) {
                twitchAccount.disconnect();
            }
        }
    }

    public void reconnect(){

        JSONObject response = new JSONObject();

        if (twitchAccount != null) {
            twitchAccount.disconnect();
        }
        twitchAccount = APIs.authenticate(oauth);
        if (twitchAccount != null) {
            twitchAccount.setClientID(BotServer.clients);
            System.out.println(twitchAccount.getChannel() + " connected.");
            response.put("event", "connected");
            response.put("username", twitchAccount.getChannel());
            String[] superOpList = Utils.getList("/loquibot/officers.txt");
            if (superOpList != null) {
                if (Utils.findStringInArray(superOpList, twitchAccount.getUserID())) {
                    response.put("is_officer", true);
                    twitchAccount.setSuperOP(true);
                }
            }
            out.println(response);

            JSONObject blockedIDs = new JSONObject();
            blockedIDs.put("event", "blocked_ids_updated");
            blockedIDs.put("ids", GloballyBlockedID.getJSON());

            out.println(blockedIDs);

            try {
                new Thread(() -> {
                    twitchAccount.connect(Utils.getOauth(), "loquibot");
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.put("event", "invalid_oauth");
            out.println(response);
        }

    }


    public void sendBlockedIDsUpdated(){
        JSONObject response = new JSONObject();
        response.put("event", "blocked_ids_updated");
        response.put("ids", GloballyBlockedID.getJSON());
        out.println(response);
    }
    public void sendBlockedUsersUpdated(){
        JSONObject response = new JSONObject();
        response.put("event", "blocked_users_updated");
        response.put("users", GloballyBlockedUser.getJSON());
        out.println(response);
    }

    public static void broadcastReportedIDsUpdated(){
        for(ClientHandler clientHandler : BotServer.clientsList){

            if(clientHandler.twitchAccount.getSuperOP()) {

                JSONObject response = new JSONObject();

                response.put("event", "reported_ids_updated");
                response.put("ids", ReportedID.getJSON());

                clientHandler.send(String.valueOf(response));
            }
        }
    }

    public static void broadcastGloballyBlockedIDsUpdated(){
        for(ClientHandler clientHandler : BotServer.clientsList){

            JSONObject response = new JSONObject();

            response.put("event", "blocked_ids_updated");
            response.put("globallyBlockedIDs", GloballyBlockedID.getJSON());

            clientHandler.send(String.valueOf(response));
        }
    }

    public static void broadcastGloballyBlockedUsersUpdated(){
        for(ClientHandler clientHandler : BotServer.clientsList){
            JSONObject response = new JSONObject();

            response.put("event", "blocked_users_updated");
            response.put("globallyBlockedIDs", GloballyBlockedUser.getJSON());

            clientHandler.send(String.valueOf(response));
        }
    }


    public void sendBroadcast(String message){
        JSONObject response = new JSONObject();
        response.put("event", "broadcast");
        response.put("message", message);
        out.println(response);
    }

    public void send(String message){
        out.println(message);
    }


    public void sendMessage(String message){
        if(twitchAccount.isOpen()) {
            twitchAccount.sendMessage(message);
        }
    }

    public static String safeGetString(JSONObject object, String key){
        try {
            if (object.has(key)) {
                if (object.get(key) instanceof String) return object.getString(key);
            }
        }
        catch (Exception ignored){}
        return "";
    }
    public static long safeGetLong(JSONObject object, String key){
        try {
            if (object.has(key)) {
                if (object.get(key) instanceof Long) return object.getLong(key);
            }
        }
        catch (Exception ignored){}
        return -1;
    }
}
