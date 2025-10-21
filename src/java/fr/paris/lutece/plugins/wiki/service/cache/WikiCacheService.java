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
package fr.paris.lutece.plugins.wiki.service.cache;

import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;

public class WikiCacheService extends AbstractCacheableService
{
    private static final String CACHE_NAME = "wikiCacheService";

    private static final String KEY_SEPARATOR = ":";
    private static final String KEY_PREFIX = "wiki" + KEY_SEPARATOR;

    private static final String OP_LIST = "list";
    private static final String OP_BY_CODE = "by_code";
    private static final String OP_BY_PARENT = "by_parent";
    private static final String OP_CURRENT = "current";
    private static final String ENTITY_REVISION = "revision";

    private WikiCacheService( )
    {
    }

    private static class SingletonHolder
    {
        static final WikiCacheService INSTANCE = new WikiCacheService( );
    }

    /**
     * Gets the singleton instance of WikiCacheService
     * 
     * @return the singleton instance
     */
    public static WikiCacheService getInstance( )
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return CACHE_NAME;
    }

    /**
     * Builds a cache key from the given components
     *
     * @param components
     *            the components of the key
     * @return the cache key
     */
    private String buildKey( String... components )
    {
        StringBuilder keyBuilder = new StringBuilder( KEY_PREFIX );
        for ( int i = 0; i < components.length; i++ )
        {
            if ( i > 0 )
            {
                keyBuilder.append( KEY_SEPARATOR );
            }
            keyBuilder.append( components [i] );
        }
        return keyBuilder.toString( );
    }

    /**
     * Gets the cache key for a wiki item by ID
     *
     * @param nId
     *            the item ID
     * @return the cache key
     */
    public String getWikiItemCacheKey( int nId )
    {
        return buildKey( "item", String.valueOf( nId ) );
    }

    /**
     * Gets the cache key for a wiki item by code
     *
     * @param strCode
     *            the item code
     * @return the cache key
     */
    public String getWikiItemByCodeCacheKey( String strCode )
    {
        return buildKey( "item", OP_BY_CODE, strCode );
    }

    /**
     * Gets the cache key for wiki items by type
     *
     * @param type
     *            the wiki item type
     * @return the cache key
     */
    public String getWikiItemListByTypeCacheKey( WikiItemType type )
    {
        return buildKey( "item", OP_LIST, "type", type.name( ) );
    }

    /**
     * Gets the cache key for wiki items by parent
     *
     * @param nIdParent
     *            the parent ID
     * @return the cache key
     */
    public String getWikiItemListByParentCacheKey( int nIdParent )
    {
        return buildKey( "item", OP_LIST, OP_BY_PARENT, String.valueOf( nIdParent ) );
    }

    /**
     * Gets the cache key for a revision
     *
     * @param nIdRevision
     *            the revision ID
     * @return the cache key
     */
    public String getRevisionCacheKey( int nIdRevision )
    {
        return buildKey( ENTITY_REVISION, String.valueOf( nIdRevision ) );
    }

    /**
     * Gets the cache key for the current revision of an entity
     *
     * @param nEntityId
     *            the entity ID
     * @return the cache key
     */
    public String getCurrentRevisionCacheKey( int nEntityId )
    {
        return buildKey( ENTITY_REVISION, OP_CURRENT, String.valueOf( nEntityId ) );
    }
}
