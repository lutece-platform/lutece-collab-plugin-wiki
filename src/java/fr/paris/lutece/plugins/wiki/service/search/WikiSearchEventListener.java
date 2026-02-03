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
package fr.paris.lutece.plugins.wiki.service.search;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.revision.RevisionHome;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.portal.business.event.ResourceEvent;
import fr.paris.lutece.portal.business.indexeraction.IndexerAction;
import fr.paris.lutece.portal.service.event.EventAction;
import fr.paris.lutece.portal.service.event.Type;
import fr.paris.lutece.portal.service.search.IndexationService;

/**
 * Wiki search event listener for handling resource indexation events
 */
@ApplicationScoped
public class WikiSearchEventListener
{
    private static final String RESOURCE_ID_SEPARATOR = "_";

    WikiSearchEventListener( )
    {
    }

    /**
     * Handles resource creation events
     *
     * @param event
     *            the resource event
     */
    public void addedResource( @Observes @Type( EventAction.CREATE ) ResourceEvent event )
    {
        String strResourceType = event.getTypeResource( );
        if ( Revision.RESOURCE_TYPE.equals( strResourceType ) )
        {
            indexResource( event );
        }
    }

    /**
     * Handles resource deletion events
     *
     * @param event
     *            the resource event
     */
    public void deletedResource( @Observes @Type( EventAction.REMOVE ) ResourceEvent event )
    {
        removeFromIndex( event );
    }

    /**
     * Indexes a resource based on the resource event
     *
     * @param event
     *            the resource event
     */
    private void indexResource( ResourceEvent event )
    {
        String strResourceType = event.getTypeResource( );
        String strIdResource = event.getIdResource( );

        if ( Revision.RESOURCE_TYPE.equals( strResourceType ) )
        {
            indexRevision( strIdResource );
        }
        else
        {
            indexWikiItem( strIdResource );
        }
    }

    /**
     * Indexes a wiki item and its children for all available languages
     *
     * @param strIdResource
     *            the resource ID as string
     */
    private void indexWikiItem( String strIdResource )
    {
        int nId = Integer.parseInt( strIdResource );
        AbstractWikiItem item = WikiItemService.findById( nId );

        if ( item != null && item.isPublished( ) && isHierarchyPublished( item ) )
        {
            Revision currentRevision = RevisionHome.getCurrentRevision( nId );

            if ( currentRevision != null )
            {
                addToIndex( item );
                indexChildren( item );
            }
        }
    }

    /**
     * Checks if the entire hierarchy of a wiki item is published
     *
     * @param item
     *            the wiki item to check
     * @return true if the hierarchy is published, false otherwise
     */
    private boolean isHierarchyPublished( AbstractWikiItem item )
    {
        AbstractWikiItem current = item;

        while ( current.getIdParent( ) != null )
        {
            AbstractWikiItem parent = WikiItemService.findById( current.getIdParent( ) );

            if ( parent == null || !parent.isPublished( ) )
            {
                return false;
            }

            current = parent;
        }

        return true;
    }

    /**
     * Adds a wiki item to the search index
     *
     * @param item
     *            the wiki item to add
     */
    private void addToIndex( AbstractWikiItem item )
    {
        String strId = item.getResourceType( ) + RESOURCE_ID_SEPARATOR + item.getId( );
        IndexationService.addIndexerAction( strId, WikiSearchIndexer.INDEXER_NAME, IndexerAction.TASK_MODIFY );
    }

    /**
     * Indexes all children of a wiki item recursively
     *
     * @param item
     *            the parent wiki item
     */
    private void indexChildren( AbstractWikiItem item )
    {
        List<AbstractWikiItem> children = WikiItemService.getItemsByParent( item.getId( ) );

        for ( AbstractWikiItem child : children )
        {
            if ( child.isPublished( ) )
            {
                Revision currentRevision = RevisionHome.getCurrentRevision( child.getId( ) );

                if ( currentRevision != null )
                {
                    addToIndex( child );
                    indexChildren( child );
                }
            }
        }
    }

    /**
     * Indexes a revision if it is the current revision
     *
     * @param strIdResource
     *            the revision ID as string
     */
    private void indexRevision( String strIdResource )
    {
        int nIdRevision = Integer.parseInt( strIdResource );
        Revision revision = RevisionHome.findByPrimaryKey( nIdRevision );

        if ( revision != null && revision.getIsCurrent( ) )
        {
            AbstractWikiItem item = WikiItemService.findById( revision.getEntityId( ) );

            if ( item != null )
            {
                String strId = item.getResourceType( ) + RESOURCE_ID_SEPARATOR + item.getId( );
                IndexationService.addIndexerAction( strId, WikiSearchIndexer.INDEXER_NAME, IndexerAction.TASK_MODIFY );
            }
        }
    }

    /**
     * Removes a resource from the search index
     *
     * @param event
     *            the resource event
     */
    private void removeFromIndex( ResourceEvent event )
    {
        String strIdResource = event.getIdResource( );
        String strResourceType = event.getTypeResource( );
        removeWikiItem( strIdResource, strResourceType );
    }

    /**
     * Removes a wiki item and its children from the search index for all available languages
     *
     * @param strIdResource
     *            the resource ID as string
     * @param strResourceType
     *            the resource type
     */
    private void removeWikiItem( String strIdResource, String strResourceType )
    {
        int nId = Integer.parseInt( strIdResource );
        String strId = strResourceType + RESOURCE_ID_SEPARATOR + nId;
        IndexationService.addIndexerAction( strId, WikiSearchIndexer.INDEXER_NAME, IndexerAction.TASK_DELETE );

        List<AbstractWikiItem> children = WikiItemService.getItemsByParent( nId );

        for ( AbstractWikiItem child : children )
        {
            removeWikiItem( String.valueOf( child.getId( ) ), child.getResourceType( ) );
        }
    }
}
