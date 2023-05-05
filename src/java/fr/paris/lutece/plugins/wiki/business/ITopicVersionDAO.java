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

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.Collection;

/**
 * ITopicVersionDAO Interface
 */
public interface ITopicVersionDAO
{
    /**
     * Insert a new record in the table.
     *
     * @param topicVersion
     *            instance of the TopicVersion object to inssert
     * @param plugin
     *            the Plugin
     */
    void insert( TopicVersion topicVersion, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nIdTopicVersion
     *            int identifier of the TopicVersion to delete
     * @param plugin
     *            the Plugin
     */
    void delete( int nIdTopicVersion, Plugin plugin );

    // /////////////////////////////////////////////////////////////////////////
    // Finders
    /**
     * Load the data from the table
     *
     * @param nKey
     *            The key
     * @param plugin
     *            the Plugin
     * @return The instance of the topicVersion
     */
    TopicVersion load( int nKey, Plugin plugin );

    void deleteByTopicVersion(int nTopicId, Plugin plugin);

    void updateIsPublished(int nIdTopicVersion, boolean bIsPublished, Plugin plugin);

    /**
     * Load the data of all the topicVersion objects and returns them as a collection
     *
     * @param plugin
     *            the Plugin
     * @return The collection which contains the data of all the topicVersion objects
     */
    Collection<TopicVersion> selectTopicVersionsList( Plugin plugin );

    /**
     * Modify the topic version
     *
     * @param topicVersion
     *            The topic version
     * @param plugin
     *            The plugin
     */
    void addTopicVersion( TopicVersion topicVersion, Plugin plugin );

    void updateTopicVersion(TopicVersion topicVersion, Plugin plugin);

    /**
     * Load last version
     *
     * @param idTopic
     *            The topic ID
     * @param plugin
     *            The plugin
     * @return The topic version
     */
    TopicVersion loadLastVersion( int idTopic, Plugin plugin );

    TopicVersion getPublishedVersion(int nTopicId, Plugin plugin);

    /**
     * Load all versions
     *
     * @param idTopic
     *            The topic id
     * @param plugin
     *            The plugin
     * @return The list of topics
     */
    Collection<TopicVersion> loadAllVersions( int idTopic, Plugin plugin );

    /**
     * Remove all topicVersions for a given topic
     * 
     * @param nTopicId
     *            The Topic Id
     * @param plugin
     *            the Plugin
     */
    void deleteByTopic( int nTopicId, Plugin plugin );


}
