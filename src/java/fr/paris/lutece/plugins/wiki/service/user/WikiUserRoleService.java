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
package fr.paris.lutece.plugins.wiki.service.user;

import java.util.List;
import java.util.stream.Collectors;

import fr.paris.lutece.plugins.mylutece.modules.users.business.MyLuteceSearchUserHome;
import fr.paris.lutece.plugins.mylutece.modules.users.business.MyLuteceUserRole;
import fr.paris.lutece.plugins.mylutece.modules.users.business.MyLuteceUserRoleHome;
import fr.paris.lutece.plugins.mylutece.service.search.MyLuteceSearchUser;
import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;

public final class WikiUserRoleService
{
    private static final String ROLE_PREFIX_VIEW = "WIKI_ITEM_VIEW_";
    private static final String ROLE_PREFIX_EDIT = "WIKI_ITEM_EDIT_";

    private WikiUserRoleService( )
    {
    }

    /**
     * Assign a user to a wiki item with VIEW permission
     *
     * @param strProviderUserId
     *            the provider user ID (connect_id_provider)
     * @param item
     *            the wiki item
     */
    public static void assignViewPermission( String strProviderUserId, AbstractWikiItem item )
    {
        String strRoleKey = ROLE_PREFIX_VIEW + item.getId( );
        assignRoleToUser( strProviderUserId, strRoleKey );
    }

    /**
     * Assign a user to a wiki item with EDIT permission
     *
     * @param strProviderUserId
     *            the provider user ID (connect_id_provider)
     * @param item
     *            the wiki item
     */
    public static void assignEditPermission( String strProviderUserId, AbstractWikiItem item )
    {
        String strRoleKey = ROLE_PREFIX_EDIT + item.getId( );
        assignRoleToUser( strProviderUserId, strRoleKey );
    }

    /**
     * Remove VIEW permission for a user on a wiki item
     *
     * @param strProviderUserId
     *            the provider user ID (connect_id_provider)
     * @param item
     *            the wiki item
     */
    public static void removeViewPermission( String strProviderUserId, AbstractWikiItem item )
    {
        String strRoleKey = ROLE_PREFIX_VIEW + item.getId( );
        removeRoleFromUser( strProviderUserId, strRoleKey );
    }

    /**
     * Remove EDIT permission for a user on a wiki item
     *
     * @param strProviderUserId
     *            the provider user ID (connect_id_provider)
     * @param item
     *            the wiki item
     */
    public static void removeEditPermission( String strProviderUserId, AbstractWikiItem item )
    {
        String strRoleKey = ROLE_PREFIX_EDIT + item.getId( );
        removeRoleFromUser( strProviderUserId, strRoleKey );
    }

    /**
     * Get all users with VIEW permission on a wiki item
     *
     * @param item
     *            the wiki item
     * @return the list of users with VIEW permission
     */
    public static List<MyLuteceSearchUser> getUsersWithViewPermission( AbstractWikiItem item )
    {
        String strRoleKey = ROLE_PREFIX_VIEW + item.getId( );
        return getUsersByRole( strRoleKey );
    }

    /**
     * Get all users with EDIT permission on a wiki item
     *
     * @param item
     *            the wiki item
     * @return the list of users with EDIT permission
     */
    public static List<MyLuteceSearchUser> getUsersWithEditPermission( AbstractWikiItem item )
    {
        String strRoleKey = ROLE_PREFIX_EDIT + item.getId( );
        return getUsersByRole( strRoleKey );
    }

    /**
     * Assign a role to a user
     *
     * @param strProviderUserId
     *            the provider user ID (connect_id_provider)
     * @param strRoleKey
     *            the role key
     */
    private static void assignRoleToUser( String strProviderUserId, String strRoleKey )
    {
        MyLuteceSearchUser user = MyLuteceSearchUserHome.findByConnectId( strProviderUserId );
        if ( user == null )
        {
            return;
        }

        List<MyLuteceUserRole> userRoles = MyLuteceUserRoleHome.getMyLuteceUserRolesListByUserId( user.getId( ) );
        boolean hasRole = userRoles.stream( ).anyMatch( r -> r.getRoleKey( ).equals( strRoleKey ) );

        if ( !hasRole )
        {
            MyLuteceUserRole localUserRole = new MyLuteceUserRole( );
            localUserRole.setIdMyLuteceSearchUser( user.getId( ) );
            localUserRole.setRoleKey( strRoleKey );
            MyLuteceUserRoleHome.create( localUserRole );
        }
    }

    /**
     * Remove a role from a user
     *
     * @param strProviderUserId
     *            the provider user ID (connect_id_provider)
     * @param strRoleKey
     *            the role key
     */
    private static void removeRoleFromUser( String strProviderUserId, String strRoleKey )
    {
        MyLuteceSearchUser user = MyLuteceSearchUserHome.findByConnectId( strProviderUserId );
        if ( user == null )
        {
            return;
        }

        List<MyLuteceUserRole> userRoles = MyLuteceUserRoleHome.getMyLuteceUserRolesListByUserId( user.getId( ) );
        userRoles.stream( ).filter( r -> r.getRoleKey( ).equals( strRoleKey ) ).forEach( r -> MyLuteceUserRoleHome.remove( r.getId( ) ) );
    }

    /**
     * Get all users by role
     *
     * @param strRoleKey
     *            the role key
     * @return the list of users with this role
     */
    private static List<MyLuteceSearchUser> getUsersByRole( String strRoleKey )
    {
        List<MyLuteceSearchUser> allUsers = MyLuteceSearchUserHome.getMyLuteceSearchUsersList( );
        return allUsers.stream( ).filter( user -> {
            List<MyLuteceUserRole> userRoles = MyLuteceUserRoleHome.getMyLuteceUserRolesListByUserId( user.getId( ) );
            return userRoles.stream( ).anyMatch( r -> r.getRoleKey( ).equals( strRoleKey ) );
        } ).collect( Collectors.toList( ) );
    }
}
