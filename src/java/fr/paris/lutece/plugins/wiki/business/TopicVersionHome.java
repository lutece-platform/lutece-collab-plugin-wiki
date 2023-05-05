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

import fr.paris.lutece.plugins.wiki.web.Constants;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.Collection;

/**
 * This class provides instances management methods (create, find, ...) for TopicVersion objects
 */
public final class TopicVersionHome
{
    // Static variable pointed at the DAO instance
    private static ITopicVersionDAO _dao = (ITopicVersionDAO) SpringContextService.getBean( "wiki.topicVersionDAO" );
    private static Plugin _plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );

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
     * @return The instance of topicVersion which has been created with its primary key.
     */
    public static TopicVersion create( TopicVersion topicVersion )
    {
        _dao.insert( topicVersion, _plugin );

        return topicVersion;
    }

    /**
     * Remove the topicVersion whose identifier is specified in parameter
     * 
     * @param nTopicVersionId
     *            The topicVersion Id
     */
    public static void remove( int nTopicVersionId )
    {
        _dao.delete( nTopicVersionId, _plugin );
    }

    /**
     * Remove all topicVersions for a given topic
     * 
     * @param nTopicId
     *            The Topic Id
     */
    static void removeByTopic( int nTopicId )
    {
        _dao.deleteByTopic( nTopicId, _plugin );
    }

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an instance of a topicVersion whose identifier is specified in parameter
     * 
     * @param nKey
     *            The topicVersion primary key
     * @return an instance of TopicVersion
     */
    public static TopicVersion findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Load the data of all the topicVersion objects and returns them in form of a collection
     * 
     * @return the collection which contains the data of all the topicVersion objects
     */
    public static Collection<TopicVersion> getTopicVersionsList( )
    {
        return _dao.selectTopicVersionsList( _plugin );
    }

    /**
     * Modify the topic's content
     * 
     * @param topicVersion
     *            The topic version
     */
    public static void addTopicVersion( TopicVersion topicVersion )
    {
        _dao.addTopicVersion( topicVersion, _plugin );
    }

    /**
     * Find the last version
     * 
     * @param idTopic
     *            The topic id
     * @return The topic version
     */
    public static TopicVersion findLastVersion( int idTopic )
    {
        return _dao.loadLastVersion( idTopic, _plugin );
    }

    /**
     * Find all versions
     * 
     * @param idTopic
     *            The topic id
     * @return A collection of topic version
     */
    public static Collection<TopicVersion> findAllVersions( int idTopic )
    {
        return _dao.loadAllVersions( idTopic, _plugin );
    }


    public static void updateTopicVersion(TopicVersion topicVersion)
    {
        _dao.updateTopicVersion( topicVersion, _plugin );
    }
    public static void updateContent(TopicVersion topicVersion)
    {
        _dao.updateContent( topicVersion, _plugin );
    }
    public static void cancelPublication(int topicId)
    {
        TopicVersion topicVersionPublished = _dao.getPublishedVersion(topicId, _plugin);
        if(topicVersionPublished != null ) {
            _dao.updateIsPublished( topicVersionPublished.getIdTopicVersion(), false, _plugin );
        }
    }

    public static TopicVersion getPublishedVersion(int topicId)
    {
        return _dao.getPublishedVersion(topicId, _plugin);
    }
}
