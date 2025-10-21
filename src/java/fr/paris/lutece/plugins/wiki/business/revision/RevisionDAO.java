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
package fr.paris.lutece.plugins.wiki.business.revision;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class provides Data Access methods for Revision objects
 */
public final class RevisionDAO implements IRevisionDAO
{
    private static final String SQL_QUERY_SELECT = "SELECT id_revision, entity_id, revision_number, title, description, content, comment, author, date_creation, is_current FROM wiki_revision WHERE id_revision = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO wiki_revision ( entity_id, revision_number, title, description, content, comment, author, date_creation, is_current ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_SELECT_CURRENT = "SELECT id_revision, entity_id, revision_number, title, description, content, comment, author, date_creation, is_current FROM wiki_revision WHERE entity_id = ? AND is_current = TRUE";
    private static final String SQL_QUERY_SELECT_BY_ENTITY = "SELECT id_revision, entity_id, revision_number, title, description, content, comment, author, date_creation, is_current FROM wiki_revision WHERE entity_id = ? ORDER BY revision_number DESC";
    private static final String SQL_QUERY_MAX_REVISION_NUMBER = "SELECT MAX(revision_number) FROM wiki_revision WHERE entity_id = ?";
    private static final String SQL_QUERY_SET_ALL_NOT_CURRENT = "UPDATE wiki_revision SET is_current = FALSE WHERE entity_id = ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( Revision revision, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, revision.getEntityId( ) );
            daoUtil.setInt( nIndex++, revision.getRevisionNumber( ) );
            daoUtil.setString( nIndex++, revision.getTitle( ) );
            daoUtil.setString( nIndex++, revision.getDescription( ) );
            daoUtil.setString( nIndex++, revision.getContent( ) );
            daoUtil.setString( nIndex++, revision.getComment( ) );
            daoUtil.setString( nIndex++, revision.getAuthor( ) );
            daoUtil.setTimestamp( nIndex++, new Timestamp( Calendar.getInstance( ).getTimeInMillis( ) ) );
            daoUtil.setBoolean( nIndex++, revision.getIsCurrent( ) );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                revision.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Optional<Revision> load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return Optional.of( dataToObject( daoUtil ) );
            }
        }
        return Optional.empty( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Revision loadCurrentRevision( int nEntityId, Plugin plugin )
    {
        Revision revision = null;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_CURRENT, plugin ) )
        {
            daoUtil.setInt( 1, nEntityId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                revision = dataToObject( daoUtil );
            }
        }
        return revision;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Revision> selectRevisionsByEntity( int nEntityId, Plugin plugin )
    {
        List<Revision> revisionList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_ENTITY, plugin ) )
        {
            daoUtil.setInt( 1, nEntityId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                revisionList.add( dataToObject( daoUtil ) );
            }
        }
        return revisionList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getNextRevisionNumber( int nEntityId, Plugin plugin )
    {
        int nMaxRevisionNumber = 0;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_MAX_REVISION_NUMBER, plugin ) )
        {
            daoUtil.setInt( 1, nEntityId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                nMaxRevisionNumber = daoUtil.getInt( 1 );
            }
        }
        return nMaxRevisionNumber + 1;
    }

    /**
     * Creates Revision object from data
     * 
     * @param daoUtil
     *            the DAOUtil
     * @return Revision object
     */
    private Revision dataToObject( DAOUtil daoUtil )
    {
        Revision revision = new Revision( );
        int nIndex = 1;
        revision.setId( daoUtil.getInt( nIndex++ ) );
        revision.setEntityId( daoUtil.getInt( nIndex++ ) );
        revision.setRevisionNumber( daoUtil.getInt( nIndex++ ) );
        revision.setTitle( daoUtil.getString( nIndex++ ) );
        revision.setDescription( daoUtil.getString( nIndex++ ) );
        revision.setContent( daoUtil.getString( nIndex++ ) );
        revision.setComment( daoUtil.getString( nIndex++ ) );
        revision.setAuthor( daoUtil.getString( nIndex++ ) );
        revision.setDateCreation( daoUtil.getTimestamp( nIndex++ ) );
        revision.setIsCurrent( daoUtil.getBoolean( nIndex ) );

        return revision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllNotCurrent( int nEntityId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SET_ALL_NOT_CURRENT, plugin ) )
        {
            daoUtil.setInt( 1, nEntityId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Revision> selectRevisionsByEntityIdsAndPeriod( List<Integer> listEntityIds, Timestamp fromDate, Plugin plugin )
    {
        List<Revision> revisionList = new ArrayList<>( );

        if ( listEntityIds == null || listEntityIds.isEmpty( ) )
        {
            return revisionList;
        }

        StringBuilder sbSQL = new StringBuilder( );
        sbSQL.append( "SELECT id_revision, entity_id, revision_number, title, description, content, comment, author, date_creation, is_current " );
        sbSQL.append( "FROM wiki_revision WHERE entity_id IN (" );
        sbSQL.append( listEntityIds.stream( ).map( String::valueOf ).collect( java.util.stream.Collectors.joining( "," ) ) );
        sbSQL.append( ")" );

        if ( fromDate != null )
        {
            sbSQL.append( " AND date_creation >= ?" );
        }

        sbSQL.append( " ORDER BY date_creation DESC" );

        try ( DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin ) )
        {
            if ( fromDate != null )
            {
                daoUtil.setTimestamp( 1, fromDate );
            }
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                revisionList.add( dataToObject( daoUtil ) );
            }
        }

        return revisionList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Revision> loadBatchByEntityAndRevisionNumber( List<int [ ]> listPairs, Plugin plugin )
    {
        Map<String, Revision> mapResult = new HashMap<>( );

        if ( listPairs == null || listPairs.isEmpty( ) )
        {
            return mapResult;
        }

        StringBuilder sbSQL = new StringBuilder( );
        sbSQL.append( "SELECT id_revision, entity_id, revision_number, title, description, content, comment, author, date_creation, is_current " );
        sbSQL.append( "FROM wiki_revision WHERE " );
        sbSQL.append( listPairs.stream( ).map( pair -> "(entity_id = " + pair [0] + " AND revision_number = " + pair [1] + ")" )
                .collect( Collectors.joining( " OR " ) ) );

        try ( DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Revision revision = dataToObject( daoUtil );
                String key = revision.getEntityId( ) + "_" + revision.getRevisionNumber( );
                mapResult.put( key, revision );
            }
        }

        return mapResult;
    }
}
