/*
 * Copyright (c) 2002-2023, City of Paris
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

import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Diff Service
 */
public final class PathService
{
    private static final String DSKEY_WIKI_ROOT_PAGENAME = "wiki.site_property.path.rootPageName";
    private static final String PAGE_DEFAULT = "home";

    /**
     * Return the ordered parent of a topic
     *
     * @param topic
     *            The topic page
     * @return The list of parent topics
     */
    public static List getParentTopics( Topic topic )
    {
        String strWikiRootPageName = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_PAGENAME, AppPropertiesService.getProperty( PAGE_DEFAULT ) );

        List topicList = new ArrayList( );

        while ( StringUtils.isNotEmpty( topic.getParentPageName( ) ) && !topic.getPageName( ).equals( strWikiRootPageName ) )
        {
            topic = TopicHome.findByPrimaryKey( topic.getParentPageName( ) );

            if ( topic == null || isTopicInList( topicList, topic ) )
            {
                break;
            }

            topicList.add( 0, topic );
        }

        return topicList;
    }

    /**
     * Checks if topic is already in an item of the list
     * 
     * @param list
     *            The list
     * @param topic
     *            The name
     * @return true if the name is in list and false otherwise
     */
    private static boolean isTopicInList( List<Topic> list, Topic topic )
    {
        if ( topic == null )
        {
            return false;
        }

        for ( Topic item : list )
        {
            if ( item.getPageName( ).equals( topic.getPageName( ) ) )
            {
                return true;
            }
        }

        return false;
    }
}
