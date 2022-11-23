package com.alphalaneous;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.LiveChatMessage;
import com.google.api.services.youtube.model.LiveChatMessageSnippet;
import com.google.api.services.youtube.model.LiveChatTextMessageDetails;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class YTMessaging {

    public static Credential credential;
    public static YouTube youtube;
    private static final List<String> scopes = Lists.newArrayList(YouTubeScopes.all());

    public static void setCredential() throws IOException {
        credential = Auth.authorize(scopes, "YouTubeCredentials", false);
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                .setApplicationName("loquibot-chat").build();
    }

    public static void sendMessage(String message, String liveChatId) throws IOException {
        LiveChatMessage liveChatMessage = new LiveChatMessage();

        LiveChatMessageSnippet snippet = new LiveChatMessageSnippet();
        snippet.setType("textMessageEvent");
        snippet.setLiveChatId(liveChatId);

        LiveChatTextMessageDetails details = new LiveChatTextMessageDetails();
        details.setMessageText(message);
        snippet.setTextMessageDetails(details);
        liveChatMessage.setSnippet(snippet);

        YouTube.LiveChatMessages.Insert request = youtube.liveChatMessages()
                .insert(Collections.singletonList("snippet"), liveChatMessage);
        request.execute();

    }
}
