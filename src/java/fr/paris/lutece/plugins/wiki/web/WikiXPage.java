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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.business.item.impl.Book;
import fr.paris.lutece.plugins.wiki.business.item.impl.Page;
import fr.paris.lutece.plugins.wiki.business.item.impl.Space;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.activity.ActivityItem;
import fr.paris.lutece.plugins.wiki.service.activity.ActivityService;
import fr.paris.lutece.plugins.wiki.service.activity.ActivityService.Period;
import fr.paris.lutece.plugins.wiki.service.search.WikiSearchEngine;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.search.SearchResult;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.xpages.XPage;


/**
 * WikiXPage handles all navigation views for the wiki
 */
@RequestScoped
@Named( "wiki.xpage.wiki" )
@Controller( xpageName = "wiki", pageTitleI18nKey = "wiki.xpage.wiki.pageTitle", pagePathI18nKey = "wiki.xpage.wiki.pagePathLabel" )
public class WikiXPage extends AbstractWikiXPage
{
    private static final long serialVersionUID = 1L;

    @Inject
    private Models _models;

    @Inject
    private WikiSearchEngine _searchEngine;

    protected static final String MARK_PAGE = "page";

    protected static final String XPAGE_NAME = "wiki";
    protected static final String MESSAGE_PAGE_TITLE = "wiki.xpage.wiki.pageTitle";
    protected static final String MESSAGE_PATH = "wiki.xpage.wiki.pagePathLabel";

    protected static final String VIEW_LIST_WIKI = "listWiki";
    protected static final String VIEW_SPACE = "viewSpace";
    protected static final String VIEW_BOOK = "viewBook";
    protected static final String VIEW_PAGE = "viewPage";
    protected static final String VIEW_SEARCH = "search";
    protected static final String VIEW_SPACE_ACTIVITY = "spaceActivity";
    protected static final String VIEW_BOOK_ACTIVITY = "bookActivity";

    protected static final String MESSAGE_NO_VIEW_RIGHTS = "wiki.message.noViewRights";

    protected static final String TEMPLATE_LIST_SPACES = "/skin/plugins/wiki/list_spaces.html";
    protected static final String TEMPLATE_VIEW_SPACE = "/skin/plugins/wiki/view_space.html";
    protected static final String TEMPLATE_VIEW_BOOK = "/skin/plugins/wiki/view_book.html";
    protected static final String TEMPLATE_VIEW_PAGE = "/skin/plugins/wiki/view_page.html";
    protected static final String TEMPLATE_SEARCH_RESULTS = "/skin/plugins/wiki/search_results.html";
    protected static final String TEMPLATE_ACTIVITY = "/skin/plugins/wiki/activity.html";

    protected static final String MARK_CATEGORIES_LIST = "categories_list";
    protected static final String MARK_CATEGORY = "category";
    protected static final String MARK_BOOKS_LIST = "books_list";
    protected static final String MARK_CHAPTER_VIEW_RIGHTS = "chapter_view_rights";
    protected static final String MARK_PAGES_LIST = "pages_list";
    protected static final String MARK_PARENT = "parent";
    protected static final String MARK_SEARCH_RESULTS = "results";
    protected static final String MARK_QUERY = "query";
    protected static final String MARK_BOOK_CODE = "book_code";
    protected static final String MARK_SPACE_CODE = "space_code";
    protected static final String MARK_CAN_CREATE_SPACE = "can_create_space";
    protected static final String MARK_IS_WIKI_ADMIN = "is_wiki_admin";
    protected static final String MARK_ALL_ITEMS = "all_items";
    protected static final String MARK_ACTIVITIES = "activities";
    protected static final String MARK_CURRENT_PERIOD = "current_period";
    protected static final String MARK_ACTIVITY_CONTEXT = "activity_context";

    protected static final String PARAMETER_PERIOD = "period";

    protected static final String PARAMETER_SPACE_CODE = "space";
    protected static final String PARAMETER_CATEGORY_CODE = "category";
    protected static final String PARAMETER_BOOK_CODE = "book";
    protected static final String PARAMETER_CHAPTER_CODE = "chapter";
    protected static final String PARAMETER_PAGE_CODE = "wiki_page";

    /**
     * Display the list of all libraries
     *
     * @param request
     *            The HTTP request
     * @return The XPage containing the list of libraries
     */
    @View( value = VIEW_LIST_WIKI, defaultView = true )
    public XPage listWiki( HttpServletRequest request )
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        List<AbstractWikiItem> allSpaces = WikiItemService.getItemsByType( WikiItemType.SPACE );
        List<AbstractWikiItem> spaces = allSpaces.stream( ).filter( space -> WikiAccessControlService.canView( user, space ) ).collect( Collectors.toList( ) );

