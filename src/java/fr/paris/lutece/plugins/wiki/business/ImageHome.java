/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import java.util.List;


/**
 * This class provides instances management methods (create, find, ...) for Image  objects
 */
public final class ImageHome
{
    // Static variable pointed at the DAO instance
    private static IImageDAO _dao = (IImageDAO) SpringContextService.getBean( "wiki.imageDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private ImageHome(  )
    {
    }

    /**
     * Creation of an instance of image
     *
     * @param image The instance of image which contains the informations to store
     * @param plugin the plugin
     *
     *
     *
     */
    public static void create( Image image, Plugin plugin )
    {
        _dao.insert( image, plugin );
    }

    /**
     * Update of image which is specified in parameter
     *
     * @param image  The instance of image which contains the informations to update
     * @param plugin the Plugin
     *
     */
    public static void update( Image image, Plugin plugin )
    {
        _dao.store( image, plugin );
    }

    /**
     * Update metatdata of image which is specified in parameter
     *
     * @param image  The instance of image which contains the informations to update
     * @param plugin the Plugin
     *
     */
    public static void updateMetadata( Image image, Plugin plugin )
    {
        _dao.storeMetadata( image, plugin );
    }

    /**
     *  remove image which is specified in parameter
     *
     * @param  nIdImage The image key to remove
     * @param plugin the Plugin
     *
     */
    public static void remove( int nIdImage, Plugin plugin )
    {
        _dao.delete( nIdImage, plugin );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
         * Load the image Object
         * @param nIdImage the image id
         * @param plugin the plugin
         * @return the Image Object
         */
    public static Image findByPrimaryKey( int nIdImage, Plugin plugin )
    {
        return _dao.load( nIdImage, plugin );
    }

    /**
     * return the list of all image
     * @param plugin the plugin
     * @return a list of image
     */
    public static List<Image> getListImages( Plugin plugin )
    {
        return _dao.selectAll( plugin );
    }
}
