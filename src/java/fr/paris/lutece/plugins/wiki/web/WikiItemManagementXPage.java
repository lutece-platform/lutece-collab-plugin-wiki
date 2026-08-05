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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.modules.users.business.AttributeMapping;
import fr.paris.lutece.plugins.mylutece.modules.users.business.AttributeMappingHome;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemFactory;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemUserPermission;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.exception.WikiValidationException;
import fr.paris.lutece.plugins.wiki.service.RevisionService;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.permission.WikiPermissionService;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.plugins.wiki.service.user.ExternalUserSearchService;
import fr.paris.lutece.plugins.wiki.service.user.WikiUserDisplayName;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.bean.BeanUtil;
import fr.paris.lutece.util.url.UrlItem;

@Controller( xpageName = "wikiitemmanagement", pageTitleI18nKey = "wiki.xpage.itemManagement.pageTitle", pagePathI18nKey = "wiki.xpage.itemManagement.pagePathLabel" )
public class WikiItemManagementXPage extends AbstractWikiXPage
{

    private static final long serialVersionUID = 1L;

    private static final String XPAGE_NAME = "wikiitemmanagement";
    private static final String PARAMETER_ID_REVISION = "id_revision";
    private static final String ACCESS_DENIED_MESSAGE = "Access denied";
    private static final String INVALID_SECURITY_TOKEN_MESSAGE = "Invalid security token";
    private static final String REVISION_NOT_FOUND_MESSAGE = "Revision not found";

    private static final ExternalUserSearchService _externalUserSearchService = ExternalUserSearchService.getInstance( );

    private static final String PARAMETER_ID = "id";
    private static final String PARAMETER_CODE = "code";
    private static final String PARAMETER_PARENT_CODE = "parent_code";
    private static final String PARAMETER_ENTITY_TYPE = "entity_type";
    private static final String PARAMETER_PAGE = "page";
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_ENCODED_SUFFIX = "_encoded";
    private static final String PARAMETER_CHILD_ID = "child_id";
    private static final String PARAMETER_USER_GUID = "user_guid";
    private static final String PARAMETER_PERMISSION_TYPE = "permission_type";
    private static final String MARK_REVISION = "revision";
    private static final String MARK_REVISION_HISTORY = "revision_history";
    private static final String MARK_ITEM_TYPE = "item_type";
    private static final String MARK_ITEM = "item";
    private static final String MARK_PARENT = "parent";
    private static final String MARK_CHILDREN = "children";
    private static final String MARK_CHILD_TYPE = "child_type";
    private static final String MARK_CHILD_TYPES = "child_types";
    private static final String MARK_VIEW_USERS = "view_users";
    private static final String MARK_EDIT_USERS = "edit_users";
    private static final String MARK_USER_SEARCH_AVAILABLE = "user_search_available";
    private static final String MARK_USER_ROLES = "user_roles";
    private static final String MARK_PERMISSION_SEARCH_FIELDS = "permission_search_fields";
    private static final String MARK_VIEW_POPULATIONS = "view_populations";
    private static final String MARK_EDIT_POPULATIONS = "edit_populations";

    private static final String NAV_VIEW_SPACE = "viewSpace";
    private static final String NAV_VIEW_BOOK = "viewBook";
    private static final String NAV_VIEW_PAGE = "viewPage";
    private static final String NAV_XPAGE = "wiki";
    private static final String NAV_PARAM_VIEW = "view";
    private static final String PORTAL_JSP_URL = "Portal.jsp";

    private static final String VIEW_CREATE_ITEM = "createItem";
    private static final String VIEW_MODIFY_ITEM = "modifyItem";
    private static final String VIEW_VIEW_REVISION = "viewRevision";
    private static final String VIEW_MANAGE_ITEM_CHILDREN = "manageItemChildren";
    private static final String VIEW_MANAGE_SPACES = "manageSpaces";
    private static final String VIEW_MOVE_ITEM = "moveItem";

    /**
     * Token action of the item modification form, shared with the permission REST endpoints that
     * carry the same token.
     */
    public static final String ACTION_UPDATE_ITEM = "updateItem";

    private static final String ACTION_CREATE_ITEM = "createItem";
    private static final String ACTION_CONFIRM_REMOVE_ITEM = "confirmRemoveItem";
    private static final String ACTION_REMOVE_ITEM = "removeItem";
    private static final String ACTION_RESTORE_REVISION = "restoreRevision";
    private static final String ACTION_REORDER_ITEM_CHILDREN = "doReorderItemChildren";
    private static final String ACTION_REORDER_SPACES = "doReorderSpaces";
    private static final String ACTION_MOVE_ITEM = "doMoveItem";
    private static final String ACTION_REMOVE_USER_PERMISSION = "removeUserPermission";

