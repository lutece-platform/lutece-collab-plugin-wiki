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
    private String _strNewText;

    /** The old text that was changed, or <code>null</code> if new text was added. */
    private String _strOldText;

    /** The zero-based position of the text that was changed. */
    private int _nPosition = -1;

    /** The diff may (optionally) contain a list of sub-diffs, such as when diffing two topics and then further showing what changed on a line. */
    private List<WikiDiff> _listSubDiffs;

    /**
     * Constructor
     */
    public WikiDiff(  )
    {
    }

    /**
     *
     * @param strOldText Old Text
     * @param strNewText New Text
     * @param nPosition Position
     */
    public WikiDiff( String strOldText, String strNewText, int nPosition )
    {
        _strOldText = strOldText;
        _strNewText = strNewText;
        _nPosition = nPosition;
    }

    /**
     * Gets changes
     * @return true if there is changes
     */
    public boolean getChange(  )
    {
        return !StringUtils.equals( _strOldText, _strNewText );
    }

    /**
     * Gets new text
     * @return The new text
     */
    public String getNewText(  )
    {
        return _strNewText;
    }

    /**
     * Sets new text
     * @param strNewText The new text
     */
    public void setNewText( String strNewText )
    {
        _strNewText = strNewText;
    }

    /**
     * Gets old text
     * @return The old text
     */
    public String getOldText(  )
    {
        return _strOldText;
    }

    /**
     * Sets old text
     * @param strOldText The old text
     */
    public void setOldText( String strOldText )
    {
        _strOldText = strOldText;
    }

    /**
     * Gets the position
     * @return The position
     */
    public int getPosition(  )
    {
        return _nPosition;
    }

    /**
     * Sets the position
     * @param nPosition The position
     */
    public void setPosition( int nPosition )
    {
        _nPosition = nPosition;
    }

    /**
     * Gets diffs
     * @return The diffs
     */
    public List<WikiDiff> getSubDiffs(  )
    {
        return _listSubDiffs;
    }

    /**
     * Sets the diffs list
     * @param liistSubDiffs The list
     */
    public void setSubDiffs( List<WikiDiff> liistSubDiffs )
    {
        _listSubDiffs = liistSubDiffs;
    }
}
