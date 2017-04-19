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

import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wiki Macro Service
 */
public class WikiMacroService
{
    private static final String BEAN_MACRO_DEFAULT = "wiki.macroDefault";
    private static boolean _bInit;
    private static Map<String, WikiMacro> _mapMacros;
    private static WikiMacro _macroDefault;

    /**
     * Service Initialization
     */
    private synchronized void init( )
    {
        List<WikiMacro> listMacros = SpringContextService.getBeansOfType( WikiMacro.class );
        _mapMacros = new HashMap<String, WikiMacro>( );
        AppLogService.info( "Wiki - initializing macros ..." );
        for ( WikiMacro macro : listMacros )
        {
            _mapMacros.put( macro.getName( ), macro );
            AppLogService.info( "Wiki - New macro '" + macro.getName( ) + "' registered" );
        }
        _macroDefault = SpringContextService.getBean( BEAN_MACRO_DEFAULT );
        _bInit = true;
    }

    /**
     * Service processing
     * 
     * @param strText
     *            The Text to process
     * @return The output text
     */
    public String processMacro( String strText )
    {
        if ( !_bInit )
        {
            init( );
        }
        String strMacro = getMacroName( strText );
        if ( strMacro != null )
        {
            WikiMacro macro = _mapMacros.get( strMacro );
            if ( macro != null )
            {
                return macro.processText( getMacroTextValue( strText ) );
            }
        }
        return _macroDefault.processText( strText );
    }

    /**
     * Extract the the name of the macro from the text
     * 
     * @param strText
     *            The Text
     * @return The macro name or null if not found
     */
    private String getMacroName( String strText )
    {
        int nPos = strText.indexOf( "|" );
        if ( nPos > 0 )
        {
            return strText.substring( 0, nPos ).trim( ).toLowerCase( );
        }
        return null;
    }

    /**
     * Extract the text of the macro
     * 
     * @param strText
     *            The Text
     * @return The text value of the macro
     */
    private String getMacroTextValue( String strText )
    {
        int nPos = strText.indexOf( "|" );
        if ( nPos > 0 )
        {
            return strText.substring( nPos + 1 );
        }
        return "";
    }

}
