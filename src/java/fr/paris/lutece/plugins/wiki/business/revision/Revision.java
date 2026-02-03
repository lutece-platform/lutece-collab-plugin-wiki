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

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * This is the business class for the object Revision
 */
public class Revision implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Revision resource type
     */
    public static final String RESOURCE_TYPE = "WIKI_REVISION";

    private int _nId;
    private int _nEntityId;
    private int _nRevisionNumber;

    @NotEmpty( message = "#i18n{wiki.validation.revision.Title.notEmpty}" )
    @Size( max = 255, message = "#i18n{wiki.validation.revision.Title.size}" )
    private String _strTitle;

    private String _strDescription;
    private String _strContent;

    @Size( max = 500, message = "#i18n{wiki.validation.revision.Comment.size}" )
    private String _strComment;

    @Size( max = 100, message = "#i18n{wiki.validation.revision.Author.size}" )
    private String _strAuthor;

    private Timestamp _dateCreation;
    private boolean _bIsCurrent;

    /**
     * Default constructor
     */
    public Revision( )
    {
    }

    /**
     * Copy constructor
     * 
     * @param revision
     *            The revision to copy
     */
    public Revision( Revision revision )
    {
        this._nId = revision._nId;
        this._nEntityId = revision._nEntityId;
        this._nRevisionNumber = revision._nRevisionNumber;
        this._strTitle = revision._strTitle;
        this._strDescription = revision._strDescription;
        this._strContent = revision._strContent;
        this._strComment = revision._strComment;
        this._strAuthor = revision._strAuthor;
        this._dateCreation = revision._dateCreation;
        this._bIsCurrent = revision._bIsCurrent;
    }

    /**
     * Returns the Id
     * 
     * @return The Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the Id
     * 
     * @param nId
     *            The Id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the EntityId
     * 
     * @return The EntityId
     */
    public int getEntityId( )
    {
        return _nEntityId;
    }

    /**
     * Sets the EntityId
     * 
     * @param nEntityId
     *            The EntityId
     */
    public void setEntityId( int nEntityId )
    {
        _nEntityId = nEntityId;
    }

    /**
     * Returns the RevisionNumber
     *
     * @return The RevisionNumber
     */
    public int getRevisionNumber( )
    {
        return _nRevisionNumber;
    }

    /**
     * Sets the RevisionNumber
     * 
     * @param nRevisionNumber
     *            The RevisionNumber
     */
    public void setRevisionNumber( int nRevisionNumber )
    {
        _nRevisionNumber = nRevisionNumber;
    }

    /**
     * Returns the Title
     * 
     * @return The Title
     */
    public String getTitle( )
    {
        return _strTitle;
    }

    /**
     * Sets the Title
     * 
     * @param strTitle
     *            The Title
     */
    public void setTitle( String strTitle )
    {
        _strTitle = strTitle;
    }

    /**
     * Returns the Description
     * 
     * @return The Description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the Description
     * 
     * @param strDescription
     *            The Description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the Content
     * 
     * @return The Content
     */
    public String getContent( )
    {
        return _strContent;
    }

    /**
     * Sets the Content
     * 
     * @param strContent
     *            The Content
     */
    public void setContent( String strContent )
    {
        _strContent = strContent;
    }

    /**
     * Returns the Comment
     * 
     * @return The Comment
     */
    public String getComment( )
    {
        return _strComment;
    }

    /**
     * Sets the Comment
     * 
     * @param strComment
     *            The Comment
     */
    public void setComment( String strComment )
    {
        _strComment = strComment;
    }

    /**
     * Returns the Author
     * 
     * @return The Author
     */
    public String getAuthor( )
    {
        return _strAuthor;
    }

    /**
     * Sets the Author
     * 
     * @param strAuthor
     *            The Author
     */
    public void setAuthor( String strAuthor )
    {
        _strAuthor = strAuthor;
    }

    /**
     * Returns the DateCreation
     * 
     * @return The DateCreation
     */
    public Timestamp getDateCreation( )
    {
        return _dateCreation;
    }

    /**
     * Sets the DateCreation
     * 
     * @param dateCreation
     *            The DateCreation
     */
    public void setDateCreation( Timestamp dateCreation )
    {
        _dateCreation = dateCreation;
    }

    /**
     * Returns the IsCurrent
     * 
     * @return The IsCurrent
     */
    public boolean getIsCurrent( )
    {
        return _bIsCurrent;
    }

    /**
     * Sets the IsCurrent
     * 
     * @param bIsCurrent
     *            The IsCurrent
     */
    public void setIsCurrent( boolean bIsCurrent )
    {
        _bIsCurrent = bIsCurrent;
    }
}
