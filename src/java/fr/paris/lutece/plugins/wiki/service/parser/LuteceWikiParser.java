/*
 * Copyright (c) 2002-2017, Mairie de Paris
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
import fr.paris.lutece.plugins.wiki.business.TopicVersion;
import fr.paris.lutece.plugins.wiki.business.TopicVersionHome;
import fr.paris.lutece.plugins.wiki.web.Constants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
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
    private static final String BEAN_PARSER_OPTIONS = "wiki.parser.options";

    private String _strPageUrl;
    private String _strLanguage;
    private static WikiMacroService _macroService = new WikiMacroService( );
    private static ParserOptions _options = SpringContextService.getBean( BEAN_PARSER_OPTIONS );

    /**
     * Constructor
     * 
     * @param strWikiText
     *            The wiki text
     * @param strPageUrl
     *            The page URL
     * @param strLanguage 
     *            The language
     */
    public LuteceWikiParser( String strWikiText, String strPageUrl, String strLanguage )
    {
        super( );
        HEADING_LEVEL_SHIFT = 0;
        _strPageUrl = strPageUrl;
        _strLanguage = strLanguage;
        setTableClass( _options.getTableClass( ) );
        setTocClass( _options.getTocClass( ) );
        parse( strWikiText );
    }

    /**
     * Render specific HTML entities
     * @param strHTML The HTML code to transform
     * @return The transformed HTML
     */
    private String renderSpecific( String strHTML )
    {
        String strRender = strHTML;
        strRender = strRender.replaceAll( "\\[lt;", "&lt;" );
        strRender = strRender.replaceAll( "\\[gt;", "&gt;" );
        strRender = strRender.replaceAll( "\\[nbsp;", "&nbsp;" );
        strRender = strRender.replaceAll( "\\[quot;", "&quot;" );
        strRender = strRender.replaceAll( "\\[amp;", "&amp;" );
        strRender = strRender.replaceAll( "\\[hashmark;", "#" );

        if ( _strPageUrl != null )
        {
            strRender = strRender.replaceAll( "#page_url", _strPageUrl );
        }

        return strRender;
    }

    /**
     * Render specific entities
     * @param strSource The source
     * @return The source transformed
     */
    public static String renderWiki( String strSource )
    {
        String strRender = strSource;
        strRender = strRender.replaceAll( "\\[lt;", "<" );
        strRender = strRender.replaceAll( "\\[gt;", ">" );
        strRender = strRender.replaceAll( "\\[nbsp;", "&nbsp;" );
        strRender = strRender.replaceAll( "\\[quot;", "\"" );
        strRender = strRender.replaceAll( "\\[amp;", "&" );
        strRender = strRender.replaceAll( "\\[hashmark;", "#" );
        return strRender;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public String toString( )
    {
        return renderSpecific( super.toString( ) );
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    protected void appendImage( String strText )
    {
        try
        {
            String [ ] link = split( strText, '|' );
            String strAlt = "illustration";
            String strWidth = null;
            String strHeight = null;
            String strAlign = null;

            int nImageId = Integer.parseInt( link [0].trim( ) );

            switch( link.length )
            {
                case 5:
                    strAlign = link [4].trim( );
                case 4:
                    strHeight = link [3].trim( );

                case 3:
                    strWidth = link [2].trim( );

                case 2:
                    strAlt = link [1].trim( );
            }

            sb.append( "<img src=\"image?resource_type=wiki_image&id=" ).append( nImageId ).append( "\" alt=\"" );
            sb.append( strAlt ).append( "\" title=\"" ).append( strAlt ).append( "\" " );

            if ( link.length < 3 )
            {
                sb.append( " class=\"" ).append( _options.getImageClass( ) ).append( "\" " );
            }
            else
            {
                sb.append( " class=\"" ).append( _options.getSizedImageClass( ) ).append( "\" " );
            }

            if ( strWidth != null )
            {
                sb.append( " width=\"" ).append( strWidth ).append( "\" " );
            }

            if ( strHeight != null )
            {
                sb.append( " height=\"" ).append( strHeight ).append( "\" " );
            }

            if ( strAlign != null )
            {
                sb.append( " align=\"" ).append( strAlign ).append( "\" " );
            }

            sb.append( " />" );
        }
        catch( NumberFormatException e )
        {
            super.appendImage( strText );
        }
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    protected void appendLink( String strText )
    {
        String [ ] link = split( strText, '|' );
        URI uri = null;

        try
        { // validate URI
            uri = new URI( link [0].trim( ) );
        }
        catch( URISyntaxException e )
        {
            AppLogService.error( "LuteceWikiParser : Error appenlink : " + e.getMessage( ), e );
        }

        if ( ( uri != null ) && uri.isAbsolute( ) )
        {
            sb.append( "<a href=\"" );
            sb.append( escapeHTML( uri.toString( ) ) );
            sb.append( "\" rel=\"nofollow\">" );
            sb.append( escapeHTML( unescapeHTML( ( ( link.length >= 2 ) && !isEmpty( link [1].trim( ) ) ) ? link [1] : link [0] ) ) );
            sb.append( "</a>" );
        }
        else
        {
            Topic topic = TopicHome.findByPrimaryKey( escapeHTML( escapeURL( link [0] ) ) );
            String strAction;
            String strColorBegin = "";
            String strColorEnd = "";
            String strTopicName = link [0];

            if ( topic == null )
            {
                strAction = "&action=" + Constants.PARAMETER_ACTION_CREATE;
                strColorBegin = "<font color=red>";
                strColorEnd = "</font>";
            }
            else
            {
                TopicVersion version = TopicVersionHome.findLastVersion( topic.getIdTopic( ) );
                if( version != null )
                {
                    strTopicName = version.getWikiContent( _strLanguage ).getPageTitle( );
                    strAction = "&view=page";
                }
                else
                {
                    AppLogService.error( "LuteceWikiParser - Unable to find topic ID : " + topic.getIdTopic( ) );
                    return;
                }
            }

            sb.append( "<a href=\"" ).append( AppPathService.getPortalUrl( ) ).append( "?page=wiki" ).append( strAction ).append( "&page_name=" )
                    .append( escapeHTML( unescapeHTML( link [0] ) ) ).append( " \" title=\"Wikipedia link\">" ).append( strColorBegin )
                    .append( escapeHTML( unescapeHTML( ( ( link.length >= 2 ) && !isEmpty( link [1].trim( ) ) ) ? link [1] : strTopicName ) ) )
                    .append( strColorEnd ).append( "</a>" );
        }
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    protected void appendMacro( String strText )
    {
        if ( "TOC".equals( strText ) )
        {
            super.appendMacro( strText ); // use default
        }
        else
            if ( "My-macro".equals( strText ) )
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
     * 
     * @param strURL
     *            the URL
     * @return The encoded URL
     */
    private static String escapeURL( String strURL )
    {
        try
        {
            return URLEncoder.encode( strURL, "utf-8" );
        }
        catch( UnsupportedEncodingException e )
        {
            AppLogService.error( "LuteceWikiParser : Error escaping Url : " + e.getMessage( ), e );

            return null;
        }
    }

    /**
     * {@inheritDoc  } 
     */
    @Override
    protected void appendNowiki( String strText )
    {
        String strMacro = escapeHTML( replaceString( replaceString( strText, "~{{{", "{{{" ), "~}}}", "}}}" ) );
        sb.append( _macroService.processMacro( strMacro ) );
    }
}
