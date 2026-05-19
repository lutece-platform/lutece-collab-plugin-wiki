/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.suggestedrevision;

import fr.paris.lutece.portal.service.plugin.Plugin;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access contract for SuggestedRevision objects.
 */
public interface ISuggestedRevisionDAO
{
    /**
     * Inserts a new suggestion.
     *
     * @param suggestion
     *            the suggestion to persist
     * @param plugin
     *            the wiki plugin
     */
    void insert( SuggestedRevision suggestion, Plugin plugin );

    /**
     * Updates the status, reviewer and review comment of a suggestion.
     *
     * @param suggestion
     *            the suggestion to update
     * @param plugin
     *            the wiki plugin
     */
    void updateStatus( SuggestedRevision suggestion, Plugin plugin );

    /**
     * Removes a suggestion by primary key.
     *
     * @param nKey
     *            the suggestion id
     * @param plugin
     *            the wiki plugin
     */
    void delete( int nKey, Plugin plugin );

    /**
     * Loads a suggestion by primary key.
     *
     * @param nKey
     *            the suggestion id
     * @param plugin
     *            the wiki plugin
     * @return the suggestion if found
     */
    Optional<SuggestedRevision> load( int nKey, Plugin plugin );

    /**
     * Returns every pending suggestion, ordered by creation date desc.
     *
     * @param plugin
     *            the wiki plugin
     * @return all pending suggestions
     */
    List<SuggestedRevision> selectAllPending( Plugin plugin );

    /**
     * Returns the suggestions submitted by the given author, ordered by creation date desc.
     *
     * @param strAuthorGuid
     *            the proposer's user guid
     * @param plugin
     *            the wiki plugin
     * @return the suggestions submitted by this author
     */
    List<SuggestedRevision> selectByAuthor( String strAuthorGuid, Plugin plugin );

    /**
     * Returns the pending suggestion from a given author on a given entity, if any.
     *
     * @param nEntityId
     *            the wiki item id
     * @param strAuthorGuid
     *            the proposer's user guid
     * @param plugin
     *            the wiki plugin
     * @return the pending suggestion if exists
     */
    Optional<SuggestedRevision> selectPendingByEntityAndAuthor( int nEntityId, String strAuthorGuid, Plugin plugin );

    /**
     * Counts pending suggestions for a single entity.
     *
     * @param nEntityId
     *            the entity id
     * @param plugin
     *            the wiki plugin
     * @return the number of pending suggestions on this entity
     */
    int countPendingByEntity( int nEntityId, Plugin plugin );

    /**
     * Counts pending suggestions for a batch of entities in a single query. Entities with no
     * pending suggestion are absent from the result.
     *
     * @param entityIds
     *            the entity ids to count for
     * @param plugin
     *            the wiki plugin
     * @return a map of entity id to pending count
     */
    Map<Integer, Integer> countPendingByEntities( List<Integer> entityIds, Plugin plugin );
}
