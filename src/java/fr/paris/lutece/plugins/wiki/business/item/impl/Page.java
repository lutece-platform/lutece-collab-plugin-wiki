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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;

public class Page extends AbstractWikiItem
{
    private static final long serialVersionUID = 1L;
    public static final String RESOURCE_TYPE = "page";

    /**
     * Constructor
     */
    public Page( )
    {
        setType( WikiItemType.PAGE );
    }

    /**
     * Copy constructor
     * 
     * @param page
     *            the page to copy
     */
    public Page( Page page )
    {
        super( page );
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
    public Set<WikiItemType> getAllowedChildTypes( )
    {
        return Collections.emptySet( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<WikiItemType> getAllowedParentTypes( )
    {
        return EnumSet.of( WikiItemType.SPACE, WikiItemType.CATEGORY, WikiItemType.BOOK, WikiItemType.CHAPTER );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page clone( )
    {
        return (Page) super.clone( );
    }
}
