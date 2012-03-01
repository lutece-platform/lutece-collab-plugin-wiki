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
package fr.paris.lutece.plugins.wiki.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.Collection;


/**
 * This class provides instances management methods (create, find, ...) for Topic objects
 */
public final class TopicHome
{
    // Static variable pointed at the DAO instance
    private static ITopicDAO _dao = (ITopicDAO) SpringContextService.getPluginBean( "wiki", "topicDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private TopicHome(  )
    {
    }

    /**
     * Create an instance of the topic class
     * @param topic The instance of the Topic which contains the informations to store
     * @param plugin the Plugin
     * @return The  instance of topic which has been created with its primary key.
     */
    public static Topic create( Topic topic, Plugin plugin )
    {
        _dao.insert( topic, plugin );

        return topic;
    }

    /**
     * Update of the topic which is specified in parameter
     * @param topic The instance of the Topic which contains the data to store
     * @param plugin the Plugin
     * @return The instance of the  topic which has been updated
     */
    public static Topic update( Topic topic, Plugin plugin )
    {
        _dao.store( topic, plugin );

        return topic;
    }

    /**
     * Remove the topic whose identifier is specified in parameter
     * @param nTopicId The topic Id
     * @param plugin the Plugin
     */
    public static void remove( int nTopicId, Plugin plugin )
    {
        _dao.delete( nTopicId, plugin );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an instance of a topic whose identifier is specified in parameter
     * @param nKey The topic primary key
     * @param plugin the Plugin
     * @return an instance of Topic
     */
    public static Topic findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
    }

    public static Topic findByPrimaryKey( String strTopicName, Plugin plugin )
    {
        return _dao.load( strTopicName, plugin );
    }

    public static void modifyContentOnly( String strTopicName, String strContent, Plugin plugin )
    {
        _dao.modify( strTopicName, strContent, plugin );
    }

    /**
     * Load the data of all the topic objects and returns them in form of a collection
     * @param plugin the Plugin
     * @return the collection which contains the data of all the topic objects
     */
    public static Collection<Topic> getTopicsList( Plugin plugin )
    {
        return _dao.selectTopicsList( plugin );
    }
}
