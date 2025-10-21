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
package fr.paris.lutece.plugins.wiki.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.exception.WikiValidationException;
import fr.paris.lutece.plugins.wiki.service.rbac.WikiRoleService;
import fr.paris.lutece.portal.business.event.ResourceEvent;
import fr.paris.lutece.portal.service.event.ResourceEventManager;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.LuteceUser;

public final class WikiItemService
{
    private static final String MESSAGE_VALIDATION_CODE_DUPLICATE = "wiki.validation.wikiitem.code.duplicate";
    private static final String MESSAGE_VALIDATION_CODE_PATTERN = "wiki.validation.wikiitem.code.pattern";
    private static final Pattern CODE_PATTERN = Pattern.compile( "^[a-zA-Z0-9-]+$" );
    private static final int MAX_HIERARCHY_DEPTH = 50;

    private WikiItemService( )
    {
    }

    /**
     * Finds a wiki item by its identifier
     *
     * @param itemId
     *            the item identifier
     * @return the wiki item or null if not found
     */
    public static AbstractWikiItem findById( int itemId )
    {
        Optional<AbstractWikiItem> optItem = WikiItemHome.findByPrimaryKey( itemId );
        if ( optItem.isPresent( ) )
        {
            AbstractWikiItem item = optItem.get( );
            setCurrentRevision( item );
            return item;
        }
        return null;
    }

    /**
     * Finds a wiki item by its code
     *
     * @param strCode
     *            the item code
     * @return the wiki item or null if not found
     */
    public static AbstractWikiItem findByCode( String strCode )
    {
        Optional<AbstractWikiItem> optItem = WikiItemHome.findByCode( strCode );
        if ( optItem.isPresent( ) )
        {
            AbstractWikiItem item = optItem.get( );
            setCurrentRevision( item );
            return item;
        }
        return null;
    }

    /**
     * Gets all items by type
     *
     * @param type
     *            the wiki item type
     * @return the list of wiki items
     */
    public static List<AbstractWikiItem> getItemsByType( WikiItemType type )
    {
        List<AbstractWikiItem> items = WikiItemHome.getWikiItemsByType( type );
        items.forEach( WikiItemService::setCurrentRevision );
        return items;
    }

    /**
     * Gets all items by multiple types
     *
     * @param types
     *            the set of wiki item types
     * @return the list of wiki items matching any of the types
     */
    public static List<AbstractWikiItem> getItemsByTypes( Set<WikiItemType> types )
    {
        List<AbstractWikiItem> items = new ArrayList<>( );
        for ( WikiItemType type : types )
        {
            items.addAll( WikiItemHome.getWikiItemsByType( type ) );
        }
        items.forEach( WikiItemService::setCurrentRevision );
        return items;
    }

    /**
     * Gets all published items by type
     *
     * @param type
     *            the wiki item type
     * @return the list of published wiki items
     */
    public static List<AbstractWikiItem> getPublishedItemsByType( WikiItemType type )
    {
        return getItemsByType( type ).stream( ).filter( AbstractWikiItem::isPublished ).collect( Collectors.toList( ) );
    }

    /**
     * Gets all items by parent identifier
     *
     * @param parentId
     *            the parent identifier
     * @return the list of wiki items
     */
    public static List<AbstractWikiItem> getItemsByParent( int parentId )
    {
        List<AbstractWikiItem> items = WikiItemHome.getWikiItemsByParent( parentId );
        items.forEach( WikiItemService::setCurrentRevision );
        return items;
    }

    /**
     * Gets all published items by parent identifier
     *
     * @param parentId
     *            the parent identifier
     * @return the list of published wiki items
     */
    public static List<AbstractWikiItem> getPublishedItemsByParent( int parentId )
    {
        return getItemsByParent( parentId ).stream( ).filter( AbstractWikiItem::isPublished ).collect( Collectors.toList( ) );
    }

