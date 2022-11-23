package com.alphalaneous;

import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class Utils {

    private static String oauth = null;

    public static HashMap<String, String> getBlocked(String file){
        HashMap<String, String> blocked = new HashMap<>();
        String[] list = getList(file);
        if(list != null){
            for(String s : list){
                blocked.put(s.split(",")[0], s.split(",")[1]);
            }
        }
        return blocked;
    }
    public static void clearFile(String file){
        try {
            Files.write(Paths.get(file), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeToFile(String file, String string){
        try {
            Files.write(Paths.get(file), string.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getOauth(){
        if(oauth == null) {
            try {
                String oauth = IOUtils.toString(new FileReader("/loquibot/oauth.txt"));
                Utils.oauth = oauth;
                return oauth;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        else return Utils.oauth;
    }

    public static boolean findStringInArray(String[] array, String string){
        for(String fString: array){
            if(fString.equals(string)){
                return true;
            }
        }
        return false;
    }

    public static String[] getList(String file){
        String text = null;
        try {
            text = IOUtils.toString(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(text != null){
            return text.split(";");
        }
        return null;
    }
}
