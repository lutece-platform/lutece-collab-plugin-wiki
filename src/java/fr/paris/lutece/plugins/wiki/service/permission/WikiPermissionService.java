/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.permission;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemUserPermission;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemUserPermissionHome;
import fr.paris.lutece.plugins.wiki.service.user.WikiUserDisplayName;
import fr.paris.lutece.portal.service.security.LuteceUser;

import java.util.Collections;
import java.util.List;

/**
 * Grants and revokes permissions held by named users on wiki items.
 *
 * Permissions are stored as wiki data, so a grant or a revoke takes effect on the very next
 * request of the target user, without them having to sign in again.
 */
public final class WikiPermissionService
{
    /**
     * Marker used by the item forms to mean "only the creator and the users explicitly listed".
     */
    public static final String ROLE_PRIVATE = "private";

    /**
     * Marker used by the item forms to mean "no restriction".
     */
    public static final String ROLE_NONE = "none";

    /**
     * Private constructor.
     */
    private WikiPermissionService( )
    {
    }

    /**
     * Grants a permission to a user on an item. Does nothing when the permission type is unknown
     * or when the permission is already granted.
     *
     * @param item
     *            the wiki item
     * @param strUserGuid
     *            the user identifier, as returned by LuteceUser#getName
     * @param strDisplayName
     *            the user name to display, falls back to the identifier when null
     * @param strPermissionType
     *            VIEW or EDIT
     */
    public static void grant( AbstractWikiItem item, String strUserGuid, String strDisplayName, String strPermissionType )
    {
        if ( isApplicable( item, strUserGuid, strPermissionType ) )
        {
            WikiItemUserPermissionHome.create( item.getId( ), strUserGuid, strDisplayName, strPermissionType );
        }
    }

    /**
     * Revokes a permission held by a user on an item.
     *
     * @param item
     *            the wiki item
     * @param strUserGuid
     *            the user identifier
     * @param strPermissionType
     *            VIEW or EDIT
     */
    public static void revoke( AbstractWikiItem item, String strUserGuid, String strPermissionType )
    {
        if ( isApplicable( item, strUserGuid, strPermissionType ) )
        {
            WikiItemUserPermissionHome.remove( item.getId( ), strUserGuid, strPermissionType );
        }
    }

    /**
     * Grants the creator the right to edit the item they just created, when that item is private.
     * Without it a creator without the wiki administrator role would lose access to their own item.
     *
     * @param item
     *            the wiki item
     * @param creator
     *            the user creating the item
     */
    public static void grantCreatorPermissions( AbstractWikiItem item, LuteceUser creator )
    {
        if ( item == null || creator == null || creator.getName( ) == null )
        {
            return;
        }

        if ( isPrivate( item.getViewRole( ) ) || isPrivate( item.getEditRole( ) ) )
        {
            grant( item, creator.getName( ), WikiUserDisplayName.of( creator ), WikiItemUserPermission.PERMISSION_EDIT );
        }
    }

    /**
     * Returns the users holding a permission on an item.
     *
     * @param item
     *            the wiki item
     * @param strPermissionType
     *            VIEW or EDIT
     * @return the permissions, ordered by display name
     */
    public static List<WikiItemUserPermission> getUsersWithPermission( AbstractWikiItem item, String strPermissionType )
    {
        if ( item == null || !isValidPermissionType( strPermissionType ) )
        {
            return Collections.emptyList( );
        }

        return WikiItemUserPermissionHome.findByItemAndType( item.getId( ), strPermissionType );
    }

    /**
     * Tells whether a role value means "private".
     *
     * @param strRole
     *            the role value carried by an item
     * @return true when the value is the private marker
     */
    private static boolean isPrivate( String strRole )
    {
        return ROLE_PRIVATE.equalsIgnoreCase( strRole );
    }

    /**
     * Tells whether a grant or a revoke request can be applied.
     *
     * @param item
     *            the wiki item
     * @param strUserGuid
     *            the user identifier
     * @param strPermissionType
     *            the permission type
     * @return true when the request carries everything needed
     */
    private static boolean isApplicable( AbstractWikiItem item, String strUserGuid, String strPermissionType )
    {
        return item != null && strUserGuid != null && !strUserGuid.isBlank( ) && isValidPermissionType( strPermissionType );
    }

    /**
     * Tells whether a permission type is supported.
     *
     * @param strPermissionType
     *            the permission type to check
     * @return true when VIEW or EDIT
     */
    private static boolean isValidPermissionType( String strPermissionType )
    {
        return WikiItemUserPermission.PERMISSION_VIEW.equals( strPermissionType ) || WikiItemUserPermission.PERMISSION_EDIT.equals( strPermissionType );
    }
}
