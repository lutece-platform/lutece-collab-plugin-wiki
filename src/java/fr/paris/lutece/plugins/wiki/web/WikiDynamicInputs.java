/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.wiki.business.*;
import fr.paris.lutece.plugins.wiki.service.ContentDeserializer;
import fr.paris.lutece.plugins.wiki.service.RoleService;
import fr.paris.lutece.plugins.wiki.service.WikiLocaleService;
import fr.paris.lutece.plugins.wiki.utils.auth.WikiAnonymousUser;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.util.AppLogService;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;


/**
 * Upload application
 */
public class WikiDynamicInputs {


    public static String saveWiki(HttpServletRequest request) throws IOException, UserNotSignedException {


       Boolean saveSuccess = false;
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String requestBody = sb.toString();

       try {
       ContentDeserializer newContent = ContentDeserializer.deserializeWikiContent(requestBody);
        LuteceUser user = WikiAnonymousUser.checkUser( request);

        Topic topic = TopicHome.findByPrimaryKey( newContent.getTopicId() );
        if ( RoleService.hasEditRole( request, topic ) ) {
            int nPreviousVersionId = newContent.getTopicVersion();

            int nTopicId = topic.getIdTopic();

            TopicVersion topicVersion = new TopicVersion();
            topicVersion.setIdTopic(nTopicId);
            topicVersion.setUserName(user.getName());
            if(topicVersion.getEditComment() == null || topicVersion.getEditComment().isEmpty()){
                topicVersion.setEditComment("AutoSave");
            }
            topicVersion.setEditComment(topicVersion.getEditComment());
            topicVersion.setIdTopicVersionPrevious(nPreviousVersionId);
            topicVersion.setIsPublished(false);
            // set the content for each language
            for (int i = 0; i < WikiLocaleService.getLanguages().size(); i++) {
                String strPageTitle = newContent.topicTitleArr.get(i);
                String strContent = newContent.topicContentArr.get(i);
                WikiContent content = new WikiContent();
                content.setPageTitle(strPageTitle);
                content.setWikiContent(strContent);
                topicVersion.addLocalizedWikiContent(WikiLocaleService.getLanguages().get(i), content);
            }
            TopicVersionHome.updateTopicVersion(topicVersion);
            topic.setViewRole(topic.getViewRole());
            topic.setEditRole(topic.getEditRole());
            topic.setParentPageName(topic.getParentPageName());
            TopicHome.update(topic);
            saveSuccess = true;
        }
       }  catch( Exception e )
       {
           AppLogService.error( "Error saving topic version automatically", e );
       }
       // return the response in json
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        System.out.println(gson.toJson(saveSuccess));
        return gson.toJson(saveSuccess);
    }

    public static void updateLastOpenModifyTopicPage(HttpServletRequest request) throws IOException, UserNotSignedException {
       System.out.println("updateLastOpenModifyTopicPage");
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String requestBody = sb.toString();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final int topicId = gson.fromJson(requestBody, int.class);
        Topic topic = TopicHome.findByPrimaryKey(topicId);
        try {
            if (RoleService.hasEditRole(request, topic)) {

                User user = WikiAnonymousUser.checkUser(request);
                System.out.println("user: " + user.getLastName());
                System.out.println("topic: " + topic.getIdTopic());
                TopicHome.updateLastOpenModifyPage(topic.getIdTopic(), user);
            }
        } catch (Exception e) {
            AppLogService.error("Error saving last user opening modify topic page", e);

        }
    }

}