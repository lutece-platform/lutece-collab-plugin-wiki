/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.permission;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;

/**
 * Data access interface for user permissions on wiki items.
 */
public interface IWikiItemUserPermissionDAO
{
    /**
     * Inserts a permission, ignoring duplicates.
     *
     * @param permission
     *            the permission to insert
     * @param plugin
     *            the plugin
     */
    void insert( WikiItemUserPermission permission, Plugin plugin );

    /**
     * Deletes one permission of a user on an item.
     *
     * @param nIdItem
     *            the item identifier
     * @param strUserGuid
     *            the user guid
     * @param strPermissionType
     *            the permission type
     * @param plugin
     *            the plugin
     */
    void delete( int nIdItem, String strUserGuid, String strPermissionType, Plugin plugin );

    /**
     * Selects the permissions granted on an item for a permission type.
     *
     * @param nIdItem
     *            the item identifier
     * @param strPermissionType
     *            the permission type
     * @param plugin
     *            the plugin
     * @return the permissions, ordered by display name
     */
    List<WikiItemUserPermission> selectByItemAndType( int nIdItem, String strPermissionType, Plugin plugin );

    /**
     * Selects every permission held by a user, across all items.
     *
     * @param strUserGuid
     *            the user guid
     * @param plugin
     *            the plugin
     * @return the permissions held by the user
     */
    List<WikiItemUserPermission> selectByUser( String strUserGuid, Plugin plugin );
}
