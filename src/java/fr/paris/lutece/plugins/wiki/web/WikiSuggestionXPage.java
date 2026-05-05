/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
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

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestedRevision;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestionStatus;
import fr.paris.lutece.plugins.wiki.service.RevisionService;
import fr.paris.lutece.plugins.wiki.service.SuggestedRevisionService;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.xpages.XPage;

/**
 * XPage handling the suggested-revision workflow:
 * <ul>
 * <li>{@code propose} / {@code doPropose}: a user without edit rights submits a proposed modification on a wiki item</li>
 * <li>{@code inbox}: list of pending suggestions on items the current user can edit (the reviewer's inbox)</li>
 * <li>{@code viewSuggestion}: side-by-side view of the current revision vs the proposal, with approve/reject actions</li>
 * <li>{@code doApprove} / {@code doReject}: reviewer actions</li>
 * </ul>
 */
@Controller( xpageName = "wikisuggestion", pageTitleI18nKey = "wiki.xpage.suggestion.pageTitle", pagePathI18nKey = "wiki.xpage.suggestion.pagePathLabel" )
public class WikiSuggestionXPage extends AbstractWikiXPage
{
    private static final long serialVersionUID = 1L;

    private static final String XPAGE_NAME = "wikisuggestion";

    private static final String VIEW_PROPOSE = "propose";
    private static final String VIEW_INBOX = "inbox";
    private static final String VIEW_DETAIL = "viewSuggestion";
    private static final String VIEW_MINE = "myProposals";

    private static final String ACTION_PROPOSE = "doPropose";
    private static final String ACTION_APPROVE = "doApprove";
    private static final String ACTION_REJECT = "doReject";

    private static final String TEMPLATE_PROPOSE = "/skin/plugins/wiki/propose_revision.html";
    private static final String TEMPLATE_INBOX = "/skin/plugins/wiki/suggestion_inbox.html";
    private static final String TEMPLATE_DETAIL = "/skin/plugins/wiki/view_suggestion.html";
    private static final String TEMPLATE_MINE = "/skin/plugins/wiki/my_proposals.html";

    private static final String PARAMETER_CODE = "code";
    private static final String PARAMETER_ID = "id";
    private static final String PARAMETER_REVIEW_COMMENT = "review_comment";
    private static final String PARAMETER_CONTENT = "content";
    private static final String PARAMETER_COMMENT = "comment";
    private static final String ENCODED_SUFFIX = "_encoded";

    private static final String MARK_ITEM = "item";
    private static final String MARK_CURRENT_REVISION = "current_revision";
    private static final String MARK_SUGGESTION = "suggestion";
    private static final String MARK_SUGGESTIONS = "suggestions";
    private static final String MARK_ITEMS_BY_ID = "items_by_id";
    private static final String MARK_CAN_REVIEW = "can_review";

    private static final String MESSAGE_PROPOSE_TITLE = "wiki.xpage.suggestion.propose.pageTitle";
    private static final String MESSAGE_INBOX_TITLE = "wiki.xpage.suggestion.inbox.pageTitle";
    private static final String MESSAGE_DETAIL_TITLE = "wiki.xpage.suggestion.detail.pageTitle";
    private static final String MESSAGE_MINE_TITLE = "wiki.xpage.suggestion.mine.pageTitle";

    private static final String MESSAGE_SUGGESTION_CREATED = "wiki.message.suggestionCreated";
    private static final String MESSAGE_SUGGESTION_COMMENT_REQUIRED = "wiki.message.suggestionCommentRequired";
    private static final String MESSAGE_SUGGESTION_ALREADY_PENDING = "wiki.message.suggestionAlreadyPending";
    private static final String MESSAGE_SUGGESTION_APPROVED = "wiki.message.suggestionApproved";
    private static final String MESSAGE_SUGGESTION_REJECTED = "wiki.message.suggestionRejected";
    private static final String MESSAGE_SUGGESTION_NOT_PENDING = "wiki.message.suggestionNotPending";
    private static final String MESSAGE_NO_ACCESS = "wiki.message.suggestionAccessDenied";

    private static final String INVALID_TOKEN = "Invalid security token";

    /**
     * Displays the form letting a user propose a modification on a wiki item.
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if not authenticated
     * @throws AccessDeniedException
     *             if the user cannot view the item
     */
    @View( value = VIEW_PROPOSE, defaultView = true )
    public XPage propose( HttpServletRequest request ) throws UserNotSignedException, SiteMessageException
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        LuteceUser user = checkAuthenticated( request );
        AbstractWikiItem item = WikiItemService.findByCode( request.getParameter( PARAMETER_CODE ) );

