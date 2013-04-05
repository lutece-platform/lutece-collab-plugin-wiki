/*
 * Copyright (c) 2002-2012, Mairie de Paris
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

import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.plugins.wiki.business.TopicVersion;
import fr.paris.lutece.plugins.wiki.business.TopicVersionHome;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;


/**
 * Wiki JSP Bean
 */
public class WikiJspBean implements Serializable
{
    private static final String URL_HOME = "../../Portal.jsp?page=wiki";
    private static final String URL_PAGE = "../../Portal.jsp?page=wiki&action=view&page_name=";
    Plugin _plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );

    /**
     * Delete a wikipage
     * @param request The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException
     */
    public String doDeletePage( HttpServletRequest request )
        throws UserNotSignedException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );
        TopicHome.remove( topic.getIdTopic(  ), _plugin );

        return URL_HOME;
    }

    /**
      * Create a wikipage
      * @param request The HTTP Request
      * @return The redirect URL
      * @throws SiteMessageException if an error occurs
      * @throws UserNotSignedException if an error occurs
      */
    public String doCreatePage( HttpServletRequest request )
        throws SiteMessageException, UserNotSignedException
    {
        LuteceUser user = SecurityService.getInstance(  ).getRemoteUser( request );
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strContent = request.getParameter( Constants.PARAMETER_CONTENT );

        if ( TopicHome.findByPrimaryKey( strPageName, _plugin ) != null )
        {
            SiteMessageService.setMessage( request, Constants.MESSAGE_PAGE_ALREADY_EXISTS, SiteMessage.TYPE_STOP );
        }

        Topic topic = new Topic(  );
        topic.setPageName( strPageName );

        Topic newTopic = TopicHome.create( topic, _plugin );
        TopicVersion version = new TopicVersion(  );
        version.setEditComment( "" );
        version.setLuteceUserId( user.getName(  ) );
        TopicVersionHome.create( version, _plugin );
        TopicVersionHome.modifyContentOnly( newTopic.getIdTopic(  ), user.getName(  ), "", strContent, 0, _plugin );

        return URL_PAGE + strPageName;
    }

    /**
     * Modify a wikipage
     * @param request The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException
     */
    public String doModifyPage( HttpServletRequest request )
        throws UserNotSignedException
    {
        LuteceUser user = SecurityService.getInstance(  ).getRemoteUser( request );
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strContent = request.getParameter( Constants.PARAMETER_CONTENT );
        String strPreviousVersionId = request.getParameter( Constants.PARAMETER_PREVIOUS_VERSION_ID );
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        String strComment = request.getParameter( Constants.PARAMETER_MODIFICATION_COMMENT );
        int nPreviousVersionId = Integer.parseInt( strPreviousVersionId );
        int nTopicId = Integer.parseInt( strTopicId );
        TopicVersionHome.modifyContentOnly( nTopicId, user.getName(  ), strComment, strContent, nPreviousVersionId,
            _plugin );

        return URL_PAGE + strPageName;
    }

    /**
     * Check authentication
     * @param request The HTTP request
     * @throws UserNotSignedException if no user connected
     */
    private void checkUser( HttpServletRequest request )
        throws UserNotSignedException
    {
        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            SecurityService.getInstance(  ).getRemoteUser( request );
        }
    }
}
