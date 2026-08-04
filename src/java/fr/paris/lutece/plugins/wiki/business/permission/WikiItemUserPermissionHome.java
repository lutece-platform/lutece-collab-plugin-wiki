/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.permission;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

/**
 * Home for user permissions on wiki items.
 */
public final class WikiItemUserPermissionHome
{
    private static final IWikiItemUserPermissionDAO _dao = SpringContextService.getBean( "wiki.wikiItemUserPermissionDAO" );
    private static final Plugin _plugin = PluginService.getPlugin( "wiki" );

    /**
     * Private constructor.
     */
    private WikiItemUserPermissionHome( )
    {
    }

    /**
     * Grants a permission to a user on an item. Does nothing when already granted.
     *
     * @param nIdItem
     *            the item identifier
     * @param strUserGuid
     *            the user guid
     * @param strUserDisplayName
     *            the user name to display
     * @param strPermissionType
     *            the permission type
     */
    public static void create( int nIdItem, String strUserGuid, String strUserDisplayName, String strPermissionType )
    {
        WikiItemUserPermission permission = new WikiItemUserPermission( );
        permission.setIdItem( nIdItem );
        permission.setUserGuid( strUserGuid );
        permission.setUserDisplayName( strUserDisplayName != null ? strUserDisplayName : strUserGuid );
        permission.setPermissionType( strPermissionType );
        _dao.insert( permission, _plugin );
    }

    /**
     * Revokes a permission held by a user on an item.
     *
     * @param nIdItem
     *            the item identifier
     * @param strUserGuid
     *            the user guid
     * @param strPermissionType
     *            the permission type
     */
    public static void remove( int nIdItem, String strUserGuid, String strPermissionType )
    {
        _dao.delete( nIdItem, strUserGuid, strPermissionType, _plugin );
    }

    /**
     * Returns the permissions granted on an item for a permission type.
     *
     * @param nIdItem
     *            the item identifier
     * @param strPermissionType
     *            the permission type
     * @return the permissions, ordered by display name
     */
    public static List<WikiItemUserPermission> findByItemAndType( int nIdItem, String strPermissionType )
    {
        return _dao.selectByItemAndType( nIdItem, strPermissionType, _plugin );
    }

    /**
     * Returns every permission held by a user, across all items.
     *
     * @param strUserGuid
     *            the user guid
     * @return the permissions held by the user
     */
    public static List<WikiItemUserPermission> findByUser( String strUserGuid )
    {
        return _dao.selectByUser( strUserGuid, _plugin );
    }
}
