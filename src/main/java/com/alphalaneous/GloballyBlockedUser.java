package com.alphalaneous;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class GloballyBlockedUser {

    private static ArrayList<GloballyBlockedUser> globallyBlockedUsers = new ArrayList<>();

    private final String reason;
    private final String name;
    private final long ID;

    public GloballyBlockedUser(long ID, String reason, String name){
        this.ID = ID;
        this.reason = reason;
        this.name = name;
        globallyBlockedUsers.add(this);
    }
    public long getID(){
        return ID;
    }
    public String getReason(){
        return reason;
    }
    public void remove(){
        globallyBlockedUsers.remove(this);
    }

    public static ArrayList<GloballyBlockedUser> getAll(){
        return globallyBlockedUsers;
    }
    public static JSONObject getJSON(){
        return loadToJson();
    }

    private static JSONObject loadToJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(GloballyBlockedUser data : globallyBlockedUsers){
            JSONObject globallyBlockedID = new JSONObject();
            globallyBlockedID.put("id", data.ID);
            globallyBlockedID.put("name", data.name);
            globallyBlockedID.put("reason", data.reason);
            jsonArray.put(globallyBlockedID);
        }
        jsonObject.put("globallyBlockedUsers", jsonArray);
        return jsonObject;
    }

    public static GloballyBlockedUser get(long ID){
        for(GloballyBlockedUser globallyBlockedID : globallyBlockedUsers){
            if(globallyBlockedID.ID == ID) return globallyBlockedID;
        }
        return null;
    }
    public static void remove(long ID){
        GloballyBlockedUser globallyBlockedID = get(ID);
        if (globallyBlockedID != null) globallyBlockedID.remove();
    }
    public static void load(){
        try {
            Path path = Paths.get("/loquibot/globallyBlockedUsers.json");
            createPathIfDoesntExist(path);

            globallyBlockedUsers = loadJsonToGlobalBlockArrayList(IOUtils.toString(new FileReader("/loquibot/globallyBlockedUsers.json")));
            new Thread(() -> {
                while(true){
                    save();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void save(){
        Path path = Paths.get("/loquibot/globallyBlockedUsers.json");
        try {
            Files.write(path, loadToJson().toString(4).getBytes());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void createPathIfDoesntExist(Path path){
        try {
            Path loquiDir = Paths.get("/loquibot/");
            if(!Files.isDirectory(loquiDir)){
                Files.createDirectory(loquiDir);
            }
            if (!Files.exists(path)) {
                Files.createFile(path);

                JSONObject object = new JSONObject();
                JSONArray array = new JSONArray();
                object.put("globallyBlockedUsers", array);

                Files.write(path, object.toString(4).getBytes());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private static ArrayList<GloballyBlockedUser> loadJsonToGlobalBlockArrayList(String jsonData){

        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray globallyBlockedUserArray = jsonObject.getJSONArray("globallyBlockedUsers");
        ArrayList<GloballyBlockedUser> globallyBlockedUsers = new ArrayList<>();
        for(int i = 0; i < globallyBlockedUserArray.length(); i++){
            try {
                JSONObject globallyBlockedUserJson = globallyBlockedUserArray.getJSONObject(i);
                GloballyBlockedUser globallyBlockedUser = new GloballyBlockedUser(globallyBlockedUserJson.getLong("id"), globallyBlockedUserJson.getString("reason"), globallyBlockedUserJson.getString("name"));
                globallyBlockedUsers.add(globallyBlockedUser);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return globallyBlockedUsers;
    }
}
