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
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;

/**
 * Wiki JSP Bean
 */
public class WikiJspBean implements Serializable
{
    private static final String PLUGIN_NAME = "wiki";
    private static final String PARAMETER_PAGE_NAME = "page_name";
    private static final String URL_HOME = "../../Portal.jsp?page=wiki";
    
    Plugin _plugin = PluginService.getPlugin( PLUGIN_NAME );
    
    /**
     * Delete a wikipage
     * @param request The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException 
     */
    public String doDeletePage( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );
        String strPageName = request.getParameter( PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );
        TopicHome.remove( topic.getIdTopic() , _plugin);
        return URL_HOME;
        
    }

    /**
     * Check authentication
     * @param request The HTTP request
     * @throws UserNotSignedException if no user connected
     */
    private void checkUser(HttpServletRequest request) throws UserNotSignedException
    {
        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            SecurityService.getInstance(  ).getRemoteUser( request );
        }
    }
}
