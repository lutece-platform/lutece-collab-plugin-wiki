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
package fr.paris.lutece.plugins.wiki.service.activity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.revision.RevisionHome;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import java.util.stream.Collectors;

/**
 * Service for managing activity timeline
 */
public final class ActivityService
{
    /**
     * Period enumeration for activity filtering
     */
    public enum Period
    {
        LAST_MONTH( "last_month" ),
        LAST_6_MONTHS( "last_6_months" ),
        LAST_YEAR( "last_year" ),
        ALL( "all" );

        private final String _strCode;

        Period( String strCode )
        {
            _strCode = strCode;
        }

        /**
         * Returns the code
         *
         * @return The code
         */
        public String getCode( )
        {
            return _strCode;
        }

        /**
         * Gets a Period from its code
         *
         * @param strCode
         *            The code
         * @return The Period or LAST_MONTH if not found
         */
        public static Period fromCode( String strCode )
        {
            if ( strCode == null )
            {
                return LAST_MONTH;
            }
            for ( Period period : values( ) )
            {
                if ( period.getCode( ).equals( strCode ) )
                {
                    return period;
                }
            }
            return LAST_MONTH;
        }
    }

    private ActivityService( )
    {
    }

    /**
     * Gets all descendant IDs of an item using bulk loading
     *
     * @param nItemId
     *            The item ID
     * @return List of descendant IDs including the item itself
     */
    public static List<Integer> getAllDescendantIds( int nItemId )
    {
        List<Integer> listIds = new ArrayList<>( );
        listIds.add( nItemId );
        listIds.addAll( WikiItemHome.getDescendants( nItemId ).stream( ).map( AbstractWikiItem::getId ).collect( Collectors.toList( ) ) );
        return listIds;
    }

    /**
     * Calculates the from date based on the period
     *
     * @param period
     *            The period
     * @return The timestamp or null for ALL
     */
    public static Timestamp getFromDate( Period period )
    {
        if ( period == Period.ALL )
        {
            return null;
        }

        Calendar cal = Calendar.getInstance( );

        switch( period )
        {
            case LAST_MONTH:
                cal.add( Calendar.MONTH, -1 );
                break;
            case LAST_6_MONTHS:
                cal.add( Calendar.MONTH, -6 );
                break;
            case LAST_YEAR:
                cal.add( Calendar.YEAR, -1 );
                break;
            default:
                return null;
        }

        return new Timestamp( cal.getTimeInMillis( ) );
    }

    /**
     * Gets activities for a space, filtered by user view rights
     *
     * @param nSpaceId
     *            The space ID
     * @param period
     *            The period filter
     * @param user
     *            The user for rights filtering
     * @return List of activity items
     */
    public static List<ActivityItem> getSpaceActivities( int nSpaceId, Period period, LuteceUser user )
    {
        List<Integer> listEntityIds = getAllDescendantIds( nSpaceId );
        return getActivitiesForEntities( listEntityIds, period, user );
    }

    /**
     * Gets activities for a book, filtered by user view rights
     *
     * @param nBookId
     *            The book ID
     * @param period
     *            The period filter
     * @param user
     *            The user for rights filtering
     * @return List of activity items
     */
    public static List<ActivityItem> getBookActivities( int nBookId, Period period, LuteceUser user )
    {
        List<Integer> listEntityIds = getAllDescendantIds( nBookId );
        return getActivitiesForEntities( listEntityIds, period, user );
    }

    /**
     * Gets activities for a list of entity IDs, filtered by user view rights
     *
     * @param listEntityIds
     *            The entity IDs
     * @param period
     *            The period filter
     * @param user
     *            The user for rights filtering
     * @return List of activity items
     */
    private static List<ActivityItem> getActivitiesForEntities( List<Integer> listEntityIds, Period period, LuteceUser user )
    {
        Timestamp fromDate = getFromDate( period );
        List<Revision> revisions = RevisionHome.getRevisionsByEntityIdsAndPeriod( listEntityIds, fromDate );

        Map<Integer, AbstractWikiItem> itemCache = new HashMap<>( );

        List<int [ ]> previousRevisionPairs = new ArrayList<>( );
        for ( Revision revision : revisions )
        {
            if ( revision.getRevisionNumber( ) > 1 )
            {
                previousRevisionPairs.add( new int [ ] {
                        revision.getEntityId( ), revision.getRevisionNumber( ) - 1
                } );
            }
        }

        Map<String, Revision> previousRevisions = RevisionHome.getBatchByEntityAndRevisionNumber( previousRevisionPairs );

        List<ActivityItem> activities = new ArrayList<>( );
        for ( Revision revision : revisions )
        {
            AbstractWikiItem item = itemCache.get( revision.getEntityId( ) );
            if ( item == null )
            {
                item = WikiItemService.findById( revision.getEntityId( ) );
                if ( item != null )
                {
                    itemCache.put( revision.getEntityId( ), item );
                }
            }

            if ( item != null && WikiAccessControlService.canView( user, item ) )
            {
                boolean bIsCreation = revision.getRevisionNumber( ) == 1;
                ActivityItem activityItem = new ActivityItem( revision, item, bIsCreation );

                String strCurrentContent = revision.getContent( ) != null ? revision.getContent( ) : "";
                String strPreviousContent = "";

                if ( !bIsCreation )
                {
                    String key = revision.getEntityId( ) + "_" + ( revision.getRevisionNumber( ) - 1 );
                    Revision previousRevision = previousRevisions.get( key );
                    if ( previousRevision != null )
                    {
                        strPreviousContent = previousRevision.getContent( ) != null ? previousRevision.getContent( ) : "";
                    }
                }

                int [ ] diffStats = computeDiffStats( strPreviousContent, strCurrentContent );
                activityItem.setLinesAdded( diffStats [0] );
                activityItem.setLinesRemoved( diffStats [1] );
                activityItem.setPreviousContent( strPreviousContent );

                activities.add( activityItem );
            }
        }

        return activities;
    }

    /**
     * Computes diff statistics (lines added, lines removed) between two contents
     *
     * @param strOldContent
     *            The old content
     * @param strNewContent
     *            The new content
     * @return Array with [linesAdded, linesRemoved]
     */
    private static int [ ] computeDiffStats( String strOldContent, String strNewContent )
    {
        String [ ] oldLines = strOldContent.isEmpty( ) ? new String [ 0] : strOldContent.split( "\n", -1 );
        String [ ] newLines = strNewContent.isEmpty( ) ? new String [ 0] : strNewContent.split( "\n", -1 );

        Set<String> oldSet = new HashSet<>( Arrays.asList( oldLines ) );
        Set<String> newSet = new HashSet<>( Arrays.asList( newLines ) );

        int nLinesAdded = 0;
        int nLinesRemoved = 0;

        for ( String line : newLines )
        {
            if ( !oldSet.contains( line ) )
            {
                nLinesAdded++;
            }
        }

        for ( String line : oldLines )
        {
            if ( !newSet.contains( line ) )
            {
                nLinesRemoved++;
            }
        }

        return new int [ ] {
                nLinesAdded, nLinesRemoved
        };
    }
}
