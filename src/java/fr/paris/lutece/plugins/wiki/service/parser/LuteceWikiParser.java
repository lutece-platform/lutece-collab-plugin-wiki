/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
package fr.paris.lutece.plugins.wiki.service.parser;

import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.plugins.wiki.web.Constants;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import static ys.wikiparser.Utils.*;

import ys.wikiparser.WikiParser;

import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;


/**
 * Lutece Wiki Parser
 */
public class LuteceWikiParser extends WikiParser
{
    private static final String PROPERTY_TABLE_CLASS = "wiki.parser.tableClass";
    
    private static String _strPortalUrl;

    /**
     * Constructor
     * @param strWikiText The wiki text
     */
    public LuteceWikiParser( String strWikiText )
    {
        super(  );
        HEADING_LEVEL_SHIFT = 0;
        setTableClass( AppPropertiesService.getProperty( PROPERTY_TABLE_CLASS ));
        parse( strWikiText );
    }

    /**
     * Set the portal URL
     * @param strUrl The URL
     */
    public static void setPortalUrl( String strUrl )
    {
        _strPortalUrl = strUrl;
    }

    /**
     * Render XHTML from wiki text
     * @param strWikiText The wiki text
     * @return The XHTML code
     */
    public static String renderXHTML( String strWikiText )
    {
        return new LuteceWikiParser( strWikiText ).toString(  );
    }

    /**
     * Append image
     * @param strText The text
     */
    @Override
    protected void appendImage( String strText )
    {
        super.appendImage( strText );
    }

    /**
     * Append link
     * @param strText The text
     */
    @Override
    protected void appendLink( String strText )
    {
        String[] link = split( strText, '|' );
        URI uri = null;

        try
        { // validate URI
            uri = new URI( link[0].trim(  ) );
        }
        catch ( URISyntaxException e )
        {
            AppLogService.error( "LuteceWikiParser : Error appenlink : " + e.getMessage(  ), e );
        }

        if ( ( uri != null ) && uri.isAbsolute(  ) )
        {
            sb.append( "<a href=\"" );
            sb.append( escapeHTML( uri.toString(  ) ) );
            sb.append( "\" rel=\"nofollow\">" );
            sb.append( escapeHTML( unescapeHTML( ( ( link.length >= 2 ) && !isEmpty( link[1].trim(  ) ) ) ? link[1]
                                                                                                          : link[0] ) ) );
            sb.append( "</a>" );
        }
        else
        {
            Plugin plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );
            Topic topic = TopicHome.findByPrimaryKey( escapeHTML( escapeURL( link[0] ) ), plugin );
            String strAction = Constants.PARAMETER_ACTION_VIEW;
            String strColorBegin = "";
            String strColorEnd = "";

            if ( topic == null )
            {
                strAction = Constants.PARAMETER_ACTION_CREATE;
                strColorBegin = "<font color=red>";
                strColorEnd = "</font>";
            }

            sb.append( "<a href=\"" );
            sb.append( _strPortalUrl );
            sb.append( "?page=wiki&page_name=" );
            sb.append( escapeHTML( unescapeHTML( link[0] ) ) );
            sb.append( "&action=" );
            sb.append( strAction );
            sb.append( " \" title=\"Wikipedia link\">" );
            sb.append( strColorBegin );
            sb.append( escapeHTML( unescapeHTML( ( ( link.length >= 2 ) && !isEmpty( link[1].trim(  ) ) ) ? link[1]
                                                                                                          : link[0] ) ) );
            sb.append( strColorEnd );
            sb.append( "</a>" );
        }
    }

    /**
     * Append a macro
     * @param strText The text
     */
    @Override
    protected void appendMacro( String strText )
    {
        if ( "TOC".equals( strText ) )
        {
            super.appendMacro( strText ); // use default
        }
        else if ( "My-macro".equals( strText ) )
        {
            sb.append( "{{ My macro output }}" );
        }
        else
        {
            super.appendMacro( strText );
        }
    }

    /**
     * Encode URL
     * @param strURL the URL
     * @return The encoded URL
     */
    private static String escapeURL( String strURL )
    {
        try
        {
            return URLEncoder.encode( strURL, "utf-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            AppLogService.error( "LuteceWikiParser : Error escaping Url : " + e.getMessage(  ), e );

            return null;
        }
    }
}
