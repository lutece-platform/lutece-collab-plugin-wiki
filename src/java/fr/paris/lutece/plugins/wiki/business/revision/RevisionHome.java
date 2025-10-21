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

import fr.paris.lutece.plugins.wiki.service.cache.WikiCacheService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * This class provides instances management methods (create, find, ...) for Revision objects
 */
public final class RevisionHome
{
    private static final IRevisionDAO _dao = SpringContextService.getBean( "wiki.revisionDAO" );
    private static final WikiCacheService _cacheService = WikiCacheService.getInstance( );
    private static final Plugin _plugin = PluginService.getPlugin( "wiki" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private RevisionHome( )
    {
    }

    /**
     * Create an instance of the revision class
     * 
     * @param revision
     *            The instance of the Revision which contains the informations to store
     * @return The instance of revision which has been created with its primary key.
     */
    public static Revision create( Revision revision )
    {
        int nRevisionNumber = _dao.getNextRevisionNumber( revision.getEntityId( ), _plugin );
        revision.setRevisionNumber( nRevisionNumber );

        if ( revision.getIsCurrent( ) )
        {
            _dao.setAllNotCurrent( revision.getEntityId( ), _plugin );
        }

        _dao.insert( revision, _plugin );
        _cacheService.putInCache( _cacheService.getRevisionCacheKey( revision.getId( ) ), revision );
        _cacheService.removeKey( _cacheService.getCurrentRevisionCacheKey( revision.getEntityId( ) ) );
        return revision;
    }

    /**
     * Returns an instance of a revision whose identifier is specified in parameter
     *
     * @param nKey
     *            The revision primary key
     * @return an instance of Revision
     */
    public static Revision findByPrimaryKey( int nKey )
    {
        String cacheKey = _cacheService.getRevisionCacheKey( nKey );
        Revision revision = (Revision) _cacheService.getFromCache( cacheKey );

        if ( revision == null )
        {
            revision = _dao.load( nKey, _plugin ).orElse( null );
            if ( revision != null )
            {
                _cacheService.putInCache( cacheKey, revision );
                revision = new Revision( revision );
            }
        }
        else
        {
            revision = new Revision( revision );
        }

        return revision;
    }

    /**
     * Load the current revision for an entity
     *
     * @param nEntityId
     *            The entity id
     * @return The current revision
     */
    public static Revision getCurrentRevision( int nEntityId )
    {
        String cacheKey = _cacheService.getCurrentRevisionCacheKey( nEntityId );
        Revision revision = (Revision) _cacheService.getFromCache( cacheKey );

        if ( revision == null )
        {
            revision = _dao.loadCurrentRevision( nEntityId, _plugin );
            if ( revision != null )
            {
                _cacheService.putInCache( cacheKey, revision );
                _cacheService.putInCache( _cacheService.getRevisionCacheKey( revision.getId( ) ), revision );
                revision = new Revision( revision );
            }
        }
        else
        {
            revision = new Revision( revision );
        }

        return revision;
    }

    /**
     * Get revision history for an entity
     *
     * @param nEntityId
     *            The entity id
     * @return List of revisions ordered by revision number desc
     */
    public static List<Revision> getRevisionHistory( int nEntityId )
    {
        return _dao.selectRevisionsByEntity( nEntityId, _plugin );
    }

    /**
     * Get revisions for multiple entities with optional date filter
     *
     * @param listEntityIds
     *            The list of entity ids
     * @param fromDate
     *            The minimum date (null for no filter)
     * @return List of revisions ordered by date descending
     */
    public static List<Revision> getRevisionsByEntityIdsAndPeriod( List<Integer> listEntityIds, Timestamp fromDate )
    {
        return _dao.selectRevisionsByEntityIdsAndPeriod( listEntityIds, fromDate, _plugin );
    }

    /**
     * Batch load revisions by entity id and revision number pairs
     *
     * @param listPairs
     *            List of int arrays [entityId, revisionNumber]
     * @return Map with key "entityId_revisionNumber" and value Revision
     */
    public static Map<String, Revision> getBatchByEntityAndRevisionNumber( List<int [ ]> listPairs )
    {
        return _dao.loadBatchByEntityAndRevisionNumber( listPairs, _plugin );
    }

}
