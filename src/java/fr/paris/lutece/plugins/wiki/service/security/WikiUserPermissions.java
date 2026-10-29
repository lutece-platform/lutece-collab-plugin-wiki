/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.security;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemAttributePermission;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemAttributePermissionHome;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemUserPermission;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemUserPermissionHome;
import fr.paris.lutece.plugins.wiki.service.user.WikiUserAttributes;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.rbac.RBACHome;
import fr.paris.lutece.portal.service.security.LuteceUser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Snapshot of everything that grants a user access to wiki items: the roles carried by the
 * session, the permissions granted to the user individually or to a population they belong to,
 * and the RBAC permissions granted to the roles the user carries.
 *
 * The roles come from the session, so building a snapshot costs nothing. The reads gathering the
 * granted permissions are issued on the first item check and never again, so a single build
 * serves any number of checks, and a role-only check costs no query at all.
 */
public final class WikiUserPermissions
{
    private static final String KEY_PREFIX_ITEM = "item:";

    private static final Set<String> WIKI_RESOURCE_TYPES = buildWikiResourceTypes( );

    private final Set<String> _setRoles = new HashSet<>( );
    private final Set<String> _setViewKeys = new HashSet<>( );
    private final Set<String> _setEditKeys = new HashSet<>( );

    private final LuteceUser _user;
    private final String _strUserGuid;
    private boolean _bGrantsLoaded;

    /**
     * Private constructor, use {@link #forUser(LuteceUser)}.
     *
     * @param user
     *            the Lutece user, null for an anonymous visitor
     */
    private WikiUserPermissions( LuteceUser user )
    {
        _user = user;
        _strUserGuid = user != null ? user.getName( ) : null;
    }

    /**
     * Builds the snapshot of a user, reading their roles from the session.
     *
     * @param user
     *            the Lutece user, may be null for an anonymous visitor
     * @return the permissions of that user
     */
    public static WikiUserPermissions forUser( LuteceUser user )
    {
        if ( user == null || user.getName( ) == null )
        {
            return new WikiUserPermissions( null );
        }

        WikiUserPermissions permissions = new WikiUserPermissions( user );

        if ( user.getRoles( ) != null )
        {
            permissions._setRoles.addAll( Arrays.asList( user.getRoles( ) ) );
        }

        return permissions;
    }

    /**
     * Tells whether the user carries the given role.
     *
     * @param strRole
     *            the role key
     * @return true when the user carries it
     */
    public boolean hasRole( String strRole )
    {
        return strRole != null && !strRole.isBlank( ) && _setRoles.contains( strRole );
    }

    /**
     * Tells whether the user is allowed to view the item, either individually or through a role.
     *
     * @param item
     *            the wiki item
     * @return true when allowed
     */
    public boolean isAllowedToView( AbstractWikiItem item )
    {
        return isAllowed( item, _setViewKeys );
    }

    /**
     * Tells whether the user is allowed to edit the item, either individually or through a role.
     *
     * @param item
     *            the wiki item
     * @return true when allowed
     */
    public boolean isAllowedToEdit( AbstractWikiItem item )
    {
        return isAllowed( item, _setEditKeys );
    }

    /**
     * Answers an access question from the loaded keys, loading them on first use.
     *
     * @param item
     *            the wiki item
     * @param setKeys
     *            the keys granting the permission being checked
     * @return true when allowed
     */
    private boolean isAllowed( AbstractWikiItem item, Set<String> setKeys )
    {
        if ( item == null || _strUserGuid == null )
        {
            return false;
        }

        loadGrants( );

        String strType = item.getResourceTypeCode( );
        return setKeys.contains( KEY_PREFIX_ITEM + item.getId( ) ) || setKeys.contains( toResourceKey( strType, item.getResourceId( ) ) )
                || setKeys.contains( toResourceKey( strType, RBAC.WILDCARD_RESOURCES_ID ) );
    }

