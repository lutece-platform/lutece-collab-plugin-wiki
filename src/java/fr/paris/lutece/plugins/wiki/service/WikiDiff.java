/*
 * Copyright (c) 2002-2012, Mairie de Paris
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

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

import java.util.List;


/**
 * Wiki Diff
 *
 */
public class WikiDiff implements Serializable
{
    /** The newly modified text, or <code>null</code> if text was deleted. */
    private String _strNewText = null;

    /** The old text that was changed, or <code>null</code> if new text was added. */
    private String _strOldText = null;

    /** The zero-based position of the text that was changed. */
    private int _nPosition = -1;

    /** The diff may (optionally) contain a list of sub-diffs, such as when diffing two topics and then further showing what changed on a line. */
    private List<WikiDiff> _listSubDiffs = null;

    /**
     *
     */
    public WikiDiff(  )
    {
    }

    /**
     *
     * @param oldText
     * @param newText
     * @param position
     */
    public WikiDiff( String oldText, String newText, int position )
    {
        _strOldText = oldText;
        _strNewText = newText;
        _nPosition = position;
    }

    /**
     *
     * @return
     */
    public boolean getChange(  )
    {
        return !StringUtils.equals( _strOldText, _strNewText );
    }

    /**
     *
     * @return
     */
    public String getNewText(  )
    {
        return _strNewText;
    }

    /**
     *
     * @param newText
     */
    public void setNewText( String newText )
    {
        _strNewText = newText;
    }

    /**
     *
     * @return
     */
    public String getOldText(  )
    {
        return _strOldText;
    }

    /**
     *
     * @param oldText
     */
    public void setOldText( String oldText )
    {
        _strOldText = oldText;
    }

    /**
     *
     * @return
     */
    public int getPosition(  )
    {
        return _nPosition;
    }

    /**
     *
     * @param position
     */
    public void setPosition( int position )
    {
        _nPosition = position;
    }

    /**
     *
     * @return
     */
    public List<WikiDiff> getSubDiffs(  )
    {
        return _listSubDiffs;
    }

    /**
     *
     * @param subDiffs
     */
    public void setSubDiffs( List<WikiDiff> subDiffs )
    {
        _listSubDiffs = subDiffs;
    }
}
