/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestedRevision;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestedRevisionHome;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestionStatus;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.security.LuteceUser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service handling the lifecycle of suggested revisions: creating
 * a proposal, listing the inbox of a reviewer, approving (which materialises
 * a real Revision via {@link RevisionService}) and rejecting.
 */
public final class SuggestedRevisionService
{
    private static final String COMMENT_FROM_SUGGESTION = "Approved from suggestion #";

    private SuggestedRevisionService( )
    {
    }

    /**
     * Persists a new pending suggestion proposed by a user without edit rights.
     *
     * @param suggestion
     *            the suggestion to create (status will be forced to PENDING)
     * @return the persisted suggestion
     */
    public static SuggestedRevision create( SuggestedRevision suggestion )
    {
        suggestion.setStatus( SuggestionStatus.PENDING );
        return SuggestedRevisionHome.create( suggestion );
    }

    /**
     * Returns a suggestion by its primary key.
     *
     * @param nId
     *            the suggestion id
     * @return the suggestion or null
     */
    public static SuggestedRevision findById( int nId )
    {
        return SuggestedRevisionHome.findByPrimaryKey( nId );
    }

    /**
     * Returns the count of pending suggestions on a wiki item.
     *
     * @param nEntityId
     *            the wiki item id
     * @return the count
     */
    public static int countPendingByEntity( int nEntityId )
    {
        return SuggestedRevisionHome.countPendingByEntity( nEntityId );
    }

    /**
     * Returns the suggestions submitted by the given user, regardless of status.
     *
     * @param user
     *            the lutece user
     * @return the user's own suggestions
     */
    public static List<SuggestedRevision> getMyProposals( LuteceUser user )
    {
        if ( user == null )
        {
            return new ArrayList<>( );
        }
        return SuggestedRevisionHome.getByAuthor( user.getName( ) );
    }

    /**
     * Returns the pending suggestion submitted by the given user on the given wiki item, if one exists. Used to enforce the "one pending proposal per user per
     * page" rule and to surface a warning on the page view.
     *
     * @param user
     *            the lutece user
     * @param nEntityId
     *            the wiki item id
     * @return the pending suggestion if any, null otherwise
     */
    public static SuggestedRevision findMyPending( LuteceUser user, int nEntityId )
    {
        if ( user == null )
        {
            return null;
        }
        return SuggestedRevisionHome.getPendingByEntityAndAuthor( nEntityId, user.getName( ) ).orElse( null );
    }

    /**
     * Returns the list of pending suggestions targeting wiki items the given
     * user has the right to edit. Used to populate the reviewer's inbox.
     *
     * @param user
     *            the lutece user
     * @return pending suggestions visible to this reviewer
     */
    public static List<SuggestedRevision> getPendingForReviewer( LuteceUser user )
    {
        if ( user == null )
        {
            return new ArrayList<>( );
        }
        List<SuggestedRevision> pending = SuggestedRevisionHome.getAllPending( );
        Map<Integer, AbstractWikiItem> itemsById = loadItemsByEntityId( pending );

        List<SuggestedRevision> result = new ArrayList<>( pending.size( ) );
        for ( SuggestedRevision s : pending )
        {
            AbstractWikiItem item = itemsById.get( s.getEntityId( ) );
            if ( item != null && WikiAccessControlService.canEdit( user, item ) )
            {
                result.add( s );
            }
        }
        return result;
    }

    /**
     * Bulk-loads the wiki items referenced by a list of suggestions, in a single query.
     *
     * @param suggestions
     *            the suggestions to load items for
     * @return a map of entity id to wiki item
     */
    public static Map<Integer, AbstractWikiItem> loadItemsByEntityId( List<SuggestedRevision> suggestions )
    {
        if ( suggestions == null || suggestions.isEmpty( ) )
        {
            return new java.util.HashMap<>( );
        }
        Set<Integer> ids = suggestions.stream( ).map( SuggestedRevision::getEntityId ).collect( Collectors.toCollection( LinkedHashSet::new ) );
        return WikiItemHome.findByIds( new ArrayList<>( ids ) ).stream( ).collect( Collectors.toMap( AbstractWikiItem::getId, Function.identity( ) ) );
    }

    /**
     * Approves a pending suggestion by creating a new current revision out of
     * its content, and marking the suggestion as APPROVED.
     *
     * @param nSuggestionId
     *            the suggestion id
     * @param reviewer
     *            display name of the approver
     * @param strReviewComment
     *            optional comment from the reviewer
     * @return the approved suggestion (or null if not found / not pending)
     */
    public static SuggestedRevision approve( int nSuggestionId, String reviewer, String strReviewComment )
    {
        SuggestedRevision s = SuggestedRevisionHome.findByPrimaryKey( nSuggestionId );
        if ( s == null || s.getStatus( ) != SuggestionStatus.PENDING )
        {
            return s;
        }

        Revision revision = new Revision( );
        revision.setEntityId( s.getEntityId( ) );
        revision.setTitle( s.getTitle( ) );
        revision.setDescription( s.getDescription( ) );
        revision.setContent( s.getContent( ) );
        revision.setComment( COMMENT_FROM_SUGGESTION + s.getId( ) );
        revision.setAuthor( reviewer );
        revision.setIsCurrent( true );
        RevisionService.create( revision );

        s.setStatus( SuggestionStatus.APPROVED );
        s.setReviewer( reviewer );
        s.setReviewComment( strReviewComment );
        return SuggestedRevisionHome.updateStatus( s );
    }

    /**
     * Rejects a pending suggestion.
     *
     * @param nSuggestionId
     *            the suggestion id
     * @param reviewer
     *            display name of the rejecter
     * @param strReviewComment
     *            optional motive from the reviewer
     * @return the rejected suggestion (or null if not found / not pending)
     */
    public static SuggestedRevision reject( int nSuggestionId, String reviewer, String strReviewComment )
    {
        SuggestedRevision s = SuggestedRevisionHome.findByPrimaryKey( nSuggestionId );
        if ( s == null || s.getStatus( ) != SuggestionStatus.PENDING )
        {
            return s;
        }

        s.setStatus( SuggestionStatus.REJECTED );
        s.setReviewer( reviewer );
        s.setReviewComment( strReviewComment );
        return SuggestedRevisionHome.updateStatus( s );
    }
}