    /**
     * Reads the granted permissions from the database, once per snapshot.
     */
    private void loadGrants( )
    {
        if ( _bGrantsLoaded )
        {
            return;
        }

        loadUserPermissions( );
        loadAttributePermissions( );
        loadRbacPermissions( );

        _bGrantsLoaded = true;
    }

    /**
     * Loads the permissions granted to the user individually.
     */
    private void loadUserPermissions( )
    {
        for ( WikiItemUserPermission permission : WikiItemUserPermissionHome.findByUser( _strUserGuid ) )
        {
            addItemGrant( permission.getIdItem( ), permission.getPermissionType( ) );
        }
    }

    /**
     * Loads the permissions granted to whole populations the user belongs to, such as everyone
     * sharing their direction. The rules are matched on the directory attribute values the user
     * carries, resolved here so a snapshot serving only role checks never pays for them.
     */
    private void loadAttributePermissions( )
    {
        Map<String, String> mapAttributes = WikiUserAttributes.getValues( _user );

        if ( mapAttributes.isEmpty( ) )
        {
            return;
        }

        for ( WikiItemAttributePermission permission : WikiItemAttributePermissionHome.findByAttributeValues( mapAttributes ) )
        {
            addItemGrant( permission.getIdItem( ), permission.getPermissionType( ) );
        }
    }

    /**
     * Adds the grant an item permission row carries to the view keys, the edit keys, or none.
     *
     * @param nIdItem
     *            the item identifier
     * @param strPermissionType
     *            the permission type of the row
     */
    private void addItemGrant( int nIdItem, String strPermissionType )
    {
        addKey( KEY_PREFIX_ITEM + nIdItem, WikiItemUserPermission.PERMISSION_VIEW.equals( strPermissionType ),
                WikiItemUserPermission.PERMISSION_EDIT.equals( strPermissionType ) );
    }

    /**
     * Loads the RBAC permissions granted on wiki resources to the roles the user carries.
     * Kept for permissions an administrator assigns to a role from the back office.
     */
    private void loadRbacPermissions( )
    {
        if ( _setRoles.isEmpty( ) )
        {
            return;
        }

        Collection<String> collPermissions = Arrays.asList( WikiItemUserPermission.PERMISSION_VIEW, WikiItemUserPermission.PERMISSION_EDIT );

        for ( RBAC rbac : RBACHome.findByPermissionsAndRoles( collPermissions, _setRoles ) )
        {
            if ( !WIKI_RESOURCE_TYPES.contains( rbac.getResourceTypeKey( ) ) )
            {
                continue;
            }

            String strPermission = rbac.getPermissionKey( );
            boolean bAnyPermission = RBAC.WILDCARD_PERMISSIONS_KEY.equals( strPermission );

            addKey( toResourceKey( rbac.getResourceTypeKey( ), rbac.getResourceId( ) ),
                    bAnyPermission || WikiItemUserPermission.PERMISSION_VIEW.equals( strPermission ),
                    bAnyPermission || WikiItemUserPermission.PERMISSION_EDIT.equals( strPermission ) );
        }
    }

    /**
     * Adds a key to the view keys, the edit keys, or both.
     *
     * @param strKey
     *            the key to add
     * @param bView
     *            whether the key grants view
     * @param bEdit
     *            whether the key grants edit
     */
    private void addKey( String strKey, boolean bView, boolean bEdit )
    {
        if ( bView )
        {
            _setViewKeys.add( strKey );
        }
        if ( bEdit )
        {
            _setEditKeys.add( strKey );
        }
    }

    /**
     * Builds the key identifying an RBAC resource.
     *
     * @param strType
     *            the resource type
     * @param strId
     *            the resource identifier
     * @return the key
     */
    private static String toResourceKey( String strType, String strId )
    {
        return strType + ':' + strId;
    }

    /**
     * Returns the RBAC resource types owned by the wiki.
     *
     * @return the resource type codes
     */
    private static Set<String> buildWikiResourceTypes( )
    {
        Set<String> setTypes = new HashSet<>( );
        for ( WikiItemType type : WikiItemType.values( ) )
        {
            setTypes.add( type.getCode( ) );
        }
        return setTypes;
    }
}