    private static final String TEMPLATE_CREATE_ITEM = "/skin/plugins/wiki/create_item.html";
    private static final String TEMPLATE_MODIFY_ITEM = "/skin/plugins/wiki/modify_item.html";
    private static final String TEMPLATE_VIEW_REVISION = "/skin/plugins/wiki/view_revision.html";
    private static final String TEMPLATE_MANAGE_ITEM_CHILDREN = "/skin/plugins/wiki/manage_item_children.html";
    private static final String TEMPLATE_MOVE_ITEM = "/skin/plugins/wiki/move_item.html";
    private static final String MESSAGE_NEW_CATEGORY_TITLE = "wiki.xpage.newCategory.pageTitle";
    private static final String MESSAGE_NEW_BOOK_TITLE = "wiki.xpage.newBook.pageTitle";
    private static final String MESSAGE_NEW_CHAPTER_TITLE = "wiki.xpage.newChapter.pageTitle";
    private static final String MESSAGE_NEW_PAGE_TITLE = "wiki.xpage.newPage.pageTitle";
    private static final String MESSAGE_MODIFY_ITEM_TITLE = "wiki.xpage.modifyItem.pageTitle";
    private static final String MESSAGE_VIEW_REVISION_TITLE = "wiki.revision.view.pageTitle";
    private static final String MESSAGE_CONFIRM_REMOVE_ITEM = "wiki.message.confirmRemoveItem";
    private static final String MESSAGE_CONFIRM_REMOVE_SPACE = "wiki.message.confirmRemoveSpace";
    private static final String MESSAGE_CONFIRM_REMOVE_CATEGORY = "wiki.message.confirmRemoveCategory";
    private static final String MESSAGE_CONFIRM_REMOVE_BOOK = "wiki.message.confirmRemoveBook";
    private static final String MESSAGE_CONFIRM_REMOVE_CHAPTER = "wiki.message.confirmRemoveChapter";
    private static final String MESSAGE_CONFIRM_REMOVE_PAGE = "wiki.message.confirmRemovePage";
    private static final String MESSAGE_ITEM_UPDATED = "wiki.message.itemUpdated";
    private static final String MESSAGE_ITEM_REMOVED = "wiki.message.itemRemoved";
    private static final String MESSAGE_REVISION_RESTORED = "wiki.message.revisionRestored";
    private static final String MESSAGE_MANAGE_ITEM_CHILDREN_TITLE = "wiki.xpage.manageChildren.pageTitle";
    private static final String MESSAGE_CHILDREN_REORDERED = "wiki.message.childrenReordered";
    private static final String MESSAGE_MOVE_ITEM_TITLE = "wiki.xpage.moveItem.pageTitle";
    private static final String MESSAGE_ITEM_MOVED = "wiki.message.itemMoved";

    private static final String PARAMETER_TARGET_PARENT_ID = "target_parent_id";

    private static final String NAV_VIEW_LIST_WIKI = "listWiki";

