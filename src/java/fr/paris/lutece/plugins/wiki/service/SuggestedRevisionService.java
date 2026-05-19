/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestedRevision;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestedRevisionHome;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestionStatus;
import fr.paris.lutece.plugins.wiki.service.merge.MergeBlock;
import fr.paris.lutece.plugins.wiki.service.merge.MergeBlocks;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.lang3.StringUtils;

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
     * Tells whether suggestions are allowed on the given item type. Currently restricted to
     * items that carry a reviewable content: pages, books and spaces.
     *
     * @param item
     *            the targeted wiki item
     * @return true when proposing/approving a revision on this item is supported
     */
    public static boolean isSuggestible( AbstractWikiItem item )
    {
        if ( item == null )
        {
            return false;
        }
        WikiItemType type = item.getType( );
        return type == WikiItemType.PAGE || type == WikiItemType.BOOK || type == WikiItemType.SPACE;
    }

    /**
     * Counts pending suggestions for a batch of entities in a single query.
     *
     * @param entityIds
     *            the entity ids
     * @return a map of entity id to pending count
     */
    public static Map<Integer, Integer> countPendingByEntities( List<Integer> entityIds )
    {
        return SuggestedRevisionHome.countPendingByEntities( entityIds );
    }

    /**
     * Returns the suggestions submitted by the given user, regardless of status. Backs the "mine" view (the proposer's history).
     *
     * @param user
     *            the lutece user
     * @return the user's own suggestions
     */
    public static List<SuggestedRevision> getMine( LuteceUser user )
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
     * Returns the list of pending suggestions targeting wiki items the given user has the right to edit. Backs the "review" view (the reviewer's queue).
     *
     * @param user
     *            the lutece user
     * @return pending suggestions visible to this reviewer
     */
    public static List<SuggestedRevision> getReview( LuteceUser user )
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
     * Approves a pending suggestion. With an explicit resolved content, it is applied verbatim;
     * otherwise a three-way merge against the current revision is attempted.
     *
     * @param nSuggestionId
     *            the suggestion id
     * @param reviewer
     *            display name of the approver
     * @param strReviewComment
     *            optional comment from the reviewer
     * @param strResolvedContent
     *            content explicitly resolved by the reviewer, or null to attempt auto-merge
     * @return the approved suggestion, or null when not found or when an unresolved conflict
     *         leaves the suggestion in PENDING
     */
    public static SuggestedRevision approve( int nSuggestionId, String reviewer, String strReviewComment, String strResolvedContent )
    {
        SuggestedRevision s = SuggestedRevisionHome.findByPrimaryKey( nSuggestionId );
        if ( s == null || s.getStatus( ) != SuggestionStatus.PENDING )
        {
            return s;
        }

        String approvedContent = strResolvedContent;
        if ( approvedContent == null )
        {
            Revision current = RevisionService.getCurrentRevision( s.getEntityId( ) );
            approvedContent = resolveAutoMergedContent( s, current );
            if ( approvedContent == null )
            {
                return null;
            }
        }

        Revision revision = new Revision( );
        revision.setEntityId( s.getEntityId( ) );
        revision.setTitle( s.getTitle( ) );
        revision.setDescription( s.getDescription( ) );
        revision.setContent( approvedContent );
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
     * Computes the three-way merge blocks for a suggestion against the given current revision.
     *
     * @param s
     *            the suggestion
     * @param current
     *            the current revision of the targeted item (may be null)
     * @return the ordered merge blocks, or an empty list when no merge is required
     */
    public static List<MergeBlock> computeMergeBlocks( SuggestedRevision s, Revision current )
    {
        if ( s == null || current == null || s.getParentRevisionId( ) <= 0 || current.getId( ) == s.getParentRevisionId( ) )
        {
            return List.of( );
        }
        Revision parent = RevisionService.findById( s.getParentRevisionId( ) );
        if ( parent == null )
        {
            AppLogService.error( "Suggested revision " + s.getId( ) + " references missing parent revision id " + s.getParentRevisionId( )
                    + " — falling back to no-merge" );
            return List.of( );
        }
        String parentContent = StringUtils.defaultString( parent.getContent( ) );
        String currentContent = StringUtils.defaultString( current.getContent( ) );
        if ( parentContent.equals( currentContent ) )
        {
            return List.of( );
        }
        return MergeBlocks.compute( parentContent, currentContent, StringUtils.defaultString( s.getContent( ) ) );
    }

    /**
     * Computes diff statistics for a suggestion against the revision that was current when it was proposed. Mirrors GitHub's {@code +X -Y} indicator. The
     * conflict flag is only meaningful for PENDING suggestions: it tells whether the proposed change still merges cleanly against the up-to-date current
     * revision.
     *
     * @param s
     *            the suggestion to analyse
     * @return a three-element array {@code [additions, deletions, hasConflict]} (hasConflict is 0 or 1)
     */
    public static int [ ] diffStats( SuggestedRevision s )
    {
        if ( s == null )
        {
            return new int [ ] { 0, 0, 0 };
        }
        String baseContent = "";
        if ( s.getParentRevisionId( ) > 0 )
        {
            Revision parent = RevisionService.findById( s.getParentRevisionId( ) );
            if ( parent != null )
            {
                baseContent = StringUtils.defaultString( parent.getContent( ) );
            }
        }
        int [ ] addDel = MergeBlocks.stats( baseContent, StringUtils.defaultString( s.getContent( ) ) );
        int hasConflict = 0;
        if ( s.getStatus( ) == SuggestionStatus.PENDING )
        {
            Revision current = RevisionService.getCurrentRevision( s.getEntityId( ) );
            if ( current != null && MergeBlocks.hasConflict( computeMergeBlocks( s, current ) ) )
            {
                hasConflict = 1;
            }
        }
        return new int [ ] { addDel [0], addDel [1], hasConflict };
    }

    /**
     * Returns the auto-mergeable content for the suggestion against the current revision, or
     * null when human intervention is required.
     *
     * @param s
     *            the suggestion being approved
     * @param current
     *            the revision currently flagged as current
     * @return the auto-merged content, or null when the merge has conflicts
     */
    private static String resolveAutoMergedContent( SuggestedRevision s, Revision current )
    {
        List<MergeBlock> blocks = computeMergeBlocks( s, current );
        if ( blocks.isEmpty( ) )
        {
            return s.getContent( );
        }
        if ( MergeBlocks.hasConflict( blocks ) )
        {
            return null;
        }
        return MergeBlocks.flatten( blocks );
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

    /**
     * Deletes a pending suggestion when initiated by its author. The suggestion must be in PENDING
     * status. Approved or rejected proposals are kept as audit history and cannot be removed.
     *
     * @param nSuggestionId
     *            the suggestion id
     * @param strAuthorGuid
     *            the requester guid (must match the suggestion's author)
     * @return true if the suggestion was deleted, false otherwise
     */
    public static boolean deleteByOwner( int nSuggestionId, String strAuthorGuid )
    {
        SuggestedRevision s = SuggestedRevisionHome.findByPrimaryKey( nSuggestionId );
        if ( s == null || s.getStatus( ) != SuggestionStatus.PENDING )
        {
            return false;
        }
        if ( strAuthorGuid == null || !strAuthorGuid.equals( s.getAuthorGuid( ) ) )
        {
            return false;
        }
        SuggestedRevisionHome.remove( nSuggestionId );
        return true;
    }
}