    /**
     * Creates a new wiki item
     *
     * @param item
     *            the wiki item to create
     * @param user
     *            the user creating the item (for private role handling)
     * @param locale
     *            the locale for error messages
     * @return the created wiki item
     * @throws WikiValidationException
     *             if validation fails
     */
    public static AbstractWikiItem create( AbstractWikiItem item, LuteceUser user, Locale locale ) throws WikiValidationException
    {
        validateCode( item, locale );
        validateParentType( item, locale );
        setNextAvailableOrder( item );

        handlePrivateRole( item, user );

        AbstractWikiItem createdItem = WikiItemHome.create( item );

        WikiRoleService.createRolesForItem( createdItem );

        if ( user != null )
        {
            assignPrivateRolePermissions( createdItem, user );
        }

        WikiItemHome.update( createdItem );

        fireResourceEvent( createdItem.getId( ), createdItem.getResourceType( ), ResourceEventManager::fireAddedResource );
        setCurrentRevision( createdItem );
        return createdItem;
    }

    /**
     * Updates a wiki item
     *
     * @param item
     *            the wiki item to update
     * @param locale
     *            the locale for error messages
     * @throws WikiValidationException
     *             if validation fails
     */
    public static void update( AbstractWikiItem item, Locale locale ) throws WikiValidationException
    {
        Optional<AbstractWikiItem> optOriginal = WikiItemHome.findByPrimaryKey( item.getId( ) );
        if ( optOriginal.isPresent( ) )
        {
            AbstractWikiItem original = optOriginal.get( );
            if ( !original.getCode( ).equals( item.getCode( ) ) )
            {
                validateCodeForUpdate( item, locale );
            }
            if ( !java.util.Objects.equals( original.getIdParent( ), item.getIdParent( ) ) )
            {
                validateParentType( item, locale );
            }
        }
        WikiItemHome.update( item );
        fireResourceEvent( item.getId( ), item.getResourceType( ), ResourceEventManager::fireUpdatedResource );
    }

    /**
     * Deletes a wiki item by its identifier
     *
     * @param itemId
     *            the item identifier
     */
    public static void delete( int itemId )
    {
        Optional<AbstractWikiItem> optItem = WikiItemHome.findByPrimaryKey( itemId );
        if ( optItem.isPresent( ) )
        {
            AbstractWikiItem item = optItem.get( );

            List<AbstractWikiItem> allDescendants = getAllDescendants( item );

            for ( AbstractWikiItem descendant : allDescendants )
            {
                WikiRoleService.removeRolesForItem( descendant );
            }

            WikiRoleService.removeRolesForItem( item );
            WikiItemHome.remove( itemId );
            fireResourceEvent( itemId, item.getResourceType( ), ResourceEventManager::fireDeletedResource );
        }
    }

    /**
     * Recursively gets all descendants of an item
     *
     * @param item
     *            the wiki item
     * @return the list of all descendants
     */
    private static List<AbstractWikiItem> getAllDescendants( AbstractWikiItem item )
    {
        return getAllDescendantsWithDepth( item, 0 );
    }

    /**
     * Recursively gets all descendants of an item with depth tracking
     *
     * @param item
     *            the wiki item
     * @param currentDepth
     *            the current recursion depth
     * @return the list of all descendants
     */
    private static List<AbstractWikiItem> getAllDescendantsWithDepth( AbstractWikiItem item, int currentDepth )
    {
        List<AbstractWikiItem> descendants = new ArrayList<>( );

        if ( currentDepth >= MAX_HIERARCHY_DEPTH )
        {
            return descendants;
        }

        List<AbstractWikiItem> children = WikiItemHome.getWikiItemsByParent( item.getId( ) );

        for ( AbstractWikiItem child : children )
        {
            descendants.add( child );
            descendants.addAll( getAllDescendantsWithDepth( child, currentDepth + 1 ) );
        }

        return descendants;
    }

    private static void setCurrentRevision( AbstractWikiItem item )
    {
        item.setCurrentRevision( RevisionService.getCurrentRevision( item.getId( ) ) );
    }

