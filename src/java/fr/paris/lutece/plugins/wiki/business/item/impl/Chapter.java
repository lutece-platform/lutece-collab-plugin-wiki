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
package fr.paris.lutece.plugins.wiki.business.item.impl;

import java.util.EnumSet;
import java.util.Set;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;

public class Chapter extends AbstractWikiItem
{
    private static final long serialVersionUID = 1L;
    public static final String RESOURCE_TYPE = "chapter";

    private int _nChapterOrder;

    /**
     * Constructor
     */
    public Chapter( )
    {
        setType( WikiItemType.CHAPTER );
    }

    /**
     * Copy constructor
     *
     * @param chapter
     *            the chapter to copy
     */
    public Chapter( Chapter chapter )
    {
        super( chapter );
        this._nChapterOrder = chapter._nChapterOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceType( )
    {
        return RESOURCE_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder( )
    {
        return _nChapterOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOrder( int order )
    {
        _nChapterOrder = order;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasOrder( )
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WikiItemType getChildType( )
    {
        return WikiItemType.PAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<WikiItemType> getAllowedChildTypes( )
    {
        return EnumSet.of( WikiItemType.PAGE );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<WikiItemType> getAllowedParentTypes( )
    {
        return EnumSet.of( WikiItemType.BOOK );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsContent( )
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Chapter clone( )
    {
        Chapter clone = (Chapter) super.clone( );
        clone._nChapterOrder = this._nChapterOrder;
        return clone;
    }
}
