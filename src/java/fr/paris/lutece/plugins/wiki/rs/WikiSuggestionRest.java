/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.rs;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestedRevision;
import fr.paris.lutece.plugins.wiki.business.suggestedrevision.SuggestionStatus;
import fr.paris.lutece.plugins.wiki.service.SuggestedRevisionService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;

/**
 * REST endpoint exposing every suggestion visible to the authenticated user
 * in a single list, each row tagged with a scope ("mine" or "review"). Drives
 * the unified suggestions page and the header badge.
 */
@Path( "wiki/suggestion" )
public class WikiSuggestionRest
{
    private static final String SCOPE_MINE = "mine";
    private static final String SCOPE_REVIEW = "review";

    @Context
    private HttpServletRequest _request;

    /**
     * Returns all suggestions visible to the current user: every proposal they authored (any status), plus pending proposals on items they can edit and did
     * not author themselves.
     *
     * @return 401 if no user is authenticated, otherwise the JSON list of {@link SuggestionDto}
     */
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response list( )
    {
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( _request );
        if ( user == null )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).build( );
        }

        String guid = user.getName( );
        List<SuggestedRevision> mine = SuggestedRevisionService.getMine( user );
        List<SuggestedRevision> review = SuggestedRevisionService.getReview( user ).stream( ).filter( s -> !guid.equals( s.getAuthorGuid( ) ) )
                .collect( Collectors.toList( ) );

        List<SuggestedRevision> all = new ArrayList<>( mine.size( ) + review.size( ) );
        all.addAll( mine );
        all.addAll( review );
        Map<Integer, AbstractWikiItem> itemsById = SuggestedRevisionService.loadItemsByEntityId( all );

        List<SuggestionDto> result = new ArrayList<>( all.size( ) );
        for ( SuggestedRevision s : mine )
        {
            result.add( SuggestionDto.from( s, itemsById.get( s.getEntityId( ) ), SCOPE_MINE, SuggestedRevisionService.diffStats( s ) ) );
        }
        for ( SuggestedRevision s : review )
        {
            result.add( SuggestionDto.from( s, itemsById.get( s.getEntityId( ) ), SCOPE_REVIEW, SuggestedRevisionService.diffStats( s ) ) );
        }
        return Response.ok( result ).build( );
    }

    /**
     * Slim view of a suggestion suitable for list rendering, without heavy content. The scope tells whether the current user is the proposer
     * ({@code "mine"}) or a potential reviewer ({@code "review"}).
     */
    public static class SuggestionDto
    {
        private final int _nId;
        private final int _nEntityId;
        private final String _strTitle;
        private final SuggestionStatus _status;
        private final String _strAuthor;
        private final Timestamp _dateCreation;
        private final String _strTargetCode;
        private final String _strTargetViewUrl;
        private final String _strScope;
        private final int _nAdditions;
        private final int _nDeletions;
        private final boolean _bHasConflict;

        /**
         * Constructs a DTO with all fields.
         *
         * @param nId
         *            the suggestion id
         * @param nEntityId
         *            the targeted wiki item id
         * @param strTitle
         *            the proposed title
         * @param status
         *            the suggestion status
         * @param strAuthor
         *            the proposer display name
         * @param dateCreation
         *            the creation timestamp
         * @param strTargetCode
         *            the targeted item code (nullable when the item has been removed)
         * @param strTargetViewUrl
         *            the targeted item view URL (nullable when the item has been removed)
         * @param strScope
         *            the scope from the current user's perspective: "mine" or "review"
         * @param nAdditions
         *            number of lines added compared to the parent revision
         * @param nDeletions
         *            number of lines deleted compared to the parent revision
         * @param bHasConflict
         *            true when the suggestion is PENDING and no longer merges cleanly against the current revision
         */
        public SuggestionDto( int nId, int nEntityId, String strTitle, SuggestionStatus status, String strAuthor, Timestamp dateCreation, String strTargetCode,
                String strTargetViewUrl, String strScope, int nAdditions, int nDeletions, boolean bHasConflict )
        {
            _nId = nId;
            _nEntityId = nEntityId;
            _strTitle = strTitle;
            _status = status;
            _strAuthor = strAuthor;
            _dateCreation = dateCreation;
            _strTargetCode = strTargetCode;
            _strTargetViewUrl = strTargetViewUrl;
            _strScope = strScope;
            _nAdditions = nAdditions;
            _nDeletions = nDeletions;
            _bHasConflict = bHasConflict;
        }

        /**
         * Builds a DTO from a domain suggestion, its target wiki item, a scope and pre-computed diff statistics.
         *
         * @param s
         *            the source suggestion
         * @param target
         *            the targeted wiki item, may be null
         * @param scope
         *            "mine" or "review"
         * @param stats
         *            three-element array {@code [additions, deletions, hasConflict]} as returned by {@link SuggestedRevisionService#diffStats}
         * @return the DTO
         */
        public static SuggestionDto from( SuggestedRevision s, AbstractWikiItem target, String scope, int [ ] stats )
        {
            return new SuggestionDto( s.getId( ), s.getEntityId( ), s.getTitle( ), s.getStatus( ), s.getAuthor( ), s.getDateCreation( ),
                    target != null ? target.getCode( ) : null, target != null ? target.getViewUrl( ) : null, scope, stats [0], stats [1], stats [2] != 0 );
        }

        /**
         * @return the suggestion id
         */
        public int getId( )
        {
            return _nId;
        }

        /**
         * @return the targeted wiki item id
         */
        public int getEntityId( )
        {
            return _nEntityId;
        }

        /**
         * @return the proposed title
         */
        public String getTitle( )
        {
            return _strTitle;
        }

        /**
         * @return the suggestion status
         */
        public SuggestionStatus getStatus( )
        {
            return _status;
        }

        /**
         * @return the proposer display name
         */
        public String getAuthor( )
        {
            return _strAuthor;
        }

        /**
         * @return the creation timestamp
         */
        public Timestamp getDateCreation( )
        {
            return _dateCreation;
        }

        /**
         * @return the targeted item code
         */
        public String getTargetCode( )
        {
            return _strTargetCode;
        }

        /**
         * @return the targeted item view URL
         */
        public String getTargetViewUrl( )
        {
            return _strTargetViewUrl;
        }

        /**
         * @return "mine" (the current user is the proposer) or "review" (the current user is a potential reviewer)
         */
        public String getScope( )
        {
            return _strScope;
        }

        /**
         * @return number of lines added compared to the parent revision
         */
        public int getAdditions( )
        {
            return _nAdditions;
        }

        /**
         * @return number of lines deleted compared to the parent revision
         */
        public int getDeletions( )
        {
            return _nDeletions;
        }

        /**
         * @return true when the suggestion is PENDING and conflicts with the current revision
         */
        public boolean getHasConflict( )
        {
            return _bHasConflict;
        }
    }
}
