package com.alphalaneous;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class APIs {

	public static TwitchAccount authenticate(String oauth) {
		try {
			URL url = new URL("https://id.twitch.tv/oauth2/validate");
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization", "OAuth " + oauth);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String x = br.readLine();
			JSONObject data = new JSONObject(x);
			String username = data.get("login").toString().replace("\"", "");
			String userID = data.get("user_id").toString().replace("\"", "");
			return new TwitchAccount(username, userID);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static String getIDs(String username) {
		JSONObject userIDObject = twitchAPI("https://api.twitch.tv/helix/users?login=" + username.toLowerCase().trim());
		if(userIDObject != null) {
			try{
				return userIDObject.getJSONArray("data").getJSONObject(0).get("id").toString().replaceAll("\"", "");
			}
			catch (Exception e){
				return null;
			}
		}

		return null;
	}
	private static JSONObject twitchAPI(String URL) {
		try {
			URL url = new URL(URL);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Client-ID", "fzwze6vc6d2f7qodgkpq2w8nnsz3rl");
			conn.setRequestProperty("Authorization", "Bearer " + Utils.getOauth());
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			return new JSONObject(br.readLine());
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
