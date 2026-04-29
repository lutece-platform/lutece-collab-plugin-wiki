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
package fr.paris.lutece.plugins.wiki.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.business.item.impl.Book;
import fr.paris.lutece.plugins.wiki.business.item.impl.Space;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.RevisionService;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;

/**
 * Abstract base class for Wiki XPages Provides flash message handling for messages that survive HTTP redirects
 */
public abstract class AbstractWikiXPage extends MVCApplication
{
    private static final long serialVersionUID = 1L;

    private static final String SESSION_FLASH_INFOS = "wiki_flash_infos";
    private static final String SESSION_FLASH_ERRORS = "wiki_flash_errors";
    private static final String SESSION_FLASH_WARNINGS = "wiki_flash_warnings";
    protected static final String MARK_BOOK = "book";
    protected static final String MARK_BOOK_CHILDREN = "book_children";
    protected static final String MARK_CHAPTER = "chapter";
    protected static final String MARK_CHAPTER_EDIT_RIGHTS = "chapter_edit_rights";
    protected static final String MARK_CAN_EDIT = "can_edit";
    protected static final String MARK_SPACE = "space";
    protected static final String MARK_SPACE_CHILDREN = "space_children";
    protected static final String MARK_SPACE_CHILDREN_EDIT_RIGHTS = "space_children_edit_rights";
    protected static final String MARK_USER = "user";
    protected static final String MARK_IS_MODULE_AI_PRESENT = "is_module_ai_present";
    protected static final String MARK_IS_MODULE_QUIZ_PRESENT = "is_module_quiz_present";
    protected static final String PLUGIN_WIKI_AI = "wiki-ai";
    protected static final String PLUGIN_WIKI_QUIZ = "wiki-quiz";

    /**
     * Adds an info flash message that will survive HTTP redirects
     *
     * @param request
     *            The HTTP request
     * @param strMessageKey
     *            The i18n message key
     */
    protected void addFlashInfo( HttpServletRequest request, String strMessageKey )
    {
        String strMessage = I18nService.getLocalizedString( strMessageKey, getLocale( request ) );
        saveFlashMessage( request, SESSION_FLASH_INFOS, strMessage );
    }

    /**
     * Adds an error flash message that will survive HTTP redirects
     *
     * @param request
     *            The HTTP request
     * @param strMessageKey
     *            The i18n message key
     */
    protected void addFlashError( HttpServletRequest request, String strMessageKey )
    {
        String strMessage = I18nService.getLocalizedString( strMessageKey, getLocale( request ) );
        saveFlashMessage( request, SESSION_FLASH_ERRORS, strMessage );
    }

    /**
     * Adds a warning flash message that will survive HTTP redirects
     *
     * @param request
     *            The HTTP request
     * @param strMessageKey
     *            The i18n message key
     */
    protected void addFlashWarning( HttpServletRequest request, String strMessageKey )
    {
        String strMessage = I18nService.getLocalizedString( strMessageKey, getLocale( request ) );
        saveFlashMessage( request, SESSION_FLASH_WARNINGS, strMessage );
    }

    /**
     * Retrieves and applies flash messages from session This method should be called at the beginning of each view
     *
     * @param request
     *            The HTTP request
     */
    protected void applyFlashMessages( HttpServletRequest request )
    {
        List<String> infos = getAndClearFlashMessages( request, SESSION_FLASH_INFOS );
        for ( String info : infos )
        {
            addInfo( info );
        }

        List<String> errors = getAndClearFlashMessages( request, SESSION_FLASH_ERRORS );
        for ( String error : errors )
        {
            addError( error );
        }

        List<String> warnings = getAndClearFlashMessages( request, SESSION_FLASH_WARNINGS );
        for ( String warning : warnings )
        {
            addWarning( warning );
        }
    }

