/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.wiki.business.item;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WikiItemDAO implements IWikiItemDAO
{
    private static final String SQL_QUERY_SELECT = "SELECT id_item, code, icon, is_published, view_role, edit_role, item_type, id_parent, item_order, date_creation, date_modification FROM wiki_item WHERE id_item = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO wiki_item ( code, icon, is_published, view_role, edit_role, item_type, id_parent, item_order, date_creation, date_modification ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_DELETE = "DELETE FROM wiki_item WHERE id_item = ?";
    private static final String SQL_QUERY_UPDATE = "UPDATE wiki_item SET code = ?, icon = ?, is_published = ?, view_role = ?, edit_role = ?, id_parent = ?, item_order = ?, date_creation = ?, date_modification = ? WHERE id_item = ?";
    private static final String SQL_QUERY_SELECT_BY_TYPE = "SELECT id_item, code, icon, is_published, view_role, edit_role, item_type, id_parent, item_order, date_creation, date_modification FROM wiki_item WHERE item_type = ? ORDER BY item_order";
    private static final String SQL_QUERY_SELECT_BY_PARENT = "SELECT id_item, code, icon, is_published, view_role, edit_role, item_type, id_parent, item_order, date_creation, date_modification FROM wiki_item WHERE id_parent = ? ORDER BY item_order";
    private static final String SQL_QUERY_SELECT_BY_PARENT_AND_TYPE = "SELECT id_item, code, icon, is_published, view_role, edit_role, item_type, id_parent, item_order, date_creation, date_modification FROM wiki_item WHERE id_parent = ? AND item_type = ? ORDER BY item_order";
    private static final String SQL_QUERY_SELECT_BY_CODE = "SELECT id_item, code, icon, is_published, view_role, edit_role, item_type, id_parent, item_order, date_creation, date_modification FROM wiki_item WHERE code = ?";

    private static final String COLUMN_ID_ITEM = "id_item";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_ICON = "icon";
    private static final String COLUMN_IS_PUBLISHED = "is_published";
    private static final String COLUMN_VIEW_ROLE = "view_role";
    private static final String COLUMN_EDIT_ROLE = "edit_role";
    private static final String COLUMN_ITEM_TYPE = "item_type";
    private static final String COLUMN_ID_PARENT = "id_parent";
    private static final String COLUMN_ITEM_ORDER = "item_order";
    private static final String COLUMN_DATE_CREATION = "date_creation";
    private static final String COLUMN_DATE_MODIFICATION = "date_modification";

    /**
     * Creates a new timestamp for the current moment
     *
     * @return the current timestamp
     */
    private static java.sql.Timestamp currentTimestamp( )
    {
        return new java.sql.Timestamp( System.currentTimeMillis( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( AbstractWikiItem wikiItem, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, wikiItem.getCode( ) );
            daoUtil.setString( nIndex++, wikiItem.getIcon( ) );
            daoUtil.setBoolean( nIndex++, wikiItem.isPublished( ) );
            daoUtil.setString( nIndex++, wikiItem.getViewRole( ) );
            daoUtil.setString( nIndex++, wikiItem.getEditRole( ) );
            daoUtil.setString( nIndex++, wikiItem.getType( ).getCode( ) );

            if ( wikiItem.getIdParent( ) != null )
            {
                daoUtil.setInt( nIndex++, wikiItem.getIdParent( ) );
            }
            else
            {
                daoUtil.setIntNull( nIndex++ );
            }

            daoUtil.setInt( nIndex++, wikiItem.getOrder( ) );

            java.sql.Timestamp now = currentTimestamp( );
            daoUtil.setTimestamp( nIndex++, now );
            daoUtil.setTimestamp( nIndex++, now );

            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                wikiItem.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AbstractWikiItem> load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            AbstractWikiItem wikiItem = null;

            if ( daoUtil.next( ) )
            {
                wikiItem = dataToObject( daoUtil );
            }

            return Optional.ofNullable( wikiItem );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( AbstractWikiItem wikiItem, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, wikiItem.getCode( ) );
            daoUtil.setString( nIndex++, wikiItem.getIcon( ) );
            daoUtil.setBoolean( nIndex++, wikiItem.isPublished( ) );
            daoUtil.setString( nIndex++, wikiItem.getViewRole( ) );
            daoUtil.setString( nIndex++, wikiItem.getEditRole( ) );

            if ( wikiItem.getIdParent( ) != null )
            {
                daoUtil.setInt( nIndex++, wikiItem.getIdParent( ) );
            }
            else
            {
                daoUtil.setIntNull( nIndex++ );
            }

            daoUtil.setInt( nIndex++, wikiItem.getOrder( ) );
            daoUtil.setTimestamp( nIndex++, wikiItem.getDateCreation( ) );
            daoUtil.setTimestamp( nIndex++, currentTimestamp( ) );

            daoUtil.setInt( nIndex, wikiItem.getId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractWikiItem> selectWikiItemsByType( WikiItemType type, Plugin plugin )
    {
        List<AbstractWikiItem> wikiItemList = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_TYPE, plugin ) )
        {
            daoUtil.setString( 1, type.getCode( ) );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                wikiItemList.add( dataToObject( daoUtil ) );
            }
        }

        return wikiItemList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractWikiItem> selectWikiItemsByParent( int nIdParent, Plugin plugin )
    {
        List<AbstractWikiItem> wikiItemList = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_PARENT, plugin ) )
        {
            daoUtil.setInt( 1, nIdParent );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                wikiItemList.add( dataToObject( daoUtil ) );
            }
        }

        return wikiItemList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractWikiItem> selectWikiItemsByParentAndType( int nIdParent, WikiItemType type, Plugin plugin )
    {
        List<AbstractWikiItem> wikiItemList = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_PARENT_AND_TYPE, plugin ) )
        {
            daoUtil.setInt( 1, nIdParent );
            daoUtil.setString( 2, type.getCode( ) );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                wikiItemList.add( dataToObject( daoUtil ) );
            }
        }

        return wikiItemList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AbstractWikiItem> selectByCode( String strCode, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CODE, plugin ) )
        {
            daoUtil.setString( 1, strCode );
            daoUtil.executeQuery( );

            AbstractWikiItem wikiItem = null;

            if ( daoUtil.next( ) )
            {
                wikiItem = dataToObject( daoUtil );
            }

            return Optional.ofNullable( wikiItem );
        }
    }

    /**
     * Map database result to wiki item object
     *
     * @param daoUtil
     *            the DAO utility
     * @return the wiki item object
     */
    private AbstractWikiItem dataToObject( DAOUtil daoUtil )
    {
        WikiItemType type = WikiItemType.valueOf( daoUtil.getString( COLUMN_ITEM_TYPE ).toUpperCase( ) );
        AbstractWikiItem wikiItem = WikiItemFactory.create( type );

        wikiItem.setId( daoUtil.getInt( COLUMN_ID_ITEM ) );
        wikiItem.setCode( daoUtil.getString( COLUMN_CODE ) );
        wikiItem.setIcon( daoUtil.getString( COLUMN_ICON ) );
        wikiItem.setIsPublished( daoUtil.getBoolean( COLUMN_IS_PUBLISHED ) );
        wikiItem.setViewRole( daoUtil.getString( COLUMN_VIEW_ROLE ) );
        wikiItem.setEditRole( daoUtil.getString( COLUMN_EDIT_ROLE ) );
        wikiItem.setType( type );

        Integer nIdParent = daoUtil.getObject( COLUMN_ID_PARENT, Integer.class );
        wikiItem.setIdParent( nIdParent );

        wikiItem.setOrder( daoUtil.getInt( COLUMN_ITEM_ORDER ) );
        wikiItem.setDateCreation( daoUtil.getTimestamp( COLUMN_DATE_CREATION ) );
        wikiItem.setDateModification( daoUtil.getTimestamp( COLUMN_DATE_MODIFICATION ) );

        return wikiItem;
    }
}