    /**
     * Validates code format (not empty and matches pattern)
     *
     * @param strCode
     *            the code to validate
     * @param locale
     *            the locale for error messages
     * @throws WikiValidationException
     *             if code format is invalid
     */
    private static void validateCodeFormat( String strCode, Locale locale ) throws WikiValidationException
    {
        if ( strCode == null || strCode.trim( ).isEmpty( ) )
        {
            throw new WikiValidationException( I18nService.getLocalizedString( "wiki.validation.wikiitem.code.notEmpty", locale ) );
        }

        if ( !CODE_PATTERN.matcher( strCode ).matches( ) )
        {
            throw new WikiValidationException( I18nService.getLocalizedString( MESSAGE_VALIDATION_CODE_PATTERN, locale ) );
        }
    }

    /**
     * Validates that the item code is unique
     *
     * @param item
     *            the wiki item
     * @param locale
     *            the locale for error messages
     * @throws WikiValidationException
     *             if code already exists
     */
    private static void validateCode( AbstractWikiItem item, Locale locale ) throws WikiValidationException
    {
        validateCodeFormat( item.getCode( ), locale );

        AbstractWikiItem existingItem = findByCode( item.getCode( ) );
        if ( existingItem != null )
        {
            Object [ ] messageArgs = {
                    item.getCode( )
            };
            throw new WikiValidationException( I18nService.getLocalizedString( MESSAGE_VALIDATION_CODE_DUPLICATE, messageArgs, locale ) );
        }
    }

    /**
     * Validates that the item code is unique during update
     *
     * @param item
     *            the wiki item
     * @param locale
     *            the locale for error messages
     * @throws WikiValidationException
     *             if code already exists for another item
     */
    private static void validateCodeForUpdate( AbstractWikiItem item, Locale locale ) throws WikiValidationException
    {
        validateCodeFormat( item.getCode( ), locale );

        AbstractWikiItem existingItem = findByCode( item.getCode( ) );
        if ( existingItem != null && existingItem.getId( ) != item.getId( ) )
        {
            Object [ ] messageArgs = {
                    item.getCode( )
            };
            throw new WikiValidationException( I18nService.getLocalizedString( MESSAGE_VALIDATION_CODE_DUPLICATE, messageArgs, locale ) );
        }
    }

    /**
     * Validates that the parent type is compatible with the item type
     *
     * @param item
     *            the wiki item
     * @param locale
     *            the locale for error messages
     * @throws WikiValidationException
     *             if parent type is invalid
     */
    private static void validateParentType( AbstractWikiItem item, Locale locale ) throws WikiValidationException
    {
        WikiItemType itemType = item.getType( );
        Integer parentId = item.getIdParent( );
        boolean requiresParent = !item.getAllowedParentTypes( ).isEmpty( );

        if ( !requiresParent )
        {
            if ( parentId != null )
            {
                throw new WikiValidationException( I18nService.getLocalizedString( "wiki.validation.space.noParent", locale ) );
            }
            return;
        }

        if ( parentId == null )
        {
            throw new WikiValidationException( I18nService.getLocalizedString( "wiki.validation." + itemType.getCode( ) + ".parentRequired", locale ) );
        }

        Optional<AbstractWikiItem> optParent = WikiItemHome.findByPrimaryKey( parentId );
        if ( !optParent.isPresent( ) )
        {
            throw new WikiValidationException( I18nService.getLocalizedString( "wiki.validation.parentNotFound", locale ) );
        }

        AbstractWikiItem parent = optParent.get( );
        if ( !parent.getAllowedChildTypes( ).contains( itemType ) )
        {
            throw new WikiValidationException( I18nService.getLocalizedString( "wiki.validation." + itemType.getCode( ) + ".invalidParent", locale ) );
        }
    }

    /**
     * Sets the next available order for the item
     *
     * @param item
     *            the wiki item
     */
    private static void setNextAvailableOrder( AbstractWikiItem item )
    {
        if ( item.hasOrder( ) )
        {
            List<AbstractWikiItem> siblings = getSiblings( item );
            int maxOrder = siblings.stream( ).mapToInt( AbstractWikiItem::getOrder ).max( ).orElse( 0 );
            item.setOrder( maxOrder + 1 );
        }
    }

