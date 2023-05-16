package fr.paris.lutece.plugins.wiki.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

public class ContentDeserializer {
    private static final long serialVersionUID = -2287035947644920508L;

    public Integer topicVersion ;
    public int topicId;
    public List<String> topicTitleArr;

    public List<String> topicContentArr;

    /**
     * Returns the content of the file
     *
     *            The HTTP request
     *            The HTTP response
     * @throws IOException
     *             If an error occurs
     */
    public Integer getTopicVersion() {
        return topicVersion;
    }
    public int getTopicId() {
        return topicId;
    }


    public static ContentDeserializer deserializeWikiContent(String requestBody){
        final Gson gson = new GsonBuilder().create();
        final ContentDeserializer content = gson.fromJson(requestBody, ContentDeserializer.class);
        return content;
    }
}
