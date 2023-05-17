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
package fr.paris.lutece.plugins.wiki.business;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.wiki.web.Constants;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.sql.Timestamp;
import java.util.Collection;

/**
 * This class provides instances management methods (create, find, ...) for Topic objects
 */
public final class TopicHome
{
    // Static variable pointed at the DAO instance
    private static ITopicDAO _dao = (ITopicDAO) SpringContextService.getBean( "wiki.topicDAO" );
    private static Plugin _plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private TopicHome( )
    {
    }

    /**
     * Create an instance of the topic class
     * 
     * @param topic
     *            The instance of the Topic which contains the informations to store
     * @return The instance of topic which has been created with its primary key.
     */
    public static Topic create( Topic topic )
    {
        _dao.insert( topic, _plugin );

        return topic;
    }

    /**
     * Update of the topic which is specified in parameter
     * 
     * @param topic
     *            The instance of the Topic which contains the data to store
     * @return The instance of the topic which has been updated
     */
    public static Topic update( Topic topic )
    {
        _dao.store( topic, _plugin );

        return topic;
    }

    /**
     * Remove the topic whose identifier is specified in parameter
     * 
     * @param nTopicId
     *            The topic Id
     */
    public static void remove( int nTopicId )
    {
        TopicVersionHome.removeByTopic( nTopicId );
        ImageHome.removeByTopic( nTopicId );
        _dao.delete( nTopicId, _plugin );
    }

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an instance of a topic whose identifier is specified in parameter
     * 
     * @param nKey
     *            The topic primary key
     * @return an instance of Topic
     */
    public static Topic findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Find a topic by it's primary key
     * 
     * @param strTopicName
     *            The topic name
     * @return The topic
     */
    public static Topic findByPrimaryKey( String strTopicName )
    {
        return _dao.load( strTopicName, _plugin );
    }

    /**
     * Load the data of all the topic objects and returns them in form of a collection
     * 
     * @return the collection which contains the data of all the topic objects
     */
    public static Collection<Topic> getTopicsList( )
    {
        return _dao.selectTopicsList( _plugin );
    }
    /*
    * record the last time view modify page has been open and by who
    */
    public static void updateLastOpenModifyPage(int topicId, User user)
    {Timestamp date = new Timestamp(System.currentTimeMillis());
        String userName = user.getFirstName() + "_" + user.getLastName();
        _dao.updateLastOpenModifyPage(topicId, userName,  date, _plugin);
    }
}
