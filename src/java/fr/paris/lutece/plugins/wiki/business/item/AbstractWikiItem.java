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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.revision.RevisionHome;
import fr.paris.lutece.plugins.wiki.service.WikiUrlService;
import fr.paris.lutece.portal.service.rbac.RBACResource;

public abstract class AbstractWikiItem implements RBACResource, Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_VALIDATION_CODE_NOT_EMPTY = "#i18n{wiki.validation.wikiitem.Code.notEmpty}";
    private static final String MESSAGE_VALIDATION_CODE_SIZE = "#i18n{wiki.validation.wikiitem.Code.size}";
    private static final String MESSAGE_VALIDATION_TYPE_NOT_EMPTY = "#i18n{wiki.validation.wikiitem.Type.notEmpty}";
    private static final int CODE_MAX_SIZE = 100;

    private int _nId;

    @NotEmpty( message = MESSAGE_VALIDATION_CODE_NOT_EMPTY )
    @Size( max = CODE_MAX_SIZE, message = MESSAGE_VALIDATION_CODE_SIZE )
    private String _strCode;

    private String _strIcon;
    private boolean _bIsPublished;
    private String _strViewRole;
    private String _strEditRole;
    private Integer _nIdParent;
    private int _nOrder;
    private Timestamp _dateCreation;
    private Timestamp _dateModification;
    private transient Revision _currentRevision;
    private transient List<AbstractWikiItem> _listChildren = new ArrayList<>( );

    @NotEmpty( message = MESSAGE_VALIDATION_TYPE_NOT_EMPTY )
    private WikiItemType _type;

    /**
     * Default constructor
     */
    public AbstractWikiItem( )
    {
    }

    /**
     * Copy constructor
     * 
     * @param item
     *            The item to copy
     */
    protected AbstractWikiItem( AbstractWikiItem item )
    {
        this._nId = item._nId;
        this._strCode = item._strCode;
        this._strIcon = item._strIcon;
        this._bIsPublished = item._bIsPublished;
        this._strViewRole = item._strViewRole;
        this._strEditRole = item._strEditRole;
        this._nIdParent = item._nIdParent;
        this._nOrder = item._nOrder;
        this._dateCreation = item._dateCreation;
        this._dateModification = item._dateModification;
        this._type = item._type;
        this._listChildren = new ArrayList<>( item._listChildren );
    }

    /**
     * Get the wiki item id
     * 
     * @return the wiki item id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Set the wiki item id
     * 
     * @param nId
     *            the wiki item id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Get the wiki item code
     * 
     * @return the wiki item code
     */
    public String getCode( )
    {
        return _strCode;
    }

    /**
     * Set the wiki item code
     * 
     * @param strCode
     *            the wiki item code
     */
    public void setCode( String strCode )
    {
        _strCode = strCode;
    }

    public String getIcon( )
    {
        return _strIcon;
    }

    public void setIcon( String strIcon )
    {
        _strIcon = strIcon;
    }

    /**
     * Check if the wiki item is published
     *
     * @return true if published, false otherwise
     */
    public boolean isPublished( )
    {
        return _bIsPublished;
    }

    /**
     * Set the published status
     * 
     * @param bIsPublished
     *            the published status
     */
    public void setIsPublished( boolean bIsPublished )
    {
        _bIsPublished = bIsPublished;
    }

    /**
     * Get the view role
     * 
     * @return the view role
     */
    public String getViewRole( )
    {
        return _strViewRole;
    }

    /**
     * Set the view role
     * 
     * @param strViewRole
     *            the view role
     */
    public void setViewRole( String strViewRole )
    {
        _strViewRole = strViewRole;
    }

    /**
     * Get the edit role
     * 
     * @return the edit role
     */
    public String getEditRole( )
    {
        return _strEditRole;
    }

    /**
     * Set the edit role
     * 
     * @param strEditRole
     *            the edit role
     */
    public void setEditRole( String strEditRole )
    {
        _strEditRole = strEditRole;
    }

    /**
     * Get the wiki item type
     * 
     * @return the wiki item type
     */
    public WikiItemType getType( )
    {
        return _type;
    }

    /**
     * Set the wiki item type
     * 
     * @param type
     *            the wiki item type
     */
    public void setType( WikiItemType type )
    {
        _type = type;
    }

    /**
     * Get the parent id
     * 
     * @return the parent id
     */
    public Integer getIdParent( )
    {
        return _nIdParent;
    }

    /**
     * Set the parent id
     *
     * @param nIdParent
     *            the parent id
     */
    public void setIdParent( Integer nIdParent )
    {
        _nIdParent = nIdParent;
    }

    /**
     * Get the creation date
     *
     * @return the creation date
     */
    public Timestamp getDateCreation( )
    {
        return _dateCreation;
    }

    /**
     * Set the creation date
     * 
     * @param dateCreation
     *            the creation date
     */
    public void setDateCreation( Timestamp dateCreation )
    {
        _dateCreation = dateCreation;
    }

    /**
     * Set the modification date
     *
     * @param dateModification
     *            the modification date
     */
    public void setDateModification( Timestamp dateModification )
    {
        _dateModification = dateModification;
    }

    /**
     * Get the current revision
     * 
     * @return the current revision
     */
    public Revision getCurrentRevision( )
    {
        return _currentRevision;
    }

    /**
     * Set the current revision
     *
     * @param currentRevision
     *            the current revision
     */
    public void setCurrentRevision( Revision currentRevision )
    {
        _currentRevision = currentRevision;
    }

    /**
     * Get the list of children
     *
     * @return the list of children
     */
    public List<AbstractWikiItem> getChildren( )
    {
        return _listChildren;
    }

    /**
     * Set the list of children
     *
     * @param listChildren
     *            the list of children
     */
    public void setChildren( List<AbstractWikiItem> listChildren )
    {
        _listChildren = listChildren;
    }

    /**
     * Get the order
     *
     * @return the order
     */
    public int getOrder( )
    {
        return _nOrder;
    }

    /**
     * Set the order
     *
     * @param order
     *            the order
     */
    public void setOrder( int order )
    {
        _nOrder = order;
    }

    /**
     * Check if the wiki item has an order
     *
     * @return true if has order, false otherwise
     */
    public boolean hasOrder( )
    {
        return true;
    }

    /**
     * Get the parent wiki item
     *
     * @return the parent wiki item or null if no parent
     */
    public AbstractWikiItem getParent( )
    {
        if ( _nIdParent == null )
        {
            return null;
        }
        return WikiItemHome.findByPrimaryKey( _nIdParent ).map( parent -> {
            parent.setCurrentRevision( RevisionHome.getCurrentRevision( parent.getId( ) ) );
            return parent;
        } ).orElse( null );
    }

    /**
     * Get the full breadcrumb path of this item (e.g. "Space > Category > Book > Chapter")
     *
     * @return the breadcrumb path
     */
    public String getBreadcrumb( )
    {
        List<String> parts = new ArrayList<>( );
        AbstractWikiItem current = this;

        while ( current != null )
        {
            Revision revision = current.getCurrentRevision( );
            if ( revision == null )
            {
                revision = RevisionHome.getCurrentRevision( current.getId( ) );
            }

            String title = ( revision != null && revision.getTitle( ) != null && !revision.getTitle( ).isEmpty( ) ) ? revision.getTitle( ) : current.getCode( );
            parts.add( 0, title );
            current = current.getParent( );
        }

        return String.join( " > ", parts );
    }

    /**
     * Get the resource type code for RBAC
     *
     * @return the resource type code
     */
    @Override
    public String getResourceTypeCode( )
    {
        return getResourceType( );
    }

    /**
     * Get the resource id for RBAC
     *
     * @return the resource id
     */
    @Override
    public String getResourceId( )
    {
        return String.valueOf( _nId );
    }

    /**
     * Get the resource type
     *
     * @return the resource type
     */
    public abstract String getResourceType( );

    /**
     * Get the child item type
     *
     * @return the child item type or null if no children
     */
    public WikiItemType getChildType( )
    {
        return null;
    }

    /**
     * Get all allowed child item types
     *
     * @return the set of allowed child item types, empty set if no children allowed
     */
    public abstract Set<WikiItemType> getAllowedChildTypes( );

    /**
     * Get all allowed parent item types
     *
     * @return the set of allowed parent item types, empty set if no parent allowed
     */
    public abstract Set<WikiItemType> getAllowedParentTypes( );

    /**
     * Check if this wiki item type supports content in revisions
     *
     * @return true if content is supported, false otherwise
     */
    public boolean supportsContent( )
    {
        return true;
    }

    /**
     * Get the view URL for this item
     *
     * @return the view URL
     */
    public String getViewUrl( )
    {
        return WikiUrlService.buildViewUrl( this );
    }

    /**
     * Get the view URL for the parent of this item
     *
     * @return the parent view URL
     */
    public String getParentViewUrl( )
    {
        return WikiUrlService.buildParentViewUrl( this );
    }

    /**
     * Clone the wiki item
     *
     * @return a copy of the wiki item
     */
    @Override
    public AbstractWikiItem clone( )
    {
        try
        {
            return (AbstractWikiItem) super.clone( );
        }
        catch( CloneNotSupportedException e )
        {
            throw new AssertionError( e );
        }
    }
}
