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

public class GloballyBlockedID {

    private static ArrayList<GloballyBlockedID> globallyBlockedIDS = new ArrayList<>();

    private final String reason;
    private final long ID;

    public GloballyBlockedID(long ID, String reason){
        this.ID = ID;
        this.reason = reason;
        globallyBlockedIDS.add(this);
    }
    public long getID(){
        return ID;
    }
    public String getReason(){
        return reason;
    }
    public void remove(){
        globallyBlockedIDS.remove(this);
    }

    public static ArrayList<GloballyBlockedID> getAll(){
        return globallyBlockedIDS;
    }
    public static JSONObject getJSON(){
        return loadToJson();
    }

    private static JSONObject loadToJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(GloballyBlockedID data : globallyBlockedIDS){
            JSONObject globallyBlockedID = new JSONObject();
            globallyBlockedID.put("id", data.ID);
            globallyBlockedID.put("reason", data.reason);
            jsonArray.put(globallyBlockedID);
        }
        jsonObject.put("globallyBlockedIDs", jsonArray);
        return jsonObject;
    }

    public static GloballyBlockedID get(long ID){
        for(GloballyBlockedID globallyBlockedID : globallyBlockedIDS){
            if(globallyBlockedID.ID == ID) return globallyBlockedID;
        }
        return null;
    }
    public static void remove(long ID){
        GloballyBlockedID globallyBlockedID = get(ID);
        if (globallyBlockedID != null) globallyBlockedID.remove();
    }
    public static void load(){
        try {
            Path path = Paths.get("/loquibot/globallyBlockedIDs.json");
            createPathIfDoesntExist(path);

            globallyBlockedIDS = loadJsonToGlobalBlockArrayList(IOUtils.toString(new FileReader("/loquibot/globallyBlockedIDs.json")));
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
        Path path = Paths.get("/loquibot/globallyBlockedIDs.json");
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
                object.put("globallyBlockedIDs", array);

                Files.write(path, object.toString(4).getBytes());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private static ArrayList<GloballyBlockedID> loadJsonToGlobalBlockArrayList(String jsonData){
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray globallyBlockedIDArray = jsonObject.getJSONArray("globallyBlockedIDs");
        ArrayList<GloballyBlockedID> globallyBlockedIDs = new ArrayList<>();
        for(int i = 0; i < globallyBlockedIDArray.length(); i++){
            try {
                JSONObject globallyBlockedIDJson = globallyBlockedIDArray.getJSONObject(i);
                GloballyBlockedID globallyBlockedID = new GloballyBlockedID(globallyBlockedIDJson.getLong("id"), globallyBlockedIDJson.getString("reason"));
                globallyBlockedIDs.add(globallyBlockedID);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return globallyBlockedIDs;
    }
}
