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
package fr.paris.lutece.plugins.wiki.service.rbac;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.rbac.RBACHome;
import fr.paris.lutece.portal.business.rbac.RBACRole;
import fr.paris.lutece.portal.business.rbac.RBACRoleHome;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;

public final class WikiRoleService
{
    public static final String PERMISSION_VIEW = "VIEW";
    public static final String PERMISSION_EDIT = "EDIT";

    private static final String ROLE_PREFIX_VIEW = "WIKI_ITEM_VIEW_";
    private static final String ROLE_PREFIX_EDIT = "WIKI_ITEM_EDIT_";
    public static final String ROLE_PREFIX_USER = "WIKI_USER_";

    private WikiRoleService( )
    {
    }

    /**
     * Create RBAC roles and permissions for a wiki item
     *
     * @param item
     *            the wiki item
     */
    public static void createRolesForItem( AbstractWikiItem item )
    {
        String strViewRoleKey = ROLE_PREFIX_VIEW + item.getId( );
        String strEditRoleKey = ROLE_PREFIX_EDIT + item.getId( );

        createRBACRole( strViewRoleKey, "View role for " + item.getCode( ) );
        createRBACRole( strEditRoleKey, "Edit role for " + item.getCode( ) );

        createFrontRole( strViewRoleKey, "View role for " + item.getCode( ) );
        createFrontRole( strEditRoleKey, "Edit role for " + item.getCode( ) );

        createRBACPermission( strViewRoleKey, item, PERMISSION_VIEW );
        createRBACPermission( strEditRoleKey, item, PERMISSION_EDIT );
    }

    /**
     * Remove RBAC roles and permissions for a wiki item
     *
     * @param item
     *            the wiki item
     */
    public static void removeRolesForItem( AbstractWikiItem item )
    {
        String strViewRoleKey = ROLE_PREFIX_VIEW + item.getId( );
        String strEditRoleKey = ROLE_PREFIX_EDIT + item.getId( );

        RBACHome.removeForRoleKey( strViewRoleKey );
        RBACHome.removeForRoleKey( strEditRoleKey );

        RBACHome.removeForResource( item.getResourceTypeCode( ), item.getResourceId( ) );

        if ( RBACRoleHome.checkExistRole( strViewRoleKey ) )
        {
            RBACRoleHome.remove( strViewRoleKey );
        }

        if ( RBACRoleHome.checkExistRole( strEditRoleKey ) )
        {
            RBACRoleHome.remove( strEditRoleKey );
        }

        if ( RoleHome.findExistRole( strViewRoleKey ) )
        {
            RoleHome.remove( strViewRoleKey );
        }

        if ( RoleHome.findExistRole( strEditRoleKey ) )
        {
            RoleHome.remove( strEditRoleKey );
        }
    }

    /**
     * Create an RBAC role
     *
     * @param strRoleKey
     *            the role key
     * @param strDescription
     *            the role description
     */
    private static void createRBACRole( String strRoleKey, String strDescription )
    {
        if ( !RBACRoleHome.checkExistRole( strRoleKey ) )
        {
            RBACRole role = new RBACRole( );
            role.setKey( strRoleKey );
            role.setDescription( strDescription );
            RBACRoleHome.create( role );
        }
    }

    /**
     * Create a front office role
     *
     * @param strRoleKey
     *            the role key
     * @param strDescription
     *            the role description
     */
    private static void createFrontRole( String strRoleKey, String strDescription )
    {
        if ( !RoleHome.findExistRole( strRoleKey ) )
        {
            Role role = new Role( );
            role.setRole( strRoleKey );
            role.setRoleDescription( strDescription );
            role.setWorkgroup( AdminWorkgroupService.ALL_GROUPS );
            RoleHome.create( role );
        }
    }

    /**
     * Create an RBAC permission
     *
     * @param strRoleKey
     *            the role key
     * @param item
     *            the wiki item
     * @param strPermission
     *            the permission key
     */
    private static void createRBACPermission( String strRoleKey, AbstractWikiItem item, String strPermission )
    {
        RBAC rbac = new RBAC( );
        rbac.setRoleKey( strRoleKey );
        rbac.setResourceTypeKey( item.getResourceTypeCode( ) );
        rbac.setResourceId( item.getResourceId( ) );
        rbac.setPermissionKey( strPermission );
        RBACHome.create( rbac );
    }

    /**
     * Create or get a user-specific role (WIKI_USER_{providerUserId})
     *
     * @param strProviderUserId
     *            the provider user ID
     * @return the role key
     */
    public static String ensureUserRole( String strProviderUserId )
    {
        String strRoleKey = ROLE_PREFIX_USER + strProviderUserId;

        createRBACRole( strRoleKey, "Private role for user " + strProviderUserId );
        createFrontRole( strRoleKey, "Private role for user " + strProviderUserId );

        return strRoleKey;
    }

    /**
     * Assign user role to an item for a specific permission
     *
     * @param strProviderUserId
     *            the provider user ID
     * @param item
     *            the wiki item
     * @param strPermission
     *            the permission (VIEW or EDIT)
     */
    public static void assignUserRoleToItem( String strProviderUserId, AbstractWikiItem item, String strPermission )
    {
        String strRoleKey = ensureUserRole( strProviderUserId );
        createRBACPermission( strRoleKey, item, strPermission );
    }
}