    /**
     * Saves a flash message in session
     *
     * @param request
     *            The HTTP request
     * @param sessionKey
     *            The session attribute key
     * @param strMessage
     *            The message to save
     */
    @SuppressWarnings( "unchecked" )
    private void saveFlashMessage( HttpServletRequest request, String sessionKey, String strMessage )
    {
        HttpSession session = request.getSession( true );
        List<String> messages = (List<String>) session.getAttribute( sessionKey );
        if ( messages == null )
        {
            messages = new ArrayList<>( );
        }
        messages.add( strMessage );
        session.setAttribute( sessionKey, messages );
    }

    /**
     * Retrieves and clears flash messages from session
     *
     * @param request
     *            The HTTP request
     * @param sessionKey
     *            The session attribute key
     * @return The list of flash messages (empty list if none)
     */
    @SuppressWarnings( "unchecked" )
    private List<String> getAndClearFlashMessages( HttpServletRequest request, String sessionKey )
    {
        HttpSession session = request.getSession( true );
        List<String> messages = (List<String>) session.getAttribute( sessionKey );
        if ( messages == null )
        {
            messages = new ArrayList<>( );
        }
        session.removeAttribute( sessionKey );
        return messages;
    }

    /**
     * Loads the children of an item for a given user, using bulk-loaded descendant tree
     *
     * @param user
     *            The Lutece user
     * @param parent
     *            The parent item
     * @return The list of visible children with their own children loaded
     */
    protected List<AbstractWikiItem> loadItemChildren( LuteceUser user, AbstractWikiItem parent )
    {
        Map<Integer, List<AbstractWikiItem>> descendantsByParent = WikiItemHome.getDescendantsByParent( parent.getId( ) );
        return buildVisibleTree( user, parent.getId( ), descendantsByParent );
    }

    /**
     * Builds the visible tree from a pre-loaded descendants map
     *
     * @param user
     *            The Lutece user
     * @param nParentId
     *            The parent id
     * @param descendantsByParent
     *            The map of parent id to children
     * @return The list of visible children with their own children loaded
     */
    private List<AbstractWikiItem> buildVisibleTree( LuteceUser user, int nParentId, Map<Integer, List<AbstractWikiItem>> descendantsByParent )
    {
        List<AbstractWikiItem> children = descendantsByParent.getOrDefault( nParentId, Collections.emptyList( ) );
        List<AbstractWikiItem> result = new ArrayList<>( );

        for ( AbstractWikiItem child : children )
        {
            child.setCurrentRevision( RevisionService.getCurrentRevision( child.getId( ) ) );

            if ( !child.isPublished( ) || !WikiAccessControlService.canView( user, child ) )
            {
                continue;
            }

            if ( !child.getAllowedChildTypes( ).isEmpty( ) && !child.supportsContent( ) )
            {
                List<AbstractWikiItem> visibleChildren = buildVisibleTree( user, child.getId( ), descendantsByParent );
                child.setChildren( visibleChildren );
            }
            result.add( child );
        }

        return result;
    }

    /**
     * Computes the edit rights for a list of children (chapters and pages) for a given user
     *
     * @param user
     *            The Lutece user
     * @param children
     *            The list of children
     * @return A map of item IDs to edit rights (true if the user can edit the item)
     */
    protected Map<String, Boolean> computeChildEditRights( LuteceUser user, List<AbstractWikiItem> children )
    {
        Map<String, Boolean> rights = new java.util.HashMap<>( );
        for ( AbstractWikiItem child : children )
        {
            rights.put( String.valueOf( child.getId( ) ), WikiAccessControlService.canEdit( user, child ) );
            if ( child.getChildren( ) != null && !child.getChildren( ).isEmpty( ) )
            {
                rights.putAll( computeChildEditRights( user, child.getChildren( ) ) );
            }
        }
        return rights;
    }