        _models.put( MARK_ALL_ITEMS, spaces );
        _models.put( MARK_CAN_CREATE_SPACE, WikiAccessControlService.canCreateSpace( user ) );
        _models.put( MARK_IS_WIKI_ADMIN, WikiAccessControlService.isWikiAdmin( user ) );
        populateCommonModel( _models, user );

        XPage page = getXPage( TEMPLATE_LIST_SPACES, locale );
        page.setTitle( I18nService.getLocalizedString( "wiki.xpage.listWiki.pageTitle", locale ) );
        return page;
    }

    /**
     * Display a specific space with its categories
     *
     * @param request
     *            The HTTP request
     * @return The XPage containing the space view
     */
    @View( VIEW_SPACE )
    public XPage viewSpace( HttpServletRequest request )
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        String strSpaceCode = request.getParameter( PARAMETER_SPACE_CODE );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        Space space = (Space) WikiItemService.findByCode( strSpaceCode );

        if ( space == null || !WikiAccessControlService.canView( user, space ) )
        {
            addError( MESSAGE_NO_VIEW_RIGHTS, locale );
            return redirectView( request, VIEW_LIST_WIKI );
        }

        populateSpaceSidebarModel( _models, user, space );

        XPage page = getXPage( TEMPLATE_VIEW_SPACE, locale );
        page.setTitle( I18nService.getLocalizedString( "wiki.xpage.viewSpace.pageTitle", locale ) );
        return page;
    }

    /**
     * Display a specific book with its chapters
     *
     * @param request
     *            The HTTP request
     * @return The XPage containing the book view
     */
    @View( VIEW_BOOK )
    public XPage viewBook( HttpServletRequest request )
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        String strBookCode = request.getParameter( PARAMETER_BOOK_CODE );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        Book book = (Book) WikiItemService.findByCode( strBookCode );

        if ( book == null || !WikiAccessControlService.canView( user, book ) )
        {
            addError( MESSAGE_NO_VIEW_RIGHTS, locale );
            return redirectView( request, VIEW_LIST_WIKI );
        }

        List<AbstractWikiItem> bookChildren = loadItemChildren( user, book );
        Map<String, Boolean> childEditRights = computeChildEditRights( user, bookChildren );

        _models.put( MARK_BOOK, book );
        _models.put( MARK_BOOK_CHILDREN, bookChildren );
        _models.put( MARK_CHAPTER_EDIT_RIGHTS, childEditRights );
        _models.put( MARK_CAN_EDIT, WikiAccessControlService.canEdit( user, book ) );
        _models.put( MARK_SPACE, findSpaceForBook( book ) );
        populateCommonModel( _models, user );

        XPage page = getXPage( TEMPLATE_VIEW_BOOK, locale );
        page.setTitle( getItemTitle( book ) );
        return page;
    }

    /**
     * Display a wiki page (unified view for all parent types)
     *
     * @param request
     *            The HTTP request
     * @return The XPage containing the page view
     */
    @View( VIEW_PAGE )
    public XPage viewPage( HttpServletRequest request )
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        String strPageCode = request.getParameter( PARAMETER_PAGE_CODE );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        Page page = (Page) WikiItemService.findByCode( strPageCode );
        if ( page == null || !WikiAccessControlService.canView( user, page ) )
        {
            addError( MESSAGE_NO_VIEW_RIGHTS, locale );
            return redirectView( request, VIEW_LIST_WIKI );
        }

        _models.put( MARK_PAGE, page );
        _models.put( MARK_CAN_EDIT, WikiAccessControlService.canEdit( user, page ) );

        AbstractWikiItem parent = page.getParent( );
        populatePageModel( _models, user, parent );
        populateCommonModel( _models, user );

        XPage xpage = getXPage( TEMPLATE_VIEW_PAGE, locale );
        xpage.setTitle( getItemTitle( page ) );
        return xpage;
    }

    /**
     * Populates the model for page view based on parent type
     *
     * @param models
     *            the models
     * @param user
     *            the user
     * @param parent
     *            the parent item
     */
    private void populatePageModel( Models models, LuteceUser user, AbstractWikiItem parent )
    {
        if ( parent == null )
        {
            return;
        }

        populateNavigationContext( models, parent );

        AbstractWikiItem book = (AbstractWikiItem) models.get( MARK_BOOK );
        AbstractWikiItem space = (AbstractWikiItem) models.get( MARK_SPACE );

        if ( book != null )
        {
            populateBookContext( models, user, (Book) book );
        }
        else if ( space != null )
        {
            populateSpaceSidebarModel( models, user, (Space) space );
        }
    }

    /**
     * Populates book context in model
     *
     * @param models
     *            the models
     * @param user
     *            the user
     * @param book
     *            the book
     */
    private void populateBookContext( Models models, LuteceUser user, Book book )
    {
        List<AbstractWikiItem> bookChildren = loadItemChildren( user, book );
        Map<String, Boolean> childEditRights = computeChildEditRights( user, bookChildren );

        models.put( MARK_BOOK, book );
        models.put( MARK_BOOK_CHILDREN, bookChildren );
        models.put( MARK_CHAPTER_EDIT_RIGHTS, childEditRights );
        models.put( MARK_SPACE, findSpaceForBook( book ) );
    }

    /**
     * Search in wiki content
     *
     * @param request
     *            The HTTP request
     * @return The XPage containing search results
     */
    @View( value = VIEW_SEARCH )
    public XPage search( HttpServletRequest request )
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        String strQuery = request.getParameter( MARK_QUERY );
        String strBookCode = request.getParameter( PARAMETER_BOOK_CODE );
        String strSpaceCode = request.getParameter( PARAMETER_SPACE_CODE );

        if ( strQuery != null && !strQuery.trim( ).isEmpty( ) )
        {
            List<SearchResult> listResults = _searchEngine.getSearchResults( strQuery, request );

            _models.put( MARK_SEARCH_RESULTS, listResults );

            if ( strBookCode != null && !strBookCode.isEmpty( ) )
            {
                _models.put( MARK_BOOK_CODE, strBookCode );
            }
            if ( strSpaceCode != null && !strSpaceCode.isEmpty( ) )
            {
                _models.put( MARK_SPACE_CODE, strSpaceCode );
            }
        }
        else
        {
            _models.put( MARK_SEARCH_RESULTS, null );
        }

        _models.put( MARK_QUERY, strQuery );
        populateCommonModel( _models, user );

        XPage page = getXPage( TEMPLATE_SEARCH_RESULTS, locale );
        page.setTitle( I18nService.getLocalizedString( "wiki.search.results.pageTitle", locale ) );
        return page;
    }

    /**
     * Display activity timeline for a space
     *
     * @param request
     *            The HTTP request
     * @return The XPage containing the activity timeline
     */
    @View( VIEW_SPACE_ACTIVITY )
    public XPage viewSpaceActivity( HttpServletRequest request )
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        String strSpaceCode = request.getParameter( PARAMETER_SPACE_CODE );
        String strPeriod = request.getParameter( PARAMETER_PERIOD );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        Space space = (Space) WikiItemService.findByCode( strSpaceCode );

        if ( space == null || !WikiAccessControlService.canView( user, space ) )
        {
            addError( MESSAGE_NO_VIEW_RIGHTS, locale );
            return redirectView( request, VIEW_LIST_WIKI );
        }

        Period period = Period.fromCode( strPeriod );
        List<ActivityItem> activities = ActivityService.getSpaceActivities( space.getId( ), period, user );

        populateSpaceSidebarModel( _models, user, space );
        _models.put( MARK_ACTIVITIES, activities );
        _models.put( MARK_CURRENT_PERIOD, period.getCode( ) );
        _models.put( MARK_ACTIVITY_CONTEXT, "space" );

        XPage page = getXPage( TEMPLATE_ACTIVITY, locale );
        page.setTitle( I18nService.getLocalizedString( "wiki.activity.pageTitle", locale ) );
        return page;
    }

    /**
     * Display activity timeline for a book
     *
     * @param request
     *            The HTTP request
     * @return The XPage containing the activity timeline
     */
    @View( VIEW_BOOK_ACTIVITY )
    public XPage viewBookActivity( HttpServletRequest request )
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        String strBookCode = request.getParameter( PARAMETER_BOOK_CODE );
        String strPeriod = request.getParameter( PARAMETER_PERIOD );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        Book book = (Book) WikiItemService.findByCode( strBookCode );

        if ( book == null || !WikiAccessControlService.canView( user, book ) )
        {
            addError( MESSAGE_NO_VIEW_RIGHTS, locale );
            return redirectView( request, VIEW_LIST_WIKI );
        }

        Period period = Period.fromCode( strPeriod );
        List<ActivityItem> activities = ActivityService.getBookActivities( book.getId( ), period, user );

        populateBookSidebarModel( _models, user, book );
        _models.put( MARK_ACTIVITIES, activities );
        _models.put( MARK_CURRENT_PERIOD, period.getCode( ) );
        _models.put( MARK_ACTIVITY_CONTEXT, "book" );

        XPage page = getXPage( TEMPLATE_ACTIVITY, locale );
        page.setTitle( I18nService.getLocalizedString( "wiki.activity.pageTitle", locale ) );
        return page;
    }

}
