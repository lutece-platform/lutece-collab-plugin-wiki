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

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for Image objects
 */
public final class ImageHome
{
    // Static variable pointed at the DAO instance
    private static IImageDAO _dao = (IImageDAO) SpringContextService.getBean( "wiki.imageDAO" );
    private static Plugin _plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private ImageHome( )
    {
    }

    /**
     * Creation of an instance of image
     *
     * @param image
     *            The instance of image which contains the informations to store
     */
    public static void create( Image image )
    {
        _dao.insert( image, _plugin );
    }

    /**
     * Update of image which is specified in parameter
     *
     * @param image
     *            The instance of image which contains the informations to update
     */
    public static void update( Image image )
    {
        _dao.store( image, _plugin );
    }

    /**
     * Update metatdata of image which is specified in parameter
     *
     * @param image
     *            The instance of image which contains the informations to update
     */
    public static void updateMetadata( Image image )
    {
        _dao.storeMetadata( image, _plugin );
    }

    /**
     * remove image which is specified in parameter
     *
     * @param nIdImage
     *            The image key to remove
     */
    public static void remove( int nIdImage )
    {
        _dao.delete( nIdImage, _plugin );
    }

    /**
     * remove all images associated to a given topic
     *
     * @param nTopicId
     *            The Topic ID
     */
    public static void removeByTopic( int nTopicId )
    {
        _dao.deleteByTopic( nTopicId, _plugin );
    }

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the image Object
     * 
     * @param nIdImage
     *            the image id
     * @return the Image Object
     */
    public static Image findByPrimaryKey( int nIdImage )
    {
        return _dao.load( nIdImage, _plugin );
    }

    /**
     * return the list of all image
     * 
     * @return a list of image
     */
    public static List<Image> getListImages( )
    {
        return _dao.selectAll( _plugin );
    }

    /**
     * Find images for a given topic
     * 
     * @param nIdTopic
     *            The topic ID
     * @return The list
     */
    public static List<Image> findByTopic( int nIdTopic )
    {
        return _dao.selectByTopicId( nIdTopic, _plugin );
    }
}
