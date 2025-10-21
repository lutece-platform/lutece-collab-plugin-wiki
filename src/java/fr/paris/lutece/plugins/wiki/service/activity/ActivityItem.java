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
package fr.paris.lutece.plugins.wiki.service.activity;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;

/**
 * DTO representing an activity item for the timeline
 */
public class ActivityItem
{
    private Revision _revision;
    private AbstractWikiItem _item;
    private boolean _bIsCreation;
    private int _nLinesAdded;
    private int _nLinesRemoved;
    private String _strPreviousContent;

    /**
     * Default constructor
     */
    public ActivityItem( )
    {
    }

    /**
     * Constructor with parameters
     *
     * @param revision
     *            The revision
     * @param item
     *            The wiki item
     * @param bIsCreation
     *            True if this is a creation (revision_number == 1)
     */
    public ActivityItem( Revision revision, AbstractWikiItem item, boolean bIsCreation )
    {
        _revision = revision;
        _item = item;
        _bIsCreation = bIsCreation;
    }

    /**
     * Returns the Revision
     *
     * @return The Revision
     */
    public Revision getRevision( )
    {
        return _revision;
    }

    /**
     * Sets the Revision
     *
     * @param revision
     *            The Revision
     */
    public void setRevision( Revision revision )
    {
        _revision = revision;
    }

    /**
     * Returns the Item
     *
     * @return The Item
     */
    public AbstractWikiItem getItem( )
    {
        return _item;
    }

    /**
     * Sets the Item
     *
     * @param item
     *            The Item
     */
    public void setItem( AbstractWikiItem item )
    {
        _item = item;
    }

    /**
     * Returns whether this is a creation activity
     *
     * @return True if creation
     */
    public boolean getIsCreation( )
    {
        return _bIsCreation;
    }

    /**
     * Sets whether this is a creation activity
     *
     * @param bIsCreation
     *            True if creation
     */
    public void setIsCreation( boolean bIsCreation )
    {
        _bIsCreation = bIsCreation;
    }

    /**
     * Returns the number of lines added
     *
     * @return The lines added count
     */
    public int getLinesAdded( )
    {
        return _nLinesAdded;
    }

    /**
     * Sets the number of lines added
     *
     * @param nLinesAdded
     *            The lines added count
     */
    public void setLinesAdded( int nLinesAdded )
    {
        _nLinesAdded = nLinesAdded;
    }

    /**
     * Returns the number of lines removed
     *
     * @return The lines removed count
     */
    public int getLinesRemoved( )
    {
        return _nLinesRemoved;
    }

    /**
     * Sets the number of lines removed
     *
     * @param nLinesRemoved
     *            The lines removed count
     */
    public void setLinesRemoved( int nLinesRemoved )
    {
        _nLinesRemoved = nLinesRemoved;
    }

    /**
     * Returns the previous revision content
     *
     * @return The previous content
     */
    public String getPreviousContent( )
    {
        return _strPreviousContent;
    }

    /**
     * Sets the previous revision content
     *
     * @param strPreviousContent
     *            The previous content
     */
    public void setPreviousContent( String strPreviousContent )
    {
        _strPreviousContent = strPreviousContent;
    }
}
