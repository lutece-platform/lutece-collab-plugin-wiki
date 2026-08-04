/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.permission;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access methods for user permissions on wiki items.
 */
public final class WikiItemUserPermissionDAO implements IWikiItemUserPermissionDAO
{
    private static final String COLUMNS = "id_item, user_guid, user_display_name, permission_type";

    private static final String SQL_QUERY_INSERT = "INSERT IGNORE INTO wiki_item_user_permission ( id_item, user_guid, user_display_name, permission_type ) VALUES ( ?, ?, ?, ? )";
    private static final String SQL_QUERY_DELETE = "DELETE FROM wiki_item_user_permission WHERE id_item = ? AND user_guid = ? AND permission_type = ?";
    private static final String SQL_QUERY_SELECT_BY_ITEM_AND_TYPE = "SELECT " + COLUMNS
            + " FROM wiki_item_user_permission WHERE id_item = ? AND permission_type = ? ORDER BY user_display_name";
    private static final String SQL_QUERY_SELECT_BY_USER = "SELECT " + COLUMNS + " FROM wiki_item_user_permission WHERE user_guid = ?";

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( WikiItemUserPermission permission, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, permission.getIdItem( ) );
            daoUtil.setString( nIndex++, permission.getUserGuid( ) );
            daoUtil.setString( nIndex++, permission.getUserDisplayName( ) );
            daoUtil.setString( nIndex, permission.getPermissionType( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nIdItem, String strUserGuid, String strPermissionType, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, nIdItem );
            daoUtil.setString( nIndex++, strUserGuid );
            daoUtil.setString( nIndex, strPermissionType );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WikiItemUserPermission> selectByItemAndType( int nIdItem, String strPermissionType, Plugin plugin )
    {
        List<WikiItemUserPermission> list = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_ITEM_AND_TYPE, plugin ) )
        {
            daoUtil.setInt( 1, nIdItem );
            daoUtil.setString( 2, strPermissionType );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                list.add( dataToObject( daoUtil ) );
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WikiItemUserPermission> selectByUser( String strUserGuid, Plugin plugin )
    {
        List<WikiItemUserPermission> list = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_USER, plugin ) )
        {
            daoUtil.setString( 1, strUserGuid );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                list.add( dataToObject( daoUtil ) );
            }
        }
        return list;
    }

    /**
     * Builds a permission from the current row.
     *
     * @param daoUtil
     *            the DAO holding the current row
     * @return the permission
     */
    private WikiItemUserPermission dataToObject( DAOUtil daoUtil )
    {
        WikiItemUserPermission permission = new WikiItemUserPermission( );
        permission.setIdItem( daoUtil.getInt( "id_item" ) );
        permission.setUserGuid( daoUtil.getString( "user_guid" ) );
        permission.setUserDisplayName( daoUtil.getString( "user_display_name" ) );
        permission.setPermissionType( daoUtil.getString( "permission_type" ) );
        return permission;
    }
}
