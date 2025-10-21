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
package fr.paris.lutece.plugins.wiki.business.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.paris.lutece.plugins.wiki.service.cache.WikiCacheService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

public final class WikiItemHome
{
    private static final IWikiItemDAO _dao = SpringContextService.getBean( "wiki.wikiItemDAO" );
    private static final WikiCacheService _cacheService = WikiCacheService.getInstance( );
    private static final Plugin _plugin = PluginService.getPlugin( "wiki" );

    private WikiItemHome( )
    {
    }

    /**
     * Create a new wiki item
     *
     * @param wikiItem
     *            the wiki item to create
     * @return the created wiki item
     */
    public static AbstractWikiItem create( AbstractWikiItem wikiItem )
    {
        _dao.insert( wikiItem, _plugin );

        String cacheKeyById = _cacheService.getWikiItemCacheKey( wikiItem.getId( ) );
        _cacheService.putInCache( cacheKeyById, wikiItem );

        invalidateListCaches( wikiItem );

        return wikiItem;
    }

    /**
     * Update an existing wiki item
     *
     * @param wikiItem
     *            the wiki item to update
     * @return the updated wiki item
     */
    public static AbstractWikiItem update( AbstractWikiItem wikiItem )
    {
        Optional<AbstractWikiItem> optOldItem = _dao.load( wikiItem.getId( ), _plugin );
        String strOldCode = optOldItem.map( AbstractWikiItem::getCode ).orElse( null );
        Integer nOldParentId = optOldItem.map( AbstractWikiItem::getIdParent ).orElse( null );

        _dao.store( wikiItem, _plugin );

        String cacheKeyById = _cacheService.getWikiItemCacheKey( wikiItem.getId( ) );
        _cacheService.removeKey( cacheKeyById );

        if ( strOldCode != null )
        {
            _cacheService.removeKey( _cacheService.getWikiItemByCodeCacheKey( strOldCode ) );
        }
        if ( !wikiItem.getCode( ).equals( strOldCode ) )
        {
            _cacheService.removeKey( _cacheService.getWikiItemByCodeCacheKey( wikiItem.getCode( ) ) );
        }

        if ( nOldParentId != null && nOldParentId != 0 && !nOldParentId.equals( wikiItem.getIdParent( ) ) )
        {
            _cacheService.removeKey( _cacheService.getWikiItemListByParentCacheKey( nOldParentId ) );
        }

        invalidateListCaches( wikiItem );

        return wikiItem;
    }

    /**
     * Remove a wiki item by its key
     *
     * @param nKey
     *            the wiki item key
     */
    public static void remove( int nKey )
    {
        Optional<AbstractWikiItem> optItem = findByPrimaryKey( nKey );

        _dao.delete( nKey, _plugin );

        if ( optItem.isPresent( ) )
        {
            AbstractWikiItem item = optItem.get( );
            String cacheKeyById = _cacheService.getWikiItemCacheKey( nKey );
            String cacheKeyByCode = _cacheService.getWikiItemByCodeCacheKey( item.getCode( ) );

            _cacheService.removeKey( cacheKeyById );
            _cacheService.removeKey( cacheKeyByCode );

            invalidateListCaches( item );
        }
    }

    /**
     * Find a wiki item by its primary key
     *
     * @param nKey
     *            the wiki item key
     * @return an Optional containing the wiki item if found, empty otherwise
     */
    public static Optional<AbstractWikiItem> findByPrimaryKey( int nKey )
    {
        String cacheKey = _cacheService.getWikiItemCacheKey( nKey );
        AbstractWikiItem cachedItem = (AbstractWikiItem) _cacheService.getFromCache( cacheKey );

        if ( cachedItem != null )
        {
            return Optional.of( cachedItem.clone( ) );
        }

        Optional<AbstractWikiItem> optItem = _dao.load( nKey, _plugin );

        if ( optItem.isPresent( ) )
        {
            AbstractWikiItem item = optItem.get( );
            _cacheService.putInCache( cacheKey, item );
            return Optional.of( item.clone( ) );
        }

        return Optional.empty( );
    }

    /**
     * Get all wiki items by type
     *
     * @param type
     *            the wiki item type
     * @return the list of wiki items matching the type
     */
    @SuppressWarnings( "unchecked" )
    public static List<AbstractWikiItem> getWikiItemsByType( WikiItemType type )
    {
        String cacheKey = _cacheService.getWikiItemListByTypeCacheKey( type );
        List<AbstractWikiItem> listItems = (List<AbstractWikiItem>) _cacheService.getFromCache( cacheKey );

        if ( listItems == null )
        {
            listItems = _dao.selectWikiItemsByType( type, _plugin );
            _cacheService.putInCache( cacheKey, listItems );
        }

        return listItems.stream( ).map( AbstractWikiItem::clone ).collect( Collectors.toList( ) );
    }

    /**
     * Get all wiki items by parent id
     *
     * @param nIdParent
     *            the parent id
     * @return the list of wiki items having the specified parent
     */
    @SuppressWarnings( "unchecked" )
    public static List<AbstractWikiItem> getWikiItemsByParent( int nIdParent )
    {
        String cacheKey = _cacheService.getWikiItemListByParentCacheKey( nIdParent );
        List<AbstractWikiItem> listItems = (List<AbstractWikiItem>) _cacheService.getFromCache( cacheKey );

        if ( listItems == null )
        {
            listItems = _dao.selectWikiItemsByParent( nIdParent, _plugin );
            _cacheService.putInCache( cacheKey, listItems );
        }

        return listItems.stream( ).map( AbstractWikiItem::clone ).collect( Collectors.toList( ) );
    }

    /**
     * Get all wiki items by parent id and type
     * 
     * @param nIdParent
     *            the parent id
     * @param type
     *            the wiki item type
     * @return the list of wiki items matching the parent and type
     */
    public static List<AbstractWikiItem> getWikiItemsByParentAndType( int nIdParent, WikiItemType type )
    {
        return getWikiItemsByParent( nIdParent ).stream( ).filter( item -> item.getType( ) == type ).collect( Collectors.toList( ) );
    }

    /**
     * Find a wiki item by its code
     * 
     * @param strCode
     *            the wiki item code
     * @return an Optional containing the wiki item if found, empty otherwise
     */
    public static Optional<AbstractWikiItem> findByCode( String strCode )
    {
        String cacheKey = _cacheService.getWikiItemByCodeCacheKey( strCode );
        AbstractWikiItem cachedItem = (AbstractWikiItem) _cacheService.getFromCache( cacheKey );

        if ( cachedItem != null )
        {
            return Optional.of( cachedItem.clone( ) );
        }

        Optional<AbstractWikiItem> optItem = _dao.selectByCode( strCode, _plugin );

        if ( optItem.isPresent( ) )
        {
            AbstractWikiItem item = optItem.get( );
            _cacheService.putInCache( cacheKey, item );
            return Optional.of( item.clone( ) );
        }

        return Optional.empty( );
    }

    /**
     * Invalidate list caches related to the given wiki item
     * 
     * @param item
     *            the wiki item
     */
    private static void invalidateListCaches( AbstractWikiItem item )
    {
        String cacheKeyByType = _cacheService.getWikiItemListByTypeCacheKey( item.getType( ) );
        _cacheService.removeKey( cacheKeyByType );

        if ( item.getIdParent( ) != null && item.getIdParent( ) != 0 )
        {
            String cacheKeyByParent = _cacheService.getWikiItemListByParentCacheKey( item.getIdParent( ) );
            _cacheService.removeKey( cacheKeyByParent );
        }
    }
}
