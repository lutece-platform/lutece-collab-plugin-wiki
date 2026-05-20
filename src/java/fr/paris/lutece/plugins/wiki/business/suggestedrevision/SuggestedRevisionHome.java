/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.suggestedrevision;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import jakarta.enterprise.inject.spi.CDI;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Static facade for SuggestedRevision persistence.
 */
public final class SuggestedRevisionHome
{
    private static final ISuggestedRevisionDAO _dao = CDI.current( ).select( ISuggestedRevisionDAO.class ).get( );
    private static final Plugin _plugin = PluginService.getPlugin( "wiki" );

    /**
     * Private constructor.
     */
    private SuggestedRevisionHome( )
    {
    }

    /**
     * Persists a new suggestion.
     *
     * @param suggestion
     *            the suggestion to persist
     * @return the persisted suggestion (with its generated id)
     */
    public static SuggestedRevision create( SuggestedRevision suggestion )
    {
        _dao.insert( suggestion, _plugin );
        return suggestion;
    }

    /**
     * Updates the status, reviewer and review comment of an existing suggestion.
     *
     * @param suggestion
     *            the suggestion to update
     * @return the updated suggestion
     */
    public static SuggestedRevision updateStatus( SuggestedRevision suggestion )
    {
        _dao.updateStatus( suggestion, _plugin );
        return suggestion;
    }

    /**
     * Removes a suggestion by primary key.
     *
     * @param nKey
     *            the suggestion id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Loads a suggestion by its primary key.
     *
     * @param nKey
     *            the suggestion id
     * @return the suggestion or null
     */
    public static SuggestedRevision findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin ).orElse( null );
    }

    /**
     * Counts the pending suggestions on a single entity.
     *
     * @param nEntityId
     *            the entity id
     * @return the count
     */
    public static int countPendingByEntity( int nEntityId )
    {
        return _dao.countPendingByEntity( nEntityId, _plugin );
    }

    /**
     * Counts pending suggestions for a batch of entities in a single query.
     *
     * @param entityIds
     *            the entity ids
     * @return a map of entity id to pending count (entities with no pending suggestion are absent)
     */
    public static Map<Integer, Integer> countPendingByEntities( List<Integer> entityIds )
    {
        return _dao.countPendingByEntities( entityIds, _plugin );
    }

    /**
     * Returns all pending suggestions across the wiki.
     *
     * @return every pending suggestion
     */
    public static List<SuggestedRevision> getAllPending( )
    {
        return _dao.selectAllPending( _plugin );
    }

    /**
     * Returns the suggestions submitted by the given author.
     *
     * @param strAuthorGuid
     *            the proposer's user guid
     * @return the suggestions submitted by this author
     */
    public static List<SuggestedRevision> getByAuthor( String strAuthorGuid )
    {
        return _dao.selectByAuthor( strAuthorGuid, _plugin );
    }

    /**
     * Returns the pending suggestion from a given author on a given entity, if any.
     *
     * @param nEntityId
     *            the wiki item id
     * @param strAuthorGuid
     *            the proposer's user guid
     * @return the pending suggestion if exists
     */
    public static Optional<SuggestedRevision> getPendingByEntityAndAuthor( int nEntityId, String strAuthorGuid )
    {
        return _dao.selectPendingByEntityAndAuthor( nEntityId, strAuthorGuid, _plugin );
    }
}
