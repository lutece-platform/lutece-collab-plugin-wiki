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
import java.util.Map;

/**
 * Data access methods for permissions granted on a directory attribute value.
 */
public final class WikiItemAttributePermissionDAO implements IWikiItemAttributePermissionDAO
{
    private static final String COLUMNS = "id_item, attribute_name, attribute_value, attribute_label, permission_type";

    private static final String SQL_QUERY_INSERT = "INSERT IGNORE INTO wiki_item_attribute_permission ( id_item, attribute_name, attribute_value, attribute_label, permission_type ) VALUES ( ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_DELETE = "DELETE FROM wiki_item_attribute_permission WHERE id_item = ? AND attribute_name = ? AND attribute_value = ? AND permission_type = ?";
    private static final String SQL_QUERY_SELECT_BY_ITEM_AND_TYPE = "SELECT " + COLUMNS
            + " FROM wiki_item_attribute_permission WHERE id_item = ? AND permission_type = ? ORDER BY attribute_label, attribute_value";
    private static final String SQL_QUERY_SELECT_BY_ATTRIBUTES = "SELECT " + COLUMNS + " FROM wiki_item_attribute_permission WHERE ";
    private static final String SQL_CONDITION_ATTRIBUTE = "( attribute_name = ? AND attribute_value = ? )";
    private static final String SQL_OR = " OR ";

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( WikiItemAttributePermission permission, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, permission.getIdItem( ) );
            daoUtil.setString( nIndex++, permission.getAttributeName( ) );
            daoUtil.setString( nIndex++, permission.getAttributeValue( ) );
            daoUtil.setString( nIndex++, permission.getAttributeLabel( ) );
            daoUtil.setString( nIndex, permission.getPermissionType( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nIdItem, String strAttributeName, String strAttributeValue, String strPermissionType, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, nIdItem );
            daoUtil.setString( nIndex++, strAttributeName );
            daoUtil.setString( nIndex++, strAttributeValue );
            daoUtil.setString( nIndex, strPermissionType );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WikiItemAttributePermission> selectByItemAndType( int nIdItem, String strPermissionType, Plugin plugin )
    {
        List<WikiItemAttributePermission> list = new ArrayList<>( );
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
    public List<WikiItemAttributePermission> selectByAttributeValues( Map<String, String> mapUserAttributes, Plugin plugin )
    {
        List<WikiItemAttributePermission> list = new ArrayList<>( );

        if ( mapUserAttributes == null )
        {
            return list;
        }

        List<String> listConditions = new ArrayList<>( );
        List<String> listParameters = new ArrayList<>( );

        for ( Map.Entry<String, String> entry : mapUserAttributes.entrySet( ) )
        {
            if ( entry.getValue( ) != null && !entry.getValue( ).isBlank( ) )
            {
                listConditions.add( SQL_CONDITION_ATTRIBUTE );
                listParameters.add( entry.getKey( ) );
                listParameters.add( entry.getValue( ) );
            }
        }

        if ( listConditions.isEmpty( ) )
        {
            return list;
        }

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_ATTRIBUTES + String.join( SQL_OR, listConditions ), plugin ) )
        {
            int nIndex = 1;
            for ( String strParameter : listParameters )
            {
                daoUtil.setString( nIndex++, strParameter );
            }
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                list.add( dataToObject( daoUtil ) );
            }
        }
        return list;
    }

    /**
     * Builds a rule from the current row.
     *
     * @param daoUtil
     *            the DAO holding the current row
     * @return the rule
     */
    private WikiItemAttributePermission dataToObject( DAOUtil daoUtil )
    {
        WikiItemAttributePermission permission = new WikiItemAttributePermission( );
        permission.setIdItem( daoUtil.getInt( "id_item" ) );
        permission.setAttributeName( daoUtil.getString( "attribute_name" ) );
        permission.setAttributeValue( daoUtil.getString( "attribute_value" ) );
        permission.setAttributeLabel( daoUtil.getString( "attribute_label" ) );
        permission.setPermissionType( daoUtil.getString( "permission_type" ) );
        return permission;
    }
}
