package fr.paris.lutece.plugins.wiki.service;

import fr.paris.lutece.portal.service.util.AppLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class ContentDeserializer {
    private static final long serialVersionUID = -2287035947644920508L;

    public Integer topicVersion ;
    public String parentPageName;
    public int topicId;
    public String topicTitle;

    public String topicContent;

    public  String language;

    public String wikiHtmlContent;

    public String wikiPageUrl;

    public String createVersion;

    public String publishVersion;

    public String editComment;

    public String viewRole;

    public String editRole;

    public String topicPageName;

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

    public String getTopicTitle() {
        return topicTitle;
    }

    public String getTopicContent() {
        return topicContent;
    }

    public String getParentPageName() {
        return parentPageName;
    }

    public String getLanguage() {
        return language;
    }

    public String getWikiHtmlContent() {
        return wikiHtmlContent;
    }

    public String getWikiPageUrl() {return wikiPageUrl;}

    public Boolean getIsCreateVersion() {
        return Boolean.parseBoolean(createVersion);
    }
    public Boolean getIsPublishVersion() {
        return Boolean.parseBoolean(publishVersion);
    }
    public String getEditComment() {
        return editComment;
    }
    public String getViewRole() {
        return viewRole;
    }
    public String getEditRole() {
        return editRole;
    }
    public String getTopicPageName() {
        return topicPageName;
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
