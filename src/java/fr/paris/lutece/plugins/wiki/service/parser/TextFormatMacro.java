/*
 * Copyright (c) 2002-2017, Mairie de Paris
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

package fr.paris.lutece.plugins.wiki.service.parser;

import java.text.MessageFormat;

/**
 * Text Wrapper Macro
 */
public class TextFormatMacro implements WikiMacro
{
    private String _strName;
    private String _strFormat;

    /**
     * Sets the macro name
     * 
     * @param strName
     *            The macro name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName( )
    {
        return _strName;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String processText( String strText )
    {
        String [ ] args = strText.split( "\\|" );
        for ( int i = 0; i < args.length; i++ )
        {
            args [i] = args [i].trim( );
        }
        return MessageFormat.format( _strFormat, (Object [ ]) args );
    }

    /**
     * @return the Format Text
     */
    public String getFormat( )
    {
        return _strFormat;
    }

    /**
     * @param strFormat
     *            the Format text to set
     */
    public void setFormat( String strFormat )
    {
        _strFormat = strFormat;
    }

}
