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
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * class  ImageDAO
 *
 */
public class ImageDAO implements IImageDAO
{
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_image ) FROM wiki_image";
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = "SELECT id_image,name,mime_type,file_value,id_topic,width,height" +
        " FROM wiki_image WHERE id_image=?";
    private static final String SQL_QUERY_SELECT_ICON = "SELECT id_image,name,mime_type,id_topic,width,height" +
        " FROM wiki_image ORDER BY name DESC  ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO  wiki_image " +
        "(id_image,name,mime_type,file_value,id_topic,width,height)VALUES(?,?,?,?,?,?,?)";
    private static final String SQL_QUERY_UPDATE = "UPDATE wiki_image  SET id_image=?,name=?,mime_type=?,file_value=?,id_topic=?,width=?,height=?" +
        " WHERE id_image=?";
    private static final String SQL_QUERY_UPDATE_METADATA = "UPDATE wiki_image  SET id_image=?,name=?,id_topic=?,width=?,height=?" +
        " WHERE id_image=?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM wiki_image  WHERE id_image=? ";
    private static final String SQL_QUERY_DELETE_BY_TOPIC = "DELETE FROM wiki_image  WHERE id_topic=? ";
    private static final String SQL_QUERY_FIND_BY_TOPIC = "SELECT id_image,name,mime_type,id_topic,width,height" +
        " FROM wiki_image WHERE id_topic=?";

    /**
         * Generates a new primary key
         *
         * @param plugin the plugin
         * @return The new primary key
         */
    private int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery(  );

        int nKey;

        daoUtil.next(  );
        nKey = daoUtil.getInt( 1 ) + 1;
        daoUtil.free(  );

        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void insert( Image image, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        int nPos = 0;
        image.setId( newPrimaryKey( plugin ) );

        daoUtil.setInt( ++nPos, image.getId(  ) );
        daoUtil.setString( ++nPos, image.getName(  ) );
        daoUtil.setString( ++nPos, image.getMimeType(  ) );
        daoUtil.setBytes( ++nPos, image.getValue(  ) );
        daoUtil.setInt( ++nPos, image.getTopicId(  ) );
        daoUtil.setInt( ++nPos, image.getWidth(  ) );
        daoUtil.setInt( ++nPos, image.getHeight(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( Image image, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );

        int nPos = 0;

        daoUtil.setInt( ++nPos, image.getId(  ) );
        daoUtil.setString( ++nPos, image.getName(  ) );
        daoUtil.setString( ++nPos, image.getMimeType(  ) );
        daoUtil.setBytes( ++nPos, image.getValue(  ) );
        daoUtil.setInt( ++nPos, image.getTopicId(  ) );
        daoUtil.setInt( ++nPos, image.getWidth(  ) );
        daoUtil.setInt( ++nPos, image.getHeight(  ) );

        daoUtil.setInt( ++nPos, image.getId(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void storeMetadata( Image image, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_METADATA, plugin );

        int nPos = 0;

        daoUtil.setInt( ++nPos, image.getId(  ) );
        daoUtil.setString( ++nPos, image.getName(  ) );
        daoUtil.setInt( ++nPos, image.getTopicId(  ) );
        daoUtil.setInt( ++nPos, image.getWidth(  ) );
        daoUtil.setInt( ++nPos, image.getHeight(  ) );
        daoUtil.setInt( ++nPos, image.getId(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Image load( int nIdImage, Plugin plugin )
    {
        Image image = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY, plugin );

        daoUtil.setInt( 1, nIdImage );

        daoUtil.executeQuery(  );

        int nPos = 0;

        if ( daoUtil.next(  ) )
        {
            image = new Image(  );
            image.setId( daoUtil.getInt( ++nPos ) );
            image.setName( daoUtil.getString( ++nPos ) );
            image.setMimeType( daoUtil.getString( ++nPos ) );
            image.setValue( daoUtil.getBytes( ++nPos ) );
            image.setTopicId( daoUtil.getInt( ++nPos ) );
            image.setWidth( daoUtil.getInt( ++nPos ) );
            image.setHeight( daoUtil.getInt( ++nPos ) );
        }

        daoUtil.free(  );

        return image;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nIdImage, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );

        daoUtil.setInt( 1, nIdImage );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteByTopic( int nTopicId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_BY_TOPIC, plugin );

        daoUtil.setInt( 1, nTopicId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Image> selectAll( Plugin plugin )
    {
        Image image = null;
        List<Image> listImage = new ArrayList<Image>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ICON, plugin );
        daoUtil.executeQuery(  );

        int nPos;

        while ( daoUtil.next(  ) )
        {
            nPos = 0;
            image = new Image(  );
            image.setId( daoUtil.getInt( ++nPos ) );
            image.setName( daoUtil.getString( ++nPos ) );
            image.setMimeType( daoUtil.getString( ++nPos ) );
            image.setTopicId( daoUtil.getInt( ++nPos ) );
            image.setWidth( daoUtil.getInt( ++nPos ) );
            image.setHeight( daoUtil.getInt( ++nPos ) );

            listImage.add( image );
        }

        daoUtil.free(  );

        return listImage;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Image> selectByTopicId( int nTopicId, Plugin plugin )
    {
        Image image = null;
        List<Image> listImage = new ArrayList<Image>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_TOPIC, plugin );
        daoUtil.setInt( 1, nTopicId );
        daoUtil.executeQuery(  );

        int nPos;

        while ( daoUtil.next(  ) )
        {
            nPos = 0;
            image = new Image(  );
            image.setId( daoUtil.getInt( ++nPos ) );
            image.setName( daoUtil.getString( ++nPos ) );
            image.setMimeType( daoUtil.getString( ++nPos ) );
            image.setTopicId( daoUtil.getInt( ++nPos ) );
            image.setWidth( daoUtil.getInt( ++nPos ) );
            image.setHeight( daoUtil.getInt( ++nPos ) );

            listImage.add( image );
        }

        daoUtil.free(  );

        return listImage;
    }
}
