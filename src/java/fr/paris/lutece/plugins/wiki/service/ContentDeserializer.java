package fr.paris.lutece.plugins.wiki.service;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.portal.service.util.AppLogService;

public class ContentDeserializer {
    private static final long serialVersionUID = -2287035947644920508L;

    public Integer topicVersion ;
    public String parentPageName;
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

    public String getParentPageName() {
        return parentPageName;
    }


    public static ContentDeserializer deserializeWikiContent(String requestBody) {
        ObjectMapper mapper = new ObjectMapper();
        ContentDeserializer contentDeserializer = null;
        try {
            contentDeserializer = mapper.readValue(requestBody, ContentDeserializer.class);
        } catch (IOException e) {
            AppLogService.error("Error deserializing wiki content", e);
        }
        return contentDeserializer;
    }
}
