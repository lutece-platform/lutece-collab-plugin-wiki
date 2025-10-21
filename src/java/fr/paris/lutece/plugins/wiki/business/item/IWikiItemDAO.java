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

import fr.paris.lutece.portal.service.plugin.Plugin;

public interface IWikiItemDAO
{
    /**
     * Insert a new wiki item
     * 
     * @param wikiItem
     *            the wiki item to insert
     * @param plugin
     *            the plugin
     */
    void insert( AbstractWikiItem wikiItem, Plugin plugin );

    /**
     * Update an existing wiki item
     * 
     * @param wikiItem
     *            the wiki item to update
     * @param plugin
     *            the plugin
     */
    void store( AbstractWikiItem wikiItem, Plugin plugin );

    /**
     * Delete a wiki item by its key
     * 
     * @param nKey
     *            the wiki item key
     * @param plugin
     *            the plugin
     */
    void delete( int nKey, Plugin plugin );

    /**
     * Load a wiki item by its key
     * 
     * @param nKey
     *            the wiki item key
     * @param plugin
     *            the plugin
     * @return an Optional containing the wiki item if found, empty otherwise
     */
    Optional<AbstractWikiItem> load( int nKey, Plugin plugin );

    /**
     * Select all wiki items by type
     * 
     * @param type
     *            the wiki item type
     * @param plugin
     *            the plugin
     * @return the list of wiki items matching the type
     */
    List<AbstractWikiItem> selectWikiItemsByType( WikiItemType type, Plugin plugin );

    /**
     * Select all wiki items by parent id
     * 
     * @param nIdParent
     *            the parent id
     * @param plugin
     *            the plugin
     * @return the list of wiki items having the specified parent
     */
    List<AbstractWikiItem> selectWikiItemsByParent( int nIdParent, Plugin plugin );

    /**
     * Select all wiki items by parent id and type
     * 
     * @param nIdParent
     *            the parent id
     * @param type
     *            the wiki item type
     * @param plugin
     *            the plugin
     * @return the list of wiki items matching the parent and type
     */
    List<AbstractWikiItem> selectWikiItemsByParentAndType( int nIdParent, WikiItemType type, Plugin plugin );

    /**
     * Select a wiki item by its code
     *
     * @param strCode
     *            the wiki item code
     * @param plugin
     *            the plugin
     * @return an Optional containing the wiki item if found, empty otherwise
     */
    Optional<AbstractWikiItem> selectByCode( String strCode, Plugin plugin );
}
