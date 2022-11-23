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

public class ReportedID {

    private static ArrayList<ReportedID> reportedIDs = new ArrayList<>();

    private final String ID;
    private final String reason;
    private final String username;
    private final String userID;

    public ReportedID(String ID, String reason, String username, String userID){
        this.ID = ID;
        this.reason = reason;
        this.username = username;
        this.userID = userID;
        reportedIDs.add(this);
    }
    public String getID(){
        return ID;
    }

    public void remove(){
        reportedIDs.remove(this);
    }

    public static ArrayList<ReportedID> getAll(){
        return reportedIDs;
    }
    public static JSONObject getJSON(){
        return loadToJson();
    }

    private static JSONObject loadToJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(ReportedID data : reportedIDs){
            JSONObject globallyBlockedID = new JSONObject();
            globallyBlockedID.put("id", data.ID);
            globallyBlockedID.put("reason", data.reason);
            globallyBlockedID.put("username", data.username);
            globallyBlockedID.put("userID", data.userID);

            jsonArray.put(globallyBlockedID);
        }
        jsonObject.put("reportedIDs", jsonArray);
        return jsonObject;
    }

    public static ReportedID get(String ID){
        for(ReportedID reportedID : reportedIDs){
            if(reportedID.ID.equalsIgnoreCase(ID)) return reportedID;
        }
        return null;
    }

    public static int getNextOf(String ID){
        int amount = 0;
        for(ReportedID reportedID : reportedIDs){
            if(reportedID.ID.equalsIgnoreCase(ID.split("_")[0])) {
                amount++;
            }
        }
        return amount+1;
    }

    public static void remove(String ID){
        ReportedID globallyBlockedID = get(ID);
        if (globallyBlockedID != null) globallyBlockedID.remove();
    }
    public static void load(){
        try {
            Path path = Paths.get("/loquibot/reportedIDs.json");
            createPathIfDoesntExist(path);

            reportedIDs = loadJsonToReportedIDsArrayList(IOUtils.toString(new FileReader("/loquibot/reportedIDs.json")));
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
        Path path = Paths.get("/loquibot/reportedIDs.json");
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
                object.put("reportedIDs", array);

                Files.write(path, object.toString(4).getBytes());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private static ArrayList<ReportedID> loadJsonToReportedIDsArrayList(String jsonData){
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray globallyBlockedIDArray = jsonObject.getJSONArray("reportedIDs");
        ArrayList<ReportedID> globallyBlockedIDs = new ArrayList<>();
        for(int i = 0; i < globallyBlockedIDArray.length(); i++){
            try {
                JSONObject globallyBlockedIDJson = globallyBlockedIDArray.getJSONObject(i);
                ReportedID globallyBlockedID = new ReportedID(globallyBlockedIDJson.getString("id"),
                        globallyBlockedIDJson.optString("reason", ""),
                        globallyBlockedIDJson.optString("username", ""),
                        globallyBlockedIDJson.optString("userID", ""));
                globallyBlockedIDs.add(globallyBlockedID);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return globallyBlockedIDs;
    }
}