    /**
     * Populates the common model attributes for wiki XPages
     *
     * @param model
     *            The model map
     * @param user
     *            The Lutece user
     */
    protected void populateCommonModel( Map<String, Object> model, LuteceUser user )
    {
        model.put( MARK_USER, user );
        model.put( MARK_IS_MODULE_AI_PRESENT, PluginService.isPluginEnable( PLUGIN_WIKI_AI ) );
        model.put( MARK_IS_MODULE_QUIZ_PRESENT, PluginService.isPluginEnable( PLUGIN_WIKI_QUIZ ) );
    }

    /**
     * Finds the space associated with a book
     *
     * @param book
     *            The book
     * @return The space if found, null otherwise
     */
    protected AbstractWikiItem findSpaceForBook( Book book )
    {
        AbstractWikiItem current = book.getIdParent( ) != null ? WikiItemService.findById( book.getIdParent( ) ) : null;
        while ( current != null )
        {
            if ( current.getType( ) == WikiItemType.SPACE )
            {
                return current;
            }
            current = current.getIdParent( ) != null ? WikiItemService.findById( current.getIdParent( ) ) : null;
        }
        return null;
    }

    /**
     * Populates the model with data required for the book sidebar
     *
     * @param model
     *            The model map
     * @param user
     *            The Lutece user
     * @param book
     *            The book
     */
    protected void populateBookSidebarModel( Map<String, Object> model, LuteceUser user, Book book )
    {
        List<AbstractWikiItem> bookChildren = loadItemChildren( user, book );
        Map<String, Boolean> childEditRights = computeChildEditRights( user, bookChildren );

        model.put( MARK_BOOK, book );
        model.put( MARK_BOOK_CHILDREN, bookChildren );
        model.put( MARK_CHAPTER_EDIT_RIGHTS, childEditRights );
        model.put( MARK_CAN_EDIT, WikiAccessControlService.canEdit( user, book ) );
        model.put( MARK_SPACE, findSpaceForBook( book ) );
        populateCommonModel( model, user );
    }

    /**
     * Populates the model with data required for the space sidebar
     *
     * @param model
     *            The model map
     * @param user
     *            The Lutece user
     * @param space
     *            The space
     */
    protected void populateSpaceSidebarModel( Map<String, Object> model, LuteceUser user, Space space )
    {
        List<AbstractWikiItem> spaceChildren = loadItemChildren( user, space );
        Map<String, Boolean> childEditRights = computeChildEditRights( user, spaceChildren );

        model.put( MARK_SPACE, space );
        model.put( MARK_SPACE_CHILDREN, spaceChildren );
        model.put( MARK_SPACE_CHILDREN_EDIT_RIGHTS, childEditRights );
        model.put( MARK_CAN_EDIT, WikiAccessControlService.canEdit( user, space ) );
        populateCommonModel( model, user );
    }

    /**
     * Gets the title of a wiki item
     *
     * @param item
     *            The wiki item
     * @return The title of the item, or the code if no title is available
     */
    protected String getItemTitle( AbstractWikiItem item )
    {
        return item.getCurrentRevision( ) != null ? item.getCurrentRevision( ).getTitle( ) : item.getCode( );
    }

    /**
     * Populates the model with navigation context by traversing up the hierarchy. Adds the appropriate BOOK, CHAPTER, or SPACE to the model.
     *
     * @param model
     *            the model map
     * @param item
     *            the starting item (can be the item itself or its parent)
     */
    protected void populateNavigationContext( Map<String, Object> model, AbstractWikiItem item )
    {
        if ( item == null )
        {
            return;
        }

        boolean chapterSet = false;
        AbstractWikiItem current = item;
        while ( current != null )
        {
            switch( current.getType( ) )
            {
                case CHAPTER:
                    if ( !chapterSet )
                    {
                        model.put( MARK_CHAPTER, current );
                        chapterSet = true;
                    }
                    break;
                case BOOK:
                    model.put( MARK_BOOK, current );
                    return;
                case SPACE:
                    model.put( MARK_SPACE, current );
                    return;
                case CATEGORY:
                    break;
                default:
                    break;
            }
            current = current.getParent( );
        }
    }
}
