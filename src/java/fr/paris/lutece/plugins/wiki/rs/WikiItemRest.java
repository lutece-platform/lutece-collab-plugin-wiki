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
package fr.paris.lutece.plugins.wiki.rs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;

@ApplicationScoped
@Path( "wiki/item" )
public class WikiItemRest
{
    @Context
    private HttpServletRequest _request;

    @GET
    @Path( "/{code}" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getItemByCode( @PathParam( "code" ) String strCode )
    {
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( _request );

        AbstractWikiItem item = WikiItemService.findByCode( strCode );

        if ( item == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).build( );
        }

        if ( !WikiAccessControlService.canView( user, item ) )
        {
            return Response.status( Response.Status.FORBIDDEN ).build( );
        }

        return Response.ok( item ).build( );
    }

    /**
     * Search for valid move destinations for an item
     *
     * @param strCode
     *            the code of the item to move
     * @param strSearch
     *            the search term (optional)
     * @param nLimit
     *            the maximum number of results (default 20)
     * @return list of destination options
     */
    @GET
    @Path( "/destinations/{code}" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response searchDestinations( @PathParam( "code" ) String strCode, @QueryParam( "search" ) String strSearch, @QueryParam( "limit" ) Integer nLimit )
    {
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( _request );

        AbstractWikiItem item = WikiItemService.findByCode( strCode );
        if ( item == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).build( );
        }

        if ( !WikiAccessControlService.canEdit( user, item ) )
        {
            return Response.status( Response.Status.FORBIDDEN ).build( );
        }

        int limit = ( nLimit != null && nLimit > 0 ) ? Math.min( nLimit, 100 ) : 20;
        String searchLower = ( strSearch != null ) ? strSearch.toLowerCase( ) : "";

        Set<Integer> itemAncestorIds = getAncestorIds( item );

        Set<WikiItemType> allowedParentTypes = item.getAllowedParentTypes( );
        Set<Integer> seenIds = new HashSet<>( );
        List<AbstractWikiItem> allDestinations = new ArrayList<>( );

        collectDestinations( WikiItemService.getItemsByTypes( allowedParentTypes ), allowedParentTypes, user, item.getIdParent( ), seenIds, allDestinations );

        List<DestinationOption> results = allDestinations.stream( )
                .map( dest -> new DestinationOption( dest.getId( ), dest.getBreadcrumb( ), dest.getType( ).getCode( ),
                        getProximityScore( dest, itemAncestorIds ) ) )
                .filter( opt -> searchLower.isEmpty( ) || opt.getBreadcrumb( ).toLowerCase( ).contains( searchLower ) )
                .sorted( Comparator.comparingInt( DestinationOption::getScore ).reversed( ).thenComparing( DestinationOption::getBreadcrumb ) ).limit( limit )
                .collect( Collectors.toList( ) );

        return Response.ok( results ).build( );
    }

    /**
     * Get all ancestor IDs of an item
     */
    private Set<Integer> getAncestorIds( AbstractWikiItem item )
    {
        Set<Integer> ancestorIds = new HashSet<>( );
        AbstractWikiItem current = item.getParent( );
        while ( current != null )
        {
            ancestorIds.add( current.getId( ) );
            current = current.getParent( );
        }
        return ancestorIds;
    }

    /**
     * Calculate proximity score based on common ancestors
     */
    private int getProximityScore( AbstractWikiItem destination, Set<Integer> itemAncestorIds )
    {
        int score = 0;
        AbstractWikiItem current = destination;
        while ( current != null )
        {
            if ( itemAncestorIds.contains( current.getId( ) ) )
            {
                score++;
            }
            current = current.getParent( );
        }
        return score;
    }

    /**
     * Recursively collects valid destinations
     */
    private void collectDestinations( List<AbstractWikiItem> items, Set<WikiItemType> allowedTypes, LuteceUser user, Integer currentParentId,
            Set<Integer> seenIds, List<AbstractWikiItem> destinations )
    {
        for ( AbstractWikiItem dest : items )
        {
            if ( !seenIds.contains( dest.getId( ) ) && WikiAccessControlService.canEdit( user, dest )
                    && ( currentParentId == null || dest.getId( ) != currentParentId ) )
            {
                seenIds.add( dest.getId( ) );
                destinations.add( dest );

                Set<WikiItemType> childTypes = dest.getAllowedChildTypes( ).stream( ).filter( allowedTypes::contains ).collect( Collectors.toSet( ) );

                if ( !childTypes.isEmpty( ) )
                {
                    List<AbstractWikiItem> children = WikiItemService.getItemsByParent( dest.getId( ) ).stream( )
                            .filter( c -> childTypes.contains( c.getType( ) ) ).collect( Collectors.toList( ) );
                    collectDestinations( children, allowedTypes, user, currentParentId, seenIds, destinations );
                }
            }
        }
    }

    /**
     * DTO for destination search results
     */
    public static class DestinationOption
    {
        private final int _nId;
        private final String _strBreadcrumb;
        private final String _strType;
        private final int _nScore;

        public DestinationOption( int nId, String strBreadcrumb, String strType, int nScore )
        {
            _nId = nId;
            _strBreadcrumb = strBreadcrumb;
            _strType = strType;
            _nScore = nScore;
        }

        public int getId( )
        {
            return _nId;
        }

        public String getBreadcrumb( )
        {
            return _strBreadcrumb;
        }

        public String getType( )
        {
            return _strType;
        }

        public int getScore( )
        {
            return _nScore;
        }
    }
}
