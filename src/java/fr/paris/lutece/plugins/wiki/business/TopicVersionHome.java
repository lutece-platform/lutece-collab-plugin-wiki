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
package fr.paris.lutece.plugins.wiki.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.Collection;

/**
 * This class provides instances management methods (create, find, ...) for TopicVersion objects
 */
public final class TopicVersionHome
{
    // Static variable pointed at the DAO instance
    private static ITopicVersionDAO _dao = (ITopicVersionDAO) SpringContextService.getBean( "wiki.topicVersionDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private TopicVersionHome( )
    {
    }

    /**
     * Create an instance of the topicVersion class
     * 
     * @param topicVersion
     *            The instance of the TopicVersion which contains the informations to store
     * @param plugin
     *            the Plugin
     * @return The instance of topicVersion which has been created with its primary key.
     */
    public static TopicVersion create( TopicVersion topicVersion, Plugin plugin )
    {
        _dao.insert( topicVersion, plugin );

        return topicVersion;
    }

    /**
     * Update of the topicVersion which is specified in parameter
     * 
     * @param topicVersion
     *            The instance of the TopicVersion which contains the data to store
     * @param plugin
     *            the Plugin
     * @return The instance of the topicVersion which has been updated
     */
    public static TopicVersion update( TopicVersion topicVersion, Plugin plugin )
    {
        _dao.store( topicVersion, plugin );

        return topicVersion;
    }

    /**
     * Remove the topicVersion whose identifier is specified in parameter
     * 
     * @param nTopicVersionId
     *            The topicVersion Id
     * @param plugin
     *            the Plugin
     */
    public static void remove( int nTopicVersionId, Plugin plugin )
    {
        _dao.delete( nTopicVersionId, plugin );
    }

    /**
     * Remove all topicVersions for a given topic
     * 
     * @param nTopicId
     *            The Topic Id
     * @param plugin
     *            the Plugin
     */
    static void removeByTopic( int nTopicId, Plugin plugin )
    {
        _dao.deleteByTopic( nTopicId, plugin );
    }

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an instance of a topicVersion whose identifier is specified in parameter
     * 
     * @param nKey
     *            The topicVersion primary key
     * @param plugin
     *            the Plugin
     * @return an instance of TopicVersion
     */
    public static TopicVersion findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
    }

    /**
     * Load the data of all the topicVersion objects and returns them in form of a collection
     * 
     * @param plugin
     *            the Plugin
     * @return the collection which contains the data of all the topicVersion objects
     */
    public static Collection<TopicVersion> getTopicVersionsList( Plugin plugin )
    {
        return _dao.selectTopicVersionsList( plugin );
    }

    /**
     * Modify the topic's content
     * 
     * @param nTopicId
     *            The topic ID
     * @param strUserName
     *            The username
     * @param strComment
     *            The comment
     * @param strContent
     *            The content
     * @param nPreviousVersion
     *            The prvious version
     * @param plugin
     *            The plugin
     */
    public static void modifyContentOnly( int nTopicId, String strUserName, String strComment, String strContent, int nPreviousVersion, Plugin plugin )
    {
        _dao.modifyTopicVersion( nTopicId, strUserName, strComment, strContent, nPreviousVersion, plugin );
    }

    /**
     * Find the last version
     * 
     * @param idTopic
     *            The topic id
     * @param plugin
     *            The plugin
     * @return The topic version
     */
    public static TopicVersion findLastVersion( int idTopic, Plugin plugin )
    {
        return _dao.loadLastVersion( idTopic, plugin );
    }

    /**
     * Find all versions
     * 
     * @param idTopic
     *            The topic id
     * @param plugin
     *            The plugin
     * @return A collection of topic version
     */
    public static Collection<TopicVersion> findAllVersions( int idTopic, Plugin plugin )
    {
        return _dao.loadAllVersions( idTopic, plugin );
    }
}