        if ( item == null || !WikiAccessControlService.canView( user, item ) )
        {
            denyAccess( request );
        }

        SuggestedRevision existingPending = SuggestedRevisionService.findMyPending( user, item.getId( ) );
        if ( existingPending != null )
        {
            Map<String, String> params = new HashMap<>( );
            params.put( PARAMETER_ID, String.valueOf( existingPending.getId( ) ) );
            return redirect( request, VIEW_DETAIL, params );
        }

        Revision currentRevision = RevisionService.getCurrentRevision( item.getId( ) );

        Map<String, Object> model = getModel( );
        model.put( MARK_ITEM, item );
        model.put( MARK_CURRENT_REVISION, currentRevision );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_PROPOSE ) );
        populateCommonModel( model, user );

        XPage page = getXPage( TEMPLATE_PROPOSE, locale, model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_PROPOSE_TITLE, locale ) );
        return page;
    }

    /**
     * Persists a new pending suggestion submitted by the proposer.
     *
     * @param request
     *            the HTTP request
     * @return the XPage redirecting back to the item view
     * @throws UserNotSignedException
     *             if not authenticated
     * @throws AccessDeniedException
     *             if access is denied or the token is invalid
     */
    @Action( ACTION_PROPOSE )
    public XPage doPropose( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException, SiteMessageException
    {
        validateToken( request, ACTION_PROPOSE );

        LuteceUser user = checkAuthenticated( request );
        AbstractWikiItem item = WikiItemService.findByCode( request.getParameter( PARAMETER_CODE ) );

        if ( item == null || !WikiAccessControlService.canView( user, item ) )
        {
            denyAccess( request );
        }

        String strComment = decodedParam( request, PARAMETER_COMMENT );
        if ( strComment == null || strComment.trim( ).isEmpty( ) )
        {
            addFlashError( request, MESSAGE_SUGGESTION_COMMENT_REQUIRED );
            Map<String, String> params = new HashMap<>( );
            params.put( PARAMETER_CODE, item.getCode( ) );
            return redirect( request, VIEW_PROPOSE, params );
        }

        SuggestedRevision existingPending = SuggestedRevisionService.findMyPending( user, item.getId( ) );
        if ( existingPending != null )
        {
            addFlashError( request, MESSAGE_SUGGESTION_ALREADY_PENDING );
            Map<String, String> params = new HashMap<>( );
            params.put( PARAMETER_ID, String.valueOf( existingPending.getId( ) ) );
            return redirect( request, VIEW_DETAIL, params );
        }

        Revision currentRevision = RevisionService.getCurrentRevision( item.getId( ) );

        SuggestedRevision suggestion = new SuggestedRevision( );
        suggestion.setEntityId( item.getId( ) );
        suggestion.setTitle( currentRevision != null ? currentRevision.getTitle( ) : item.getCode( ) );
        suggestion.setDescription( currentRevision != null ? currentRevision.getDescription( ) : null );
        suggestion.setContent( decodedParam( request, PARAMETER_CONTENT ) );
        suggestion.setComment( strComment );
        suggestion.setAuthor( buildDisplayName( user ) );
        suggestion.setAuthorGuid( user.getName( ) );

        SuggestedRevisionService.create( suggestion );

        addFlashInfo( request, MESSAGE_SUGGESTION_CREATED );
        return redirect( request, item.getViewUrl( ) );
    }

    /**
     * Displays the reviewer's inbox: pending suggestions on items the user can edit.
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if not authenticated
     */
    @View( VIEW_INBOX )
    public XPage inbox( HttpServletRequest request ) throws UserNotSignedException
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        LuteceUser user = checkAuthenticated( request );

        List<SuggestedRevision> pending = SuggestedRevisionService.getPendingForReviewer( user );

        Map<String, Object> model = getModel( );
        model.put( MARK_SUGGESTIONS, pending );
        model.put( MARK_ITEMS_BY_ID, buildItemsByIdMap( pending ) );
        populateCommonModel( model, user );

        XPage page = getXPage( TEMPLATE_INBOX, locale, model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_INBOX_TITLE, locale ) );
        return page;
    }

    /**
     * Displays the proposer's history: every suggestion they have submitted, with current status.
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if not authenticated
     */
    @View( VIEW_MINE )
    public XPage myProposals( HttpServletRequest request ) throws UserNotSignedException
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        LuteceUser user = checkAuthenticated( request );

        List<SuggestedRevision> proposals = SuggestedRevisionService.getMyProposals( user );

        Map<String, Object> model = getModel( );
        model.put( MARK_SUGGESTIONS, proposals );
        model.put( MARK_ITEMS_BY_ID, buildItemsByIdMap( proposals ) );
        populateCommonModel( model, user );

        XPage page = getXPage( TEMPLATE_MINE, locale, model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_MINE_TITLE, locale ) );
        return page;
    }

    /**
     * Displays the side-by-side detail of one suggestion (current revision vs proposal).
     *
     * @param request
     *            the HTTP request
     * @return the XPage
     * @throws UserNotSignedException
     *             if not authenticated
     * @throws AccessDeniedException
     *             if the user cannot edit the targeted item
     */
    @View( VIEW_DETAIL )
    public XPage viewSuggestion( HttpServletRequest request ) throws UserNotSignedException, SiteMessageException
    {
        applyFlashMessages( request );

        Locale locale = getLocale( request );
        LuteceUser user = checkAuthenticated( request );

        SuggestedRevision suggestion = loadSuggestion( request );
        AbstractWikiItem item = suggestion != null ? WikiItemService.findById( suggestion.getEntityId( ) ) : null;
        if ( suggestion == null || item == null )
        {
            denyAccess( request );
        }

        boolean canEditItem = WikiAccessControlService.canEdit( user, item );
        boolean isOwner = user.getName( ) != null && user.getName( ).equals( suggestion.getAuthorGuid( ) );
        if ( !canEditItem && !isOwner )
        {
            denyAccess( request );
        }
        boolean canReview = canEditItem && suggestion.getStatus( ) == SuggestionStatus.PENDING;

        Revision currentRevision = RevisionService.getCurrentRevision( item.getId( ) );

        Map<String, Object> model = getModel( );
        model.put( MARK_ITEM, item );
        model.put( MARK_CURRENT_REVISION, currentRevision );
        model.put( MARK_SUGGESTION, suggestion );
        model.put( MARK_CAN_REVIEW, canReview );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_APPROVE ) );
        populateCommonModel( model, user );

        XPage page = getXPage( TEMPLATE_DETAIL, locale, model );
        page.setTitle( I18nService.getLocalizedString( MESSAGE_DETAIL_TITLE, locale ) );
        return page;
    }

    /**
     * Approves a pending suggestion: creates a real Revision and marks the suggestion APPROVED.
     *
     * @param request
     *            the HTTP request
     * @return the XPage redirecting to the inbox
     * @throws UserNotSignedException
     *             if not authenticated
     * @throws AccessDeniedException
     *             if the user cannot edit the targeted item or the token is invalid
     */
    @Action( ACTION_APPROVE )
    public XPage doApprove( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException, SiteMessageException
    {
        validateToken( request, ACTION_APPROVE );
        return reviewAction( request, true );
    }

    /**
     * Rejects a pending suggestion.
     *
     * @param request
     *            the HTTP request
     * @return the XPage redirecting to the inbox
     * @throws UserNotSignedException
     *             if not authenticated
     * @throws AccessDeniedException
     *             if the user cannot edit the targeted item or the token is invalid
     */
    @Action( ACTION_REJECT )
    public XPage doReject( HttpServletRequest request ) throws UserNotSignedException, AccessDeniedException, SiteMessageException
    {
        validateToken( request, ACTION_APPROVE );
        return reviewAction( request, false );
    }

    /**
     * Shared logic for approve/reject: enforces edit rights, performs the action, sets a flash message and redirects.
     *
     * @param request
     *            the HTTP request
     * @param approve
     *            true to approve, false to reject
     * @return the redirect XPage
     * @throws UserNotSignedException
     *             if not authenticated
     * @throws AccessDeniedException
     *             if the user cannot edit the targeted item
     */
    private XPage reviewAction( HttpServletRequest request, boolean approve ) throws UserNotSignedException, SiteMessageException
    {
        LuteceUser user = checkAuthenticated( request );
        SuggestedRevision suggestion = loadSuggestion( request );
        AbstractWikiItem item = suggestion != null ? WikiItemService.findById( suggestion.getEntityId( ) ) : null;
        if ( suggestion == null || item == null || !WikiAccessControlService.canEdit( user, item ) )
        {
            denyAccess( request );
        }

        if ( suggestion.getStatus( ) != SuggestionStatus.PENDING )
        {
            addFlashError( request, MESSAGE_SUGGESTION_NOT_PENDING );
            return redirect( request, VIEW_INBOX, new HashMap<>( ) );
        }

        String reviewer = buildDisplayName( user );
        String reviewComment = request.getParameter( PARAMETER_REVIEW_COMMENT );

        if ( approve )
        {
            SuggestedRevisionService.approve( suggestion.getId( ), reviewer, reviewComment );
            addFlashInfo( request, MESSAGE_SUGGESTION_APPROVED );
        }
        else
        {
            SuggestedRevisionService.reject( suggestion.getId( ), reviewer, reviewComment );
            addFlashInfo( request, MESSAGE_SUGGESTION_REJECTED );
        }
        return redirect( request, VIEW_INBOX, new HashMap<>( ) );
    }

    /**
     * Builds a map keyed by entity-id (as String to fit the FreeMarker context) holding the wiki item targeted by each suggestion. Items are bulk-loaded in
     * one query.
     *
     * @param suggestions
     *            the suggestions whose targeted items must be resolved
     * @return a map of entity id (String) to wiki item
     */
    private Map<String, AbstractWikiItem> buildItemsByIdMap( List<SuggestedRevision> suggestions )
    {
        Map<Integer, AbstractWikiItem> loaded = SuggestedRevisionService.loadItemsByEntityId( suggestions );
        Map<String, AbstractWikiItem> result = new HashMap<>( loaded.size( ) );
        for ( Map.Entry<Integer, AbstractWikiItem> entry : loaded.entrySet( ) )
        {
            result.put( String.valueOf( entry.getKey( ) ), entry.getValue( ) );
        }
        return result;
    }

    /**
     * Loads the suggestion designated by the {@code id} request parameter.
     *
     * @param request
     *            the HTTP request
     * @return the suggestion, or null if the parameter is missing/invalid or no suggestion matches
     */
    private SuggestedRevision loadSuggestion( HttpServletRequest request )
    {
        String strId = request.getParameter( PARAMETER_ID );
        if ( strId == null || strId.isEmpty( ) )
        {
            return null;
        }
        try
        {
            return SuggestedRevisionService.findById( Integer.parseInt( strId ) );
        }
        catch ( NumberFormatException e )
        {
            return null;
        }
    }

    /**
     * Aborts the request with a Lutece site message page (HTTP 200 with a friendly error UI). The thrown {@link SiteMessageException} is caught natively by the
     * Lutece front-office so the caller never returns past this call.
     *
     * @param request
     *            the HTTP request
     * @throws SiteMessageException
     *             always, to interrupt the controller flow
     */
    private void denyAccess( HttpServletRequest request ) throws SiteMessageException
    {
        SiteMessageService.setMessage( request, MESSAGE_NO_ACCESS, SiteMessage.TYPE_STOP );
    }

    /**
     * Returns the authenticated user, or throws if not signed in.
     *
     * @param request
     *            the HTTP request
     * @return the lutece user
     * @throws UserNotSignedException
     *             if no user is registered
     */
    private LuteceUser checkAuthenticated( HttpServletRequest request ) throws UserNotSignedException
    {
        LuteceUser user = SecurityService.isAuthenticationEnable( ) ? SecurityService.getInstance( ).getRegisteredUser( request ) : null;
        if ( user == null )
        {
            throw new UserNotSignedException( );
        }
        return user;
    }

    /**
     * Validates the security token bound to the given action.
     *
     * @param request
     *            the HTTP request
     * @param action
     *            the action name
     * @throws AccessDeniedException
     *             if the token is invalid
     */
    private void validateToken( HttpServletRequest request, String action ) throws AccessDeniedException
    {
        if ( !SecurityTokenService.getInstance( ).validate( request, action ) )
        {
            throw new AccessDeniedException( INVALID_TOKEN );
        }
    }

    /**
     * Returns the value of a request parameter, decoding it from base64 if a {@code <name>_encoded} variant was posted by the xss-safe form encoder.
     *
     * @param request
     *            the HTTP request
     * @param name
     *            the parameter name
     * @return the decoded value (may be null)
     */
    private String decodedParam( HttpServletRequest request, String name )
    {
        String encoded = request.getParameter( name + ENCODED_SUFFIX );
        if ( encoded != null && !encoded.isEmpty( ) )
        {
            byte [ ] decodedBytes = Base64.getDecoder( ).decode( encoded );
            return new String( decodedBytes, StandardCharsets.UTF_8 );
        }
        return request.getParameter( name );
    }

    /**
     * Builds a human-readable display name from a lutece user. Falls back to the lutece login when no first/last name is set.
     *
     * @param user
     *            the lutece user
     * @return the display name
     */
    private String buildDisplayName( LuteceUser user )
    {
        String fullName = ( ( user.getFirstName( ) == null ? "" : user.getFirstName( ) ) + " " + ( user.getLastName( ) == null ? "" : user.getLastName( ) ) ).trim( );
        return fullName.isEmpty( ) ? user.getName( ) : fullName;
    }
}
