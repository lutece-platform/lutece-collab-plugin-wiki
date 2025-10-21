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
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * IRevisionDAO Interface
 */
public interface IRevisionDAO
{
    /**
     * Insert a new record in the table.
     *
     * @param revision
     *            instance of the Revision object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( Revision revision, Plugin plugin );

    /**
     * Load the data from the table
     *
     * @param nKey
     *            The identifier of the revision
     * @param plugin
     *            the Plugin
     * @return Optional containing the revision if found, empty otherwise
     */
    Optional<Revision> load( int nKey, Plugin plugin );

    /**
     * Load the current revision for an entity
     *
     * @param nEntityId
     *            The entity id
     * @param plugin
     *            the Plugin
     * @return The current revision
     */
    Revision loadCurrentRevision( int nEntityId, Plugin plugin );

    /**
     * Load all revisions for an entity ordered by revision number descending
     *
     * @param nEntityId
     *            The entity id
     * @param plugin
     *            the Plugin
     * @return The list of revisions (revision history)
     */
    List<Revision> selectRevisionsByEntity( int nEntityId, Plugin plugin );

    /**
     * Get the next revision number for an entity
     *
     * @param nEntityId
     *            The entity id
     * @param plugin
     *            the Plugin
     * @return The next revision number
     */
    int getNextRevisionNumber( int nEntityId, Plugin plugin );

    /**
     * Set all revisions as not current for an entity
     *
     * @param nEntityId
     *            The entity id
     * @param plugin
     *            the Plugin
     */
    void setAllNotCurrent( int nEntityId, Plugin plugin );

    /**
     * Load revisions for multiple entities with optional date filter
     *
     * @param listEntityIds
     *            The list of entity ids
     * @param fromDate
     *            The minimum date (null for no filter)
     * @param plugin
     *            the Plugin
     * @return The list of revisions ordered by date descending
     */
    List<Revision> selectRevisionsByEntityIdsAndPeriod( List<Integer> listEntityIds, Timestamp fromDate, Plugin plugin );

    /**
     * Batch load revisions by entity id and revision number pairs
     *
     * @param listPairs
     *            List of int arrays [entityId, revisionNumber]
     * @param plugin
     *            the Plugin
     * @return Map with key "entityId_revisionNumber" and value Revision
     */
    Map<String, Revision> loadBatchByEntityAndRevisionNumber( List<int [ ]> listPairs, Plugin plugin );
}