    /**
     * Gets sibling items for the given item
     *
     * @param item
     *            the wiki item
     * @return the list of sibling items
     */
    private static List<AbstractWikiItem> getSiblings( AbstractWikiItem item )
    {
        if ( item.getIdParent( ) == null )
        {
            return WikiItemHome.getWikiItemsByType( item.getType( ) );
        }
        else
        {
            return WikiItemHome.getWikiItemsByParentAndType( item.getIdParent( ), item.getType( ) );
        }
    }

    /**
     * Fires a resource event
     *
     * @param resourceId
     *            the resource identifier
     * @param resourceType
     *            the resource type
     * @param eventFirer
     *            the event firer consumer
     */
    private static void fireResourceEvent( int resourceId, String resourceType, Consumer<ResourceEvent> eventFirer )
    {
        ResourceEvent event = new ResourceEvent( );
        event.setIdResource( String.valueOf( resourceId ) );
        event.setTypeResource( resourceType );
        eventFirer.accept( event );
    }

    /**
     * Moves an item to a new parent
     *
     * @param item
     *            the item to move
     * @param newParentId
     *            the new parent id
     * @param locale
     *            the locale for error messages
     * @throws WikiValidationException
     *             if validation fails
     */
    public static void move( AbstractWikiItem item, int newParentId, Locale locale ) throws WikiValidationException
    {
        Optional<AbstractWikiItem> optNewParent = WikiItemHome.findByPrimaryKey( newParentId );
        if ( !optNewParent.isPresent( ) )
        {
            throw new WikiValidationException( I18nService.getLocalizedString( "wiki.validation.parentNotFound", locale ) );
        }

        Integer originalParentId = item.getIdParent( );
        item.setIdParent( newParentId );

        try
        {
            validateParentType( item, locale );
            setNextAvailableOrder( item );
            WikiItemHome.update( item );
            fireResourceEvent( item.getId( ), item.getResourceType( ), ResourceEventManager::fireUpdatedResource );
        }
        catch( WikiValidationException e )
        {
            item.setIdParent( originalParentId );
            throw e;
        }
    }

    /**
     * Handles the "private" role by converting it to a user-specific technical role
     *
     * @param item
     *            the wiki item
     * @param user
     *            the user
     */
    private static void handlePrivateRole( AbstractWikiItem item, LuteceUser user )
    {
        if ( user != null && user.getName( ) != null )
        {
            String strProviderUserId = user.getName( );

            if ( "private".equals( item.getViewRole( ) ) )
            {
                String strUserRole = WikiRoleService.ensureUserRole( strProviderUserId );
                item.setViewRole( strUserRole );
            }

            if ( "private".equals( item.getEditRole( ) ) )
            {
                String strUserRole = WikiRoleService.ensureUserRole( strProviderUserId );
                item.setEditRole( strUserRole );
            }
        }
    }

    /**
     * Assigns permissions to the user's private role for the item
     *
     * @param item
     *            the wiki item
     * @param user
     *            the user
     */
    private static void assignPrivateRolePermissions( AbstractWikiItem item, LuteceUser user )
    {
        if ( user != null && user.getName( ) != null )
        {
            String strProviderUserId = user.getName( );
            String strUserRole = WikiRoleService.ROLE_PREFIX_USER + strProviderUserId;

            if ( item.getViewRole( ) != null && item.getViewRole( ).equals( strUserRole ) )
            {
                WikiRoleService.assignUserRoleToItem( strProviderUserId, item, WikiRoleService.PERMISSION_VIEW );
            }

            if ( item.getEditRole( ) != null && item.getEditRole( ).equals( strUserRole ) )
            {
                WikiRoleService.assignUserRoleToItem( strProviderUserId, item, WikiRoleService.PERMISSION_EDIT );
            }
        }
    }

}