    /**
     * Displays the item creation form
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @View( value = VIEW_CREATE_ITEM, defaultView = true )
    public XPage createItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        String strItemType = request.getParameter( PARAMETER_ENTITY_TYPE );
        WikiItemType itemType = WikiItemType.valueOf( strItemType.toUpperCase( ) );

        String strParentCode = request.getParameter( PARAMETER_PARENT_CODE );
        AbstractWikiItem parent = null;
        if ( strParentCode != null && !strParentCode.isEmpty( ) )
        {
            parent = WikiItemService.findByCode( strParentCode );
        }

        LuteceUser user = checkCreateAccess( request, itemType, parent );

        Map<String, Object> model = getModel( );
        model.put( MARK_USER, user );
        model.put( MARK_ITEM_TYPE, itemType );
        model.put( MARK_PARENT, parent );
        model.put( MARK_USER_ROLES, getUserRoles( user, getLocale( request ) ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_ITEM ) );

        if ( parent != null )
        {
            populateNavigationContext( model, parent );
        }

        String titleKey = getNewItemTitleKey( itemType );
        XPage page = getXPage( TEMPLATE_CREATE_ITEM, getLocale( request ), model );
        page.setTitle( I18nService.getLocalizedString( titleKey, getLocale( request ) ) );
        return page;
    }

    /**
     * Processes the item creation
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @Action( ACTION_CREATE_ITEM )
    public XPage doCreateItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        validateToken( request, ACTION_CREATE_ITEM );

        String strItemType = request.getParameter( PARAMETER_ENTITY_TYPE );
        WikiItemType itemType = WikiItemType.valueOf( strItemType.toUpperCase( ) );

        String strParentId = request.getParameter( "id_parent" );
        AbstractWikiItem parent = null;
        if ( strParentId != null && !strParentId.isEmpty( ) )
        {
            parent = WikiItemService.findById( Integer.parseInt( strParentId ) );
        }
        LuteceUser user = checkCreateAccess( request, itemType, parent );

        AbstractWikiItem item = WikiItemFactory.create( itemType );
        populate( item, request );

        try
        {
            item = WikiItemService.create( item, user, getLocale( request ) );

            String strTitleEncoded = request.getParameter( "title_encoded" );
            if ( strTitleEncoded != null && !strTitleEncoded.trim( ).isEmpty( ) )
            {
                String strAuthor = WikiUserDisplayName.of( user );

                Revision revision = new Revision( );
                revision.setEntityId( item.getId( ) );
                revision.setAuthor( strAuthor );
                populateWithDecode( revision, request );
                revision.setIsCurrent( true );

                RevisionService.create( revision );
            }

            Map<String, String> modifyParams = new HashMap<>( );
            modifyParams.put( PARAMETER_CODE, item.getCode( ) );
            return redirect( request, VIEW_MODIFY_ITEM, modifyParams );
        }
        catch( WikiValidationException e )
        {
            addError( e.getMessage( ) );
            Map<String, String> params = new HashMap<>( );
            params.put( PARAMETER_ENTITY_TYPE, strItemType );
            if ( parent != null )
            {
                params.put( PARAMETER_PARENT_CODE, parent.getCode( ) );
            }
            return redirect( request, VIEW_CREATE_ITEM, params );
        }
    }

    /**
     * Displays the item modification form
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @View( VIEW_MODIFY_ITEM )
    public XPage modifyItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        String strCode = request.getParameter( PARAMETER_CODE );
        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        LuteceUser user = checkEditAccess( request, item );

        List<WikiItemUserPermission> viewUsers = WikiPermissionService.getUsersWithPermission( item, WikiItemUserPermission.PERMISSION_VIEW );
        List<WikiItemUserPermission> editUsers = WikiPermissionService.getUsersWithPermission( item, WikiItemUserPermission.PERMISSION_EDIT );

        Map<String, Object> model = getModel( );
        model.put( MARK_ITEM, item );
        model.put( MARK_ITEM_TYPE, item.getType( ) );
        model.put( MARK_USER, user );
        model.put( MARK_VIEW_USERS, viewUsers );
        model.put( MARK_EDIT_USERS, editUsers );
        model.put( MARK_VIEW_POPULATIONS, WikiPermissionService.getAttributesWithPermission( item, WikiItemUserPermission.PERMISSION_VIEW ) );
        model.put( MARK_EDIT_POPULATIONS, WikiPermissionService.getAttributesWithPermission( item, WikiItemUserPermission.PERMISSION_EDIT ) );
        model.put( MARK_USER_SEARCH_AVAILABLE, _externalUserSearchService.isAvailable( ) );
        model.put( MARK_PERMISSION_SEARCH_FIELDS, getProviderSearchFields( getLocale( request ) ) );
        model.put( MARK_USER_ROLES, getUserRoles( user, getLocale( request ) ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_UPDATE_ITEM ) );
        List<Revision> revisionHistory = RevisionService.getRevisionHistory( item.getId( ) );
        model.put( MARK_REVISION_HISTORY, revisionHistory );

        populateNavigationContext( model, item.getParent( ) );

        XPage page = getXPage( TEMPLATE_MODIFY_ITEM, getLocale( request ), model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_MODIFY_ITEM_TITLE, getLocale( request ) ) );
        return page;
    }

    /**
     * Processes the item update
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @Action( ACTION_UPDATE_ITEM )
    public XPage doUpdateItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        validateToken( request, ACTION_UPDATE_ITEM );

        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID ) );
        AbstractWikiItem item = WikiItemService.findById( nId );
        LuteceUser user = checkEditAccess( request, item );

        String strOriginalCode = item.getCode( );
        item.setIsPublished( false );
        populate( item, request );

        try
        {
            WikiItemService.update( item, getLocale( request ) );

            String strTitleEncoded = request.getParameter( "title_encoded" );
            if ( strTitleEncoded != null && !strTitleEncoded.trim( ).isEmpty( ) )
            {
                String strAuthor = WikiUserDisplayName.of( user );

                Revision revision = new Revision( );
                revision.setEntityId( item.getId( ) );
                revision.setAuthor( strAuthor );
                populateWithDecode( revision, request );
                revision.setIsCurrent( true );

                RevisionService.create( revision );
            }

            addFlashInfo( request, MESSAGE_ITEM_UPDATED );
            return redirectToView( request, item );
        }
        catch( WikiValidationException e )
        {
            addError( e.getMessage( ) );
            Map<String, String> params = new HashMap<>( );
            params.put( PARAMETER_CODE, strOriginalCode );
            return redirect( request, VIEW_MODIFY_ITEM, params );
        }
    }

    /**
     * Displays the item removal confirmation
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     * @throws SiteMessageException
     *             if site message error occurs
     */
    @Action( ACTION_CONFIRM_REMOVE_ITEM )
    public XPage confirmRemoveItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException, SiteMessageException
    {
        String strCode = request.getParameter( PARAMETER_CODE );
        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        checkEditAccess( request, item );

        UrlItem url = new UrlItem( AppPathService.getPortalUrl( ) );
        url.addParameter( PARAMETER_PAGE, XPAGE_NAME );
        url.addParameter( PARAMETER_ACTION, ACTION_REMOVE_ITEM );
        url.addParameter( PARAMETER_ID, String.valueOf( item.getId( ) ) );
        url.addParameter( SecurityTokenService.PARAMETER_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_REMOVE_ITEM ) );

