/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.suggestedrevision;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Data Access methods for SuggestedRevision objects.
 */
public final class SuggestedRevisionDAO implements ISuggestedRevisionDAO
{
    private static final String COLUMNS = "id_suggestion, entity_id, title, description, content, comment, author, author_guid, status, review_comment, reviewer, date_creation, date_review";

    private static final String SQL_QUERY_INSERT = "INSERT INTO wiki_suggested_revision ( entity_id, title, description, content, comment, author, author_guid, status, date_creation ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_SELECT = "SELECT " + COLUMNS + " FROM wiki_suggested_revision WHERE id_suggestion = ?";
    private static final String SQL_QUERY_UPDATE_STATUS = "UPDATE wiki_suggested_revision SET status = ?, review_comment = ?, reviewer = ?, date_review = ? WHERE id_suggestion = ?";
    private static final String SQL_QUERY_COUNT_PENDING_BY_ENTITY = "SELECT COUNT(*) FROM wiki_suggested_revision WHERE entity_id = ? AND status = 'PENDING'";
    private static final String SQL_QUERY_SELECT_ALL_PENDING = "SELECT " + COLUMNS + " FROM wiki_suggested_revision WHERE status = 'PENDING' ORDER BY date_creation DESC";
    private static final String SQL_QUERY_SELECT_BY_AUTHOR = "SELECT " + COLUMNS + " FROM wiki_suggested_revision WHERE author_guid = ? ORDER BY date_creation DESC";
    private static final String SQL_QUERY_SELECT_PENDING_BY_ENTITY_AND_AUTHOR = "SELECT " + COLUMNS + " FROM wiki_suggested_revision WHERE entity_id = ? AND author_guid = ? AND status = 'PENDING'";

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( SuggestedRevision suggestion, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, suggestion.getEntityId( ) );
            daoUtil.setString( nIndex++, suggestion.getTitle( ) );
            daoUtil.setString( nIndex++, suggestion.getDescription( ) );
            daoUtil.setString( nIndex++, suggestion.getContent( ) );
            daoUtil.setString( nIndex++, suggestion.getComment( ) );
            daoUtil.setString( nIndex++, suggestion.getAuthor( ) );
            daoUtil.setString( nIndex++, suggestion.getAuthorGuid( ) );
            daoUtil.setString( nIndex++, suggestion.getStatus( ).name( ) );
            daoUtil.setTimestamp( nIndex++, new Timestamp( Calendar.getInstance( ).getTimeInMillis( ) ) );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                suggestion.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus( SuggestedRevision suggestion, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_STATUS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, suggestion.getStatus( ).name( ) );
            daoUtil.setString( nIndex++, suggestion.getReviewComment( ) );
            daoUtil.setString( nIndex++, suggestion.getReviewer( ) );
            daoUtil.setTimestamp( nIndex++, new Timestamp( Calendar.getInstance( ).getTimeInMillis( ) ) );
            daoUtil.setInt( nIndex++, suggestion.getId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SuggestedRevision> load( int nKey, Plugin plugin )
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
     * {@inheritDoc}
     */
    @Override
    public List<SuggestedRevision> selectAllPending( Plugin plugin )
    {
        List<SuggestedRevision> list = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_PENDING, plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                list.add( dataToObject( daoUtil ) );
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SuggestedRevision> selectByAuthor( String strAuthorGuid, Plugin plugin )
    {
        List<SuggestedRevision> list = new ArrayList<>( );
        if ( strAuthorGuid == null || strAuthorGuid.isEmpty( ) )
        {
            return list;
        }
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_AUTHOR, plugin ) )
        {
            daoUtil.setString( 1, strAuthorGuid );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                list.add( dataToObject( daoUtil ) );
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SuggestedRevision> selectPendingByEntityAndAuthor( int nEntityId, String strAuthorGuid, Plugin plugin )
    {
        if ( strAuthorGuid == null || strAuthorGuid.isEmpty( ) )
        {
            return Optional.empty( );
        }
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_PENDING_BY_ENTITY_AND_AUTHOR, plugin ) )
        {
            daoUtil.setInt( 1, nEntityId );
            daoUtil.setString( 2, strAuthorGuid );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return Optional.of( dataToObject( daoUtil ) );
            }
        }
        return Optional.empty( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countPendingByEntity( int nEntityId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_COUNT_PENDING_BY_ENTITY, plugin ) )
        {
            daoUtil.setInt( 1, nEntityId );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
        }
        return 0;
    }

    /**
     * Builds a SuggestedRevision from the current row of the DAOUtil.
     *
     * @param daoUtil
     *            the DAOUtil positioned on a row
     * @return the SuggestedRevision instance
     */
    private SuggestedRevision dataToObject( DAOUtil daoUtil )
    {
        SuggestedRevision s = new SuggestedRevision( );
        int nIndex = 1;
        s.setId( daoUtil.getInt( nIndex++ ) );
        s.setEntityId( daoUtil.getInt( nIndex++ ) );
        s.setTitle( daoUtil.getString( nIndex++ ) );
        s.setDescription( daoUtil.getString( nIndex++ ) );
        s.setContent( daoUtil.getString( nIndex++ ) );
        s.setComment( daoUtil.getString( nIndex++ ) );
        s.setAuthor( daoUtil.getString( nIndex++ ) );
        s.setAuthorGuid( daoUtil.getString( nIndex++ ) );
        s.setStatus( SuggestionStatus.fromString( daoUtil.getString( nIndex++ ) ) );
        s.setReviewComment( daoUtil.getString( nIndex++ ) );
        s.setReviewer( daoUtil.getString( nIndex++ ) );
        s.setDateCreation( daoUtil.getTimestamp( nIndex++ ) );
        s.setDateReview( daoUtil.getTimestamp( nIndex++ ) );
        return s;
    }
}
