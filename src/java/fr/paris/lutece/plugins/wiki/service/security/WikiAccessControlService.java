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
package fr.paris.lutece.plugins.wiki.service.security;

import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.service.permission.WikiPermissionService;
import fr.paris.lutece.portal.service.security.LuteceUser;

/**
 * Service for controlling access to wiki items based on user roles and permissions.
 */
public final class WikiAccessControlService
{
    private static final String ROLE_WIKI_ADMIN = "wiki_admin";
    public static final String ROLE_WIKI_CREATE_SPACE = "wiki_create_space";
    public static final String ROLE_WIKI_CREATE_BOOK = "wiki_create_book";
    public static final String ROLE_WIKI_AI_FEATURES = "wiki_ai_features";

    /**
     * Private constructor to prevent instantiation.
     */
    private WikiAccessControlService( )
    {
    }

    /**
     * Checks if the user can view the specified wiki item.
     *
     * @param user
     *            the Lutece user
     * @param item
     *            the wiki item to check
     * @return true if the user can view the item, false otherwise
     */
    public static boolean canView( LuteceUser user, AbstractWikiItem item )
    {
        return canView( WikiUserPermissions.forUser( user ), item );
    }

    /**
     * Checks if the user can view the specified wiki item, against already loaded permissions.
     *
     * Callers checking many items in a row should load the permissions once and use this method,
     * so the permissions are read from the database only once.
     *
     * @param permissions
     *            the permissions of the user
     * @param item
     *            the wiki item to check
     * @return true if the user can view the item, false otherwise
     */
    public static boolean canView( WikiUserPermissions permissions, AbstractWikiItem item )
    {
        if ( item == null )
        {
            return false;
        }

        if ( permissions.hasRole( ROLE_WIKI_ADMIN ) )
        {
            return true;
        }

        List<AbstractWikiItem> hierarchy = getHierarchy( item );

        for ( AbstractWikiItem ancestor : hierarchy )
        {
            if ( !canViewOwn( permissions, ancestor, hierarchy ) )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the user is a wiki admin.
     *
     * @param user
     *            the Lutece user
     * @return true if the user is a wiki admin, false otherwise
     */
    public static boolean isWikiAdmin( LuteceUser user )
    {
        return hasRole( user, ROLE_WIKI_ADMIN );
    }

    /**
     * Checks if the user can create a space.
     *
     * @param user
     *            the Lutece user
     * @return true if the user can create a space, false otherwise
     */
    public static boolean canCreateSpace( LuteceUser user )
    {
        return hasRole( user, ROLE_WIKI_ADMIN ) || hasRole( user, ROLE_WIKI_CREATE_SPACE );
    }

    /**
     * Checks if the user can create a book.
     *
     * @param user
     *            the Lutece user
     * @return true if the user can create a book, false otherwise
     */
    public static boolean canCreateBook( LuteceUser user )
    {
        return hasRole( user, ROLE_WIKI_ADMIN ) || hasRole( user, ROLE_WIKI_CREATE_BOOK );
    }

    /**
     * Checks if the user can use AI features.
     *
     * @param user
     *            the Lutece user
     * @return true if the user can use AI features, false otherwise
     */
    public static boolean canUseAiFeatures( LuteceUser user )
    {
        return hasRole( user, ROLE_WIKI_ADMIN ) || hasRole( user, ROLE_WIKI_AI_FEATURES );
    }

    /**
     * Checks if the user can edit the specified wiki item.
     *
     * @param user
     *            the Lutece user
     * @param item
     *            the wiki item to check
     * @return true if the user can edit the item, false otherwise
     */
    public static boolean canEdit( LuteceUser user, AbstractWikiItem item )
    {
        return canEdit( WikiUserPermissions.forUser( user ), item );
    }

    /**
     * Checks if the user can edit the specified wiki item, against already loaded permissions.
     *
     * @param permissions
     *            the permissions of the user
     * @param item
     *            the wiki item to check
     * @return true if the user can edit the item, false otherwise
     */
    public static boolean canEdit( WikiUserPermissions permissions, AbstractWikiItem item )
    {
        if ( item == null )
        {
            return false;
        }

        if ( permissions.hasRole( ROLE_WIKI_ADMIN ) )
        {
            return true;
        }

        List<AbstractWikiItem> hierarchy = getHierarchy( item );

        for ( int i = 0; i < hierarchy.size( ) - 1; i++ )
        {
            if ( !canViewOwn( permissions, hierarchy.get( i ), hierarchy ) )
            {
                return false;
            }
        }

        return hierarchy.stream( ).anyMatch( ancestor -> canEditOwn( permissions, ancestor ) );
    }

    /**
     * Checks if the user can view their own wiki item based on publication status and RBAC permissions.
     *
     * @param permissions
     *            the permissions of the user
     * @param item
     *            the wiki item to check
     * @param hierarchy
     *            the hierarchy of wiki items
     * @return true if the user can view their own item, false otherwise
     */
    private static boolean canViewOwn( WikiUserPermissions permissions, AbstractWikiItem item, List<AbstractWikiItem> hierarchy )
    {
        if ( !item.isPublished( ) )
        {
            return hierarchy.stream( ).anyMatch( ancestor -> canEditOwn( permissions, ancestor ) );
        }

        String viewRole = item.getViewRole( );
        if ( viewRole == null || viewRole.isBlank( ) || WikiPermissionService.ROLE_NONE.equalsIgnoreCase( viewRole ) )
        {
            return true;
        }

        if ( permissions.isAllowedToView( item ) || permissions.isAllowedToEdit( item ) )
        {
            return true;
        }

        return permissions.hasRole( viewRole ) || permissions.hasRole( item.getEditRole( ) );
    }

    /**
     * Checks if the user can edit their own wiki item based on RBAC permissions.
     *
     * @param permissions
     *            the permissions of the user
     * @param item
     *            the wiki item to check
     * @return true if the user can edit their own item, false otherwise
     */
    private static boolean canEditOwn( WikiUserPermissions permissions, AbstractWikiItem item )
    {
        return permissions.isAllowedToEdit( item ) || permissions.hasRole( item.getEditRole( ) );
    }

    /**
     * Checks if the user has the specified role.
     *
     * @param user
     *            the Lutece user
     * @param role
     *            the role to check
     * @return true if the user has the role, false otherwise
     */
    private static boolean hasRole( LuteceUser user, String role )
    {
        return WikiUserPermissions.forUser( user ).hasRole( role );
    }

    /**
     * Retrieves the hierarchy of wiki items from the specified item to the root.
     *
     * @param item
     *            the wiki item to start from
     * @return the list of wiki items in hierarchical order
     */
    private static List<AbstractWikiItem> getHierarchy( AbstractWikiItem item )
    {
        List<AbstractWikiItem> hierarchy = new ArrayList<>( );
        AbstractWikiItem current = item;

        while ( current != null )
        {
            hierarchy.add( 0, current );
            current = current.getIdParent( ) != null ? WikiItemHome.findByPrimaryKey( current.getIdParent( ) ).orElse( null ) : null;
        }

        return hierarchy;
    }
}