        String strConfirmMessage = getConfirmMessageForItemType( item );
        SiteMessageService.setMessage( request, strConfirmMessage, SiteMessage.TYPE_CONFIRMATION, url.getUrl( ) );
        return null;
    }

    private String getConfirmMessageForItemType( AbstractWikiItem item )
    {
        switch( item.getType( ) )
        {
            case SPACE:
                return MESSAGE_CONFIRM_REMOVE_SPACE;
            case CATEGORY:
                return MESSAGE_CONFIRM_REMOVE_CATEGORY;
            case BOOK:
                return MESSAGE_CONFIRM_REMOVE_BOOK;
            case CHAPTER:
                return MESSAGE_CONFIRM_REMOVE_CHAPTER;
            case PAGE:
                return MESSAGE_CONFIRM_REMOVE_PAGE;
            default:
                return MESSAGE_CONFIRM_REMOVE_ITEM;
        }
    }

    /**
     * Processes the item removal
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @Action( ACTION_REMOVE_ITEM )
    public XPage doRemoveItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        validateToken( request, ACTION_REMOVE_ITEM );

        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID ) );
        AbstractWikiItem item = WikiItemService.findById( nId );
        checkEditAccess( request, item );

        WikiItemService.delete( nId );
        addFlashInfo( request, MESSAGE_ITEM_REMOVED );

        return redirectToParentView( request, item );
    }

    /**
     * Processes the revision restoration
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @Action( ACTION_RESTORE_REVISION )
    public XPage doRestoreRevision( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        validateToken( request, ACTION_RESTORE_REVISION );

        int nRevisionId = Integer.parseInt( request.getParameter( PARAMETER_ID_REVISION ) );
        Revision revision = RevisionService.findById( nRevisionId );
        if ( revision == null )
        {
            throw new AccessDeniedException( REVISION_NOT_FOUND_MESSAGE );
        }

        AbstractWikiItem item = WikiItemService.findById( revision.getEntityId( ) );
        LuteceUser user = checkEditAccess( request, item );

        RevisionService.restoreRevision( nRevisionId, WikiUserDisplayName.of( user ) );
        addFlashInfo( request, MESSAGE_REVISION_RESTORED );

        return redirectToView( request, item );
    }

    /**
     * Displays the revision view
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @View( VIEW_VIEW_REVISION )
    public XPage viewRevision( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        int nRevisionId = Integer.parseInt( request.getParameter( PARAMETER_ID ) );
        Revision revision = RevisionService.findById( nRevisionId );
        if ( revision == null )
        {
            throw new AccessDeniedException( REVISION_NOT_FOUND_MESSAGE );
        }

        AbstractWikiItem item = WikiItemService.findById( revision.getEntityId( ) );
        LuteceUser user = checkViewAccess( request, item );

        Map<String, Object> model = getModel( );
        model.put( MARK_REVISION, revision );
        model.put( MARK_ITEM, item );
        model.put( MARK_USER, user );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_RESTORE_REVISION ) );

        XPage page = getXPage( TEMPLATE_VIEW_REVISION, getLocale( request ), model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_VIEW_REVISION_TITLE, getLocale( request ) ) );
        return page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getViewUrl( String view )
    {
        if ( NAV_VIEW_LIST_WIKI.equals( view ) || NAV_VIEW_SPACE.equals( view ) || NAV_VIEW_BOOK.equals( view ) || NAV_VIEW_PAGE.equals( view ) )
        {
            UrlItem url = new UrlItem( PORTAL_JSP_URL );
            url.addParameter( PARAMETER_PAGE, NAV_XPAGE );
            url.addParameter( NAV_PARAM_VIEW, view );
            return url.getUrl( );
        }
        return super.getViewUrl( view );
    }

    /**
     * Displays the item children management interface
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @View( VIEW_MANAGE_ITEM_CHILDREN )
    public XPage manageItemChildren( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        applyFlashMessages( request );

        String strCode = request.getParameter( PARAMETER_CODE );
        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        LuteceUser user = checkEditAccess( request, item );

        List<AbstractWikiItem> children = WikiItemService.getItemsByParent( item.getId( ) );
        WikiItemType primaryChildType = item.getChildType( );
        Set<WikiItemType> childTypes = item.getAllowedChildTypes( );
        String strChildType = primaryChildType != null ? primaryChildType.getCode( ) : null;
        List<String> childTypeCodes = childTypes.stream( ).map( WikiItemType::getCode ).collect( Collectors.toList( ) );

        Map<String, Object> model = getModel( );
        model.put( MARK_ITEM, item );
        model.put( MARK_CHILDREN, children );
        model.put( MARK_CHILD_TYPE, strChildType );
        model.put( MARK_CHILD_TYPES, childTypeCodes );
        model.put( MARK_USER, user );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_REORDER_ITEM_CHILDREN ) );

        populateNavigationContext( model, item.getParent( ) );

        XPage page = getXPage( TEMPLATE_MANAGE_ITEM_CHILDREN, getLocale( request ), model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_MANAGE_ITEM_CHILDREN_TITLE, getLocale( request ) ) );
        return page;
    }

    /**
     * Processes the item children reordering
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     * @throws WikiValidationException
     *             if validation error occurs
     */
    @Action( ACTION_REORDER_ITEM_CHILDREN )
    public XPage doReorderItemChildren( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException, WikiValidationException
    {
        validateToken( request, ACTION_REORDER_ITEM_CHILDREN );

        String strCode = request.getParameter( PARAMETER_CODE );
        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        checkEditAccess( request, item );

        String [ ] childIds = request.getParameterValues( PARAMETER_CHILD_ID );
        if ( childIds != null )
        {
            for ( int i = 0; i < childIds.length; i++ )
            {
                int nChildId = Integer.parseInt( childIds [i] );
                AbstractWikiItem child = WikiItemService.findById( nChildId );
                if ( child.hasOrder( ) )
                {
                    child.setOrder( i + 1 );
                    WikiItemService.update( child, getLocale( request ) );
                }
            }
        }

        addFlashInfo( request, MESSAGE_CHILDREN_REORDERED );

        Map<String, String> params = new HashMap<>( );
        params.put( PARAMETER_CODE, item.getCode( ) );
        return redirect( request, VIEW_MANAGE_ITEM_CHILDREN, params );
    }

    /**
     * Displays the spaces management view
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @View( VIEW_MANAGE_SPACES )
    public XPage manageSpaces( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        applyFlashMessages( request );

        LuteceUser user = checkAdminAccess( request );

        List<AbstractWikiItem> spaces = WikiItemService.getItemsByType( WikiItemType.SPACE );

        Map<String, Object> model = getModel( );
        model.put( MARK_CHILDREN, spaces );
        model.put( MARK_CHILD_TYPE, "space" );
        model.put( MARK_USER, user );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_REORDER_SPACES ) );

        XPage page = getXPage( TEMPLATE_MANAGE_ITEM_CHILDREN, getLocale( request ), model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_MANAGE_ITEM_CHILDREN_TITLE, getLocale( request ) ) );
        return page;
    }

    /**
     * Processes the spaces reordering
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     * @throws WikiValidationException
     *             if validation error occurs
     */
    @Action( ACTION_REORDER_SPACES )
    public XPage doReorderSpaces( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException, WikiValidationException
    {
        validateToken( request, ACTION_REORDER_SPACES );
        checkAdminAccess( request );

        String [ ] childIds = request.getParameterValues( PARAMETER_CHILD_ID );
        if ( childIds != null )
        {
            for ( int i = 0; i < childIds.length; i++ )
            {
                int nChildId = Integer.parseInt( childIds [i] );
                AbstractWikiItem space = WikiItemService.findById( nChildId );
                if ( space.hasOrder( ) )
                {
                    space.setOrder( i + 1 );
                    WikiItemService.update( space, getLocale( request ) );
                }
            }
        }

        addFlashInfo( request, MESSAGE_CHILDREN_REORDERED );

        return redirect( request, VIEW_MANAGE_SPACES, new HashMap<>( ) );
    }

    /**
     * Displays the move item form
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @View( VIEW_MOVE_ITEM )
    public XPage moveItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        String strCode = request.getParameter( PARAMETER_CODE );
        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        checkEditAccess( request, item );

        Map<String, Object> model = getModel( );
        model.put( MARK_ITEM, item );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MOVE_ITEM ) );

        XPage page = getXPage( TEMPLATE_MOVE_ITEM, getLocale( request ), model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_MOVE_ITEM_TITLE, getLocale( request ) ) );
        return page;
    }

    /**
     * Processes the item move
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @Action( ACTION_MOVE_ITEM )
    public XPage doMoveItem( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        validateToken( request, ACTION_MOVE_ITEM );

        String strCode = request.getParameter( PARAMETER_CODE );
        String strTargetParentId = request.getParameter( PARAMETER_TARGET_PARENT_ID );

        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        checkEditAccess( request, item );

        if ( strTargetParentId != null && !strTargetParentId.isEmpty( ) )
        {
            int nTargetParentId = Integer.parseInt( strTargetParentId );
            AbstractWikiItem targetParent = WikiItemService.findById( nTargetParentId );
            checkEditAccess( request, targetParent );

            try
            {
                WikiItemService.move( item, nTargetParentId, getLocale( request ) );
                addFlashInfo( request, MESSAGE_ITEM_MOVED );
                return redirectToView( request, item );
            }
            catch( WikiValidationException e )
            {
                addError( e.getMessage( ) );
                Map<String, String> params = new HashMap<>( );
                params.put( PARAMETER_CODE, item.getCode( ) );
                return redirect( request, VIEW_MOVE_ITEM, params );
            }
        }

        Map<String, String> params = new HashMap<>( );
        params.put( PARAMETER_CODE, item.getCode( ) );
        return redirect( request, VIEW_MOVE_ITEM, params );
    }

    /**
     * Remove a user permission
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if user is not signed in
     * @throws AccessDeniedException
     *             if access is denied
     */
    @Action( ACTION_REMOVE_USER_PERMISSION )
    public XPage doRemoveUserPermission( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        validateToken( request, ACTION_UPDATE_ITEM );

        String strCode = request.getParameter( PARAMETER_CODE );
        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        checkEditAccess( request, item );

        if ( !_externalUserSearchService.isAvailable( ) )
        {
            throw new AccessDeniedException( "User search service is not available" );
        }

        String strUserGuid = request.getParameter( PARAMETER_USER_GUID );
        String strPermissionType = request.getParameter( PARAMETER_PERMISSION_TYPE );

        if ( strPermissionType != null && strUserGuid != null && !strUserGuid.isEmpty( ) )
        {
            WikiPermissionService.revoke( item, strUserGuid, strPermissionType );
        }

        return redirectToPermissions( request, strCode );
    }

    /**
     * Checks if user is authenticated
     *
     * @param request
     *            the HTTP request
     * @throws UserNotSignedException
     *             if user is not signed in
     */
    private LuteceUser checkAuthentication( HttpServletRequest request ) throws UserNotSignedException
    {
        LuteceUser user = SecurityService.isAuthenticationEnable( ) ? SecurityService.getInstance( ).getRegisteredUser( request ) : null;
        if ( user == null )
        {
            throw new UserNotSignedException( );
        }
        return user;
    }

    /**
     * Validates the security token for the given action
     *
     * @param request
     *            the HTTP request
     * @param action
     *            the action name
     * @throws AccessDeniedException
     *             if token is invalid
     */
    private void validateToken( HttpServletRequest request, String action ) throws AccessDeniedException
    {
        if ( !SecurityTokenService.getInstance( ).validate( request, action ) )
        {
            throw new AccessDeniedException( INVALID_SECURITY_TOKEN_MESSAGE );
        }
    }

    /**
     * Checks if user has edit access to the item
     *
     * @param request
     *            the HTTP request
     * @param item
     *            the wiki item
     * @throws AccessDeniedException
     *             if access is denied
     */
    private LuteceUser checkEditAccess( HttpServletRequest request, AbstractWikiItem item ) throws UserNotSignedException, AccessDeniedException
    {
        LuteceUser user = checkAuthentication( request );
        if ( item == null || !WikiAccessControlService.canEdit( user, item ) )
        {
            throw new AccessDeniedException( ACCESS_DENIED_MESSAGE );
        }
        return user;
    }

    /**
     * Checks if user has permission to create an item of the specified type
     *
     * @param user
     *            the Lutece user
     * @param itemType
     *            the type of item to create
     * @param parent
     *            the parent item (can be null for top-level items)
     * @throws AccessDeniedException
     *             if user doesn't have permission to create this type of item
     */
    private LuteceUser checkCreateAccess( HttpServletRequest request, WikiItemType itemType, AbstractWikiItem parent )
            throws UserNotSignedException, AccessDeniedException
    {
        LuteceUser user = checkAuthentication( request );
        switch( itemType )
        {
            case SPACE:
                if ( !WikiAccessControlService.canCreateSpace( user ) )
                {
                    throw new AccessDeniedException( ACCESS_DENIED_MESSAGE );
                }
                break;

            case BOOK:
                if ( !WikiAccessControlService.canCreateBook( user ) )
                {
                    throw new AccessDeniedException( ACCESS_DENIED_MESSAGE );
                }
                break;

            case CATEGORY:
            case CHAPTER:
            case PAGE:
                if ( parent == null || !WikiAccessControlService.canEdit( user, parent ) )
                {
                    throw new AccessDeniedException( ACCESS_DENIED_MESSAGE );
                }
                break;

            default:
                throw new AccessDeniedException( ACCESS_DENIED_MESSAGE );
        }
        return user;
    }

    /**
     * Checks if user has view access to the item
     *
     * @param request
     *            the HTTP request
     * @param item
     *            the wiki item
     * @throws AccessDeniedException
     *             if access is denied
     */
    private LuteceUser checkViewAccess( HttpServletRequest request, AbstractWikiItem item ) throws UserNotSignedException, AccessDeniedException
    {
        LuteceUser user = checkAuthentication( request );
        if ( item == null || !WikiAccessControlService.canView( user, item ) )
        {
            throw new AccessDeniedException( ACCESS_DENIED_MESSAGE );
        }
        return user;
    }

    /**
     * Checks if user is a wiki administrator
     *
     * @param request
     *            the HTTP request
     * @throws AccessDeniedException
     *             if access is denied
     */
    private LuteceUser checkAdminAccess( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException
    {
        LuteceUser user = checkAuthentication( request );
        if ( !WikiAccessControlService.isWikiAdmin( user ) )
        {
            throw new AccessDeniedException( ACCESS_DENIED_MESSAGE );
        }
        return user;
    }

    /**
     * Populates a bean with decoded request parameters
     *
     * @param bean
     *            the bean to populate
     * @param request
     *            the HTTP request
     */
    private void populateWithDecode( Object bean, HttpServletRequest request )
    {
        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper( request )
        {
            @Override
            public String getParameter( String name )
            {
                String encodedValue = super.getParameter( name + PARAMETER_ENCODED_SUFFIX );
                if ( encodedValue != null && !encodedValue.isEmpty( ) )
                {
                    byte [ ] decodedBytes = Base64.getDecoder( ).decode( encodedValue );
                    return new String( decodedBytes, StandardCharsets.UTF_8 );
                }
                return super.getParameter( name );
            }

            @Override
            public String [ ] getParameterValues( String name )
            {
                String value = getParameter( name );
                return value != null ? new String [ ] {
                        value
                } : super.getParameterValues( name );
            }

            @Override
            public Map<String, String [ ]> getParameterMap( )
            {
                Map<String, String [ ]> map = new HashMap<>( super.getParameterMap( ) );
                for ( String paramName : super.getParameterMap( ).keySet( ) )
                {
                    if ( paramName.endsWith( PARAMETER_ENCODED_SUFFIX ) )
                    {
                        String fieldName = paramName.substring( 0, paramName.length( ) - PARAMETER_ENCODED_SUFFIX.length( ) );
                        String decodedValue = getParameter( fieldName );
                        if ( decodedValue != null )
                        {
                            map.put( fieldName, new String [ ] {
                                    decodedValue
                            } );
                        }
                    }
                }
                return map;
            }
        };
        BeanUtil.populate( bean, wrappedRequest );
    }

    /**
     * Redirects to the modification page of an item. The page itself puts the view back on the user
     * permission panel: an anchor would do it too, but the browser animates that jump because
     * Bootstrap sets a smooth scroll behaviour.
     *
     * @param request
     *            the HTTP request
     * @param strCode
     *            the item code
     * @return the redirection
     */
    private XPage redirectToPermissions( HttpServletRequest request, String strCode )
    {
        Map<String, String> params = new HashMap<>( );
        params.put( PARAMETER_CODE, strCode );

        return redirect( request, VIEW_MODIFY_ITEM, params );
    }

    /**
     * Redirects to the public view of an item.
     *
     * @param request
     *            the HTTP request
     * @param item
     *            the wiki item
     * @return the redirection
     */
    private XPage redirectToView( HttpServletRequest request, AbstractWikiItem item )
    {
        return redirect( request, item.getViewUrl( ) );
    }

    /**
     * Redirects to the parent view of the given wiki item
     *
     * @param request
     *            the HTTP request
     * @param item
     *            the wiki item
     * @return the XPage
     */
    private XPage redirectToParentView( HttpServletRequest request, AbstractWikiItem item )
    {
        return redirect( request, item.getParentViewUrl( ) );
    }

    /**
     * Gets the title key for new item creation based on item type
     *
     * @param type
     *            the wiki item type
     * @return the title key
     */
    private String getNewItemTitleKey( WikiItemType type )
    {
        switch( type )
        {
            case CATEGORY:
                return MESSAGE_NEW_CATEGORY_TITLE;
            case BOOK:
                return MESSAGE_NEW_BOOK_TITLE;
            case CHAPTER:
                return MESSAGE_NEW_CHAPTER_TITLE;
            case PAGE:
                return MESSAGE_NEW_PAGE_TITLE;
            default:
                return MESSAGE_NEW_BOOK_TITLE;
        }
    }

    /**
     * Resolves the searchable provider attributes into labelled criteria, so a template can render
     * the search fields without joining the mappings itself. The set depends on the authentication
     * provider configuration, so it is read at each call.
     *
     * @param locale
     *            the locale for the attribute labels
     * @return the criteria, code being the provider attribute id and name its label
     */
    private ReferenceList getProviderSearchFields( Locale locale )
    {
        ReferenceList listFields = new ReferenceList( );

        if ( !_externalUserSearchService.isAvailable( ) )
        {
            return listFields;
        }

        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );

        Map<Integer, IAttribute> mapAttributesById = new HashMap<>( );
        for ( IAttribute attribute : AttributeHome.findAll( locale, myLutecePlugin ) )
        {
            mapAttributesById.put( attribute.getIdAttribute( ), attribute );
        }

        for ( AttributeMapping mapping : AttributeMappingHome.getAttributeMappingsList( ) )
        {
            IAttribute attribute = mapAttributesById.get( mapping.getId( ) );
            if ( attribute != null )
            {
                listFields.addItem( mapping.getIdProviderAttribute( ).trim( ), attribute.getTitle( ) );
            }
        }

        return listFields;
    }

    /**
     * Gets the list of available roles for the user
     *
     * @param user
     *            the Lutece user
     * @param locale
     *            the locale
     * @return the list of roles (always includes "none" + user roles)
     */
    private ReferenceList getUserRoles( LuteceUser user, Locale locale )
    {
        ReferenceList userRoles = new ReferenceList( );
        userRoles.addItem( WikiPermissionService.ROLE_NONE, I18nService.getLocalizedString( "wiki.role.none", locale ) );
        userRoles.addItem( WikiPermissionService.ROLE_PRIVATE, I18nService.getLocalizedString( "wiki.role.private", locale ) );

        if ( user != null && user.getRoles( ) != null )
        {
            for ( String strRole : user.getRoles( ) )
            {
                Role role = RoleHome.findByPrimaryKey( strRole );
                if ( role != null )
                {
                    userRoles.addItem( strRole, role.getRoleDescription( ) );
                }
                else
                {
                    userRoles.addItem( strRole, strRole );
                }
            }
        }
        return userRoles;
    }

}
