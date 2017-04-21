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

package fr.paris.lutece.plugins.wiki.service;

import fr.paris.lutece.plugins.wiki.business.TopicVersion;
import fr.paris.lutece.plugins.wiki.service.parser.LuteceWikiParser;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import java.util.Locale;

/**
 * Wiki Service
 */
public class WikiService extends AbstractCacheableService
{
    private static final String SERVICE_CACHE_NAME = "Wiki Cache Service";

    private static WikiService _singleton;

    /** Private Constructor */
    private WikiService( )
    {
        initCache( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName( )
    {
        return SERVICE_CACHE_NAME;
    }

    /**
     * Returns the unique instance
     * 
     * @return The instance
     */
    public static synchronized WikiService instance( )
    {
        if ( _singleton == null )
        {
            _singleton = new WikiService( );
        }
        return _singleton;
    }

    /**
     * Get the Wiki page in HTML format
     * 
     * @param strPageName
     *            The page name
     * @param version
     *            The content version
     * @param strPageUrl
     *            The page URL
     * @param strLanguage
     *            The language
     * @return The HTML code
     */
    public synchronized String getWikiPage( String strPageName, TopicVersion version, String strPageUrl , String strLanguage )
    {
        StringBuilder sbKey = new StringBuilder( );
        sbKey.append( strPageName ).append( "[" ).append( version.getIdTopicVersion( ) ).append( "]" );
        sbKey.append( ( ( strPageUrl != null ) ? "[Url]" : "[noUrl]" ) );
        sbKey.append( ":").append( strLanguage );
        String strPageContent = (String) getFromCache( sbKey.toString( ) );
        if ( strPageContent == null )
        {
            String strContent = version.getWikiContent( strLanguage ).getWikiContent();
            strPageContent = new LuteceWikiParser( strContent , strPageUrl ).toString( );
            putInCache( sbKey.toString( ), strPageContent );
        }
        return strPageContent;
    }

    /**
     * Get the Wiki page in HTML format
     * 
     * @param strPageName
     *            The page name
     * @param version
     *            The topic version 
     * @param strLanguage
     *            The language
     * @return The HTML code
     */
    public synchronized String getWikiPage( String strPageName, TopicVersion version , String strLanguage )
    {
        return getWikiPage( strPageName, version, null , strLanguage );
    }

    /**
     * Render the wiki content for the editor (convert special characters)
     * 
     * @param version
     *            The version
     * @return The content
     */
    public static String renderEditor( TopicVersion version , Locale locale )
    {
        return LuteceWikiParser.renderWiki( version.getWikiContent( locale.getLanguage() ).getWikiContent() );
    }

}
