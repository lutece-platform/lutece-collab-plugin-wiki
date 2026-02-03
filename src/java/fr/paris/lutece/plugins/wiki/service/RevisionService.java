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
package fr.paris.lutece.plugins.wiki.service;

import java.util.List;

import jakarta.enterprise.inject.spi.CDI;

import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.revision.RevisionHome;
import fr.paris.lutece.portal.business.event.ResourceEvent;
import fr.paris.lutece.portal.service.event.EventAction;
import fr.paris.lutece.portal.service.event.Type.TypeQualifier;

public final class RevisionService
{

    private static final String COMMENT_RESTORED_FROM_REVISION = "Restored from revision #";

    private RevisionService( )
    {
    }

    /**
     * Finds a revision by its ID
     * 
     * @param revisionId
     *            the revision identifier
     * @return the revision if found, null otherwise
     */
    public static Revision findById( int revisionId )
    {
        return RevisionHome.findByPrimaryKey( revisionId );
    }

    /**
     * Gets the current revision for an entity
     *
     * @param entityId
     *            the entity identifier
     * @return the current revision if exists, null otherwise
     */
    public static Revision getCurrentRevision( int entityId )
    {
        return RevisionHome.getCurrentRevision( entityId );
    }

    /**
     * Gets the revision history for an entity
     *
     * @param entityId
     *            the entity identifier
     * @return list of revisions for the entity
     */
    public static List<Revision> getRevisionHistory( int entityId )
    {
        return RevisionHome.getRevisionHistory( entityId );
    }

    /**
     * Creates a new revision
     *
     * @param revision
     *            the revision to create
     * @return the created revision
     */
    public static Revision create( Revision revision )
    {
        validateAndNormalizeContent( revision );
        Revision createdRevision = RevisionHome.create( revision );
        fireResourceEvent( createdRevision.getId( ), EventAction.CREATE );
        return createdRevision;
    }

    /**
     * Restores a specific revision
     *
     * @param revisionId
     *            the revision identifier to restore
     * @param author
     *            the author performing the restoration
     */
    public static void restoreRevision( int revisionId, String author )
    {
        Revision revisionToRestore = RevisionHome.findByPrimaryKey( revisionId );

        if ( revisionToRestore == null )
        {
            return;
        }

        Revision newRevision = new Revision( );
        newRevision.setEntityId( revisionToRestore.getEntityId( ) );
        newRevision.setTitle( revisionToRestore.getTitle( ) );
        newRevision.setDescription( revisionToRestore.getDescription( ) );
        newRevision.setContent( revisionToRestore.getContent( ) );
        newRevision.setComment( COMMENT_RESTORED_FROM_REVISION + revisionToRestore.getId( ) );
        newRevision.setIsCurrent( true );
        newRevision.setAuthor( author );

        Revision createdRevision = RevisionHome.create( newRevision );

        fireResourceEvent( createdRevision.getId( ), EventAction.CREATE );
    }

    /**
     * Validates and normalizes content for a revision based on the wiki item type If the wiki item does not support content, the content is set to null
     *
     * @param revision
     *            the revision to validate
     */
    private static void validateAndNormalizeContent( Revision revision )
    {
        if ( revision.getEntityId( ) <= 0 )
        {
            return;
        }

        WikiItemHome.findByPrimaryKey( revision.getEntityId( ) ).ifPresent( item -> {
            if ( !item.supportsContent( ) )
            {
                revision.setContent( null );
            }
        } );
    }

    /**
     * Fires a resource event for a revision
     *
     * @param resourceId
     *            the resource identifier
     * @param action
     *            the event action
     */
    private static void fireResourceEvent( int resourceId, EventAction action )
    {
        ResourceEvent event = new ResourceEvent( );
        event.setIdResource( String.valueOf( resourceId ) );
        event.setTypeResource( Revision.RESOURCE_TYPE );
        CDI.current( ).getBeanManager( ).getEvent( )
            .select( ResourceEvent.class, new TypeQualifier( action ) ).fire( event );
    }
}
