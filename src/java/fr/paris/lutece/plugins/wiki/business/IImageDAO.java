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

import java.util.List;

/**
 *
 * IImageDAO
 *
 */
public interface IImageDAO
{
    /**
     * Insert a new record in the table.
     *
     * @param image
     *            instance of the Image object to insert
     * @param plugin
     *            the plugin
     */
    void insert( Image image, Plugin plugin );

    /**
     * update record in the table.
     *
     * @param image
     *            instance of the Image object to update
     * @param plugin
     *            the plugin
     */
    void store( Image image, Plugin plugin );

    /**
     * update image metadata in the table.
     *
     * @param image
     *            instance of the Image object to update
     * @param plugin
     *            the plugin
     */
    void storeMetadata( Image image, Plugin plugin );

    /**
     * Load the image Object
     * 
     * @param nIdImage
     *            the image id
     * @param plugin
     *            the plugin
     * @return the image Object
     */
    Image load( int nIdImage, Plugin plugin );

    /**
     * Delete the Image Object
     * 
     * @param nIdImage
     *            theimage id
     * @param plugin
     *            the plugin
     */
    void delete( int nIdImage, Plugin plugin );

    /**
     * select all Images
     * 
     * @param plugin
     *            the plugin
     * @return a list of Image
     */
    List<Image> selectAll( Plugin plugin );

    /**
     * select images for a given topic
     * 
     * @param nTopicId
     *            The topic ID
     * @param plugin
     *            The plugin
     * @return The list
     */
    List<Image> selectByTopicId( int nTopicId, Plugin plugin );

    /**
     * remove all images associated to a given topic
     *
     * @param nTopicId
     *            The Topic ID
     * @param plugin
     *            the Plugin
     *
     */
    void deleteByTopic( int nTopicId, Plugin plugin );
}
