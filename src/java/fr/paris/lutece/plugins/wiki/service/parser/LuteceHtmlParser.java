/*
 * Copyright (c) 2002-2023, City of Paris
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class LuteceHtmlParser
{
    private final static String STYLE = "style";
    private final static String WIKI_ALIGN_CONTENT_VAL = "wiki-align-content-val-";
    private final static String SUB_LINK_CONTAINER = "subLinkContainer";
    public static String parseHtml( String htmlFromEditor, String wikiPageUrl, String pageTitle )
    {
        htmlFromEditor = SpecialChar.renderWiki( htmlFromEditor );
        Document parser = Jsoup.parse( htmlFromEditor );
        Element doc = parser.body( );
        List<Element> headers = doc.select( "h1, h2, h3, h4, h5, h6" );
        for ( Element header : headers )
        {
            String headerText = header.text( );
            headerText = headerText.replaceAll( " ", "_" );
           // replace all non-alphanumeric characters with nothing
            headerText = headerText.replaceAll( "[^A-Za-z0-9_]", "" );
            headerText = headerText.toLowerCase( );
            header.attr( "id", headerText );
        }
        List<Element> preElements = doc.select( "pre" );
        for ( Element preElement : preElements )
        {
            preElement.attr( STYLE, "background-color: #2f3241" );
        }
        doc.select( ".toastui-editor-md-preview-highlight" ).forEach( element -> element.removeClass( "toastui-editor-md-preview-highlight" ) );
        if ( doc.select( ".ProseMirror" ).text( ).contains( WIKI_ALIGN_CONTENT_VAL ) )
        {
            String alignmentValue = doc.select( ".ProseMirror" ).text( ).split( WIKI_ALIGN_CONTENT_VAL ) [1].substring( 0, 1 );
            doc.select( ".toastui-editor-contents" ).addClass( WIKI_ALIGN_CONTENT_VAL + alignmentValue );
        }
        Element toc = doc.select( ".toc" ).first( );
        if ( toc != null )
        {
            Element tableOfContent = createTableOfContent( doc, wikiPageUrl, pageTitle );
            doc.select( ".toc" ).remove( );

            Element flexDiv = new Element( "div" );
            flexDiv.addClass( "wiki-nav-content-wrapper" );
            flexDiv.appendChild( tableOfContent );
            Element contentDiv = new Element( "div" );
            contentDiv.append( parser.body( ).outerHtml( ) );
            flexDiv.appendChild( contentDiv );
            doc = flexDiv;

            return SpecialChar.reverseRender( doc.outerHtml( ) );
        }
        else
        {
            Element contentDiv = new Element( "div" );
            contentDiv.append( parser.body( ).outerHtml( ) );
            return SpecialChar.reverseRender( contentDiv.outerHtml( ) );
        }
    }

    public static Element createTableOfContent( Element doc, String wikiPageUrl, String pageTitle )
    {
        Element tableOfContent = new Element( "ul" );
        tableOfContent.addClass( "nav" );
        tableOfContent.addClass( "flex-column" );
        tableOfContent.addClass( "wiki-topic-nav" );
        Element titleElement = new Element( "a" );
        titleElement.addClass( "nav-link" );
        titleElement.attr( "href", wikiPageUrl );
        titleElement.attr( STYLE, "font-weight: bold; font-size: 1.5rem;" );
        titleElement.text( pageTitle );
        tableOfContent.appendChild( titleElement );
        List<Element> headers = doc.select( "h1, h2, h3" );
        for ( int i = 0; i < headers.size( ); i++ )
        {
            Element header = headers.get( i );
            String headerText = header.text( );
            String headerLevel = header.tagName( );
            Element linkElement = new Element( "a" );
            linkElement.attr( "href", wikiPageUrl + "#" + header.id( ) );
            linkElement.addClass( "nav-link" );
            linkElement.text( headerText );
            Element navItem = new Element( "li" );
            if ( headerLevel.equals( "h1" ) )
            {
                navItem.addClass( "nav-item" );
                linkElement.attr( STYLE, "font-weight: bold;" );
                if ( i + 1 < headers.size( ) )
                {
                    if ( headers.get( i + 1 ).tagName( ).equals( "h1" ) )
                    {
                        tableOfContent.appendChild( linkElement );
                    }
                    else
                        if ( headers.get( i + 1 ).tagName( ).equals( "h2" ) || headers.get( i + 1 ).tagName( ).equals( "h3" ) )
                        {
                            Element divContainer = new Element( "div" );
                            divContainer.attr( STYLE, "display: flex; flex-direction: row; spacing: 5px;" );
                            divContainer.appendChild( linkElement );
                            navItem.appendChild( divContainer );
                            tableOfContent.appendChild( navItem );
                            Element subLinkContainer = new Element( "ul" );
                            subLinkContainer.addClass( SUB_LINK_CONTAINER );
                            tableOfContent.appendChild( subLinkContainer );
                        }
                        else
                        {
                            tableOfContent.appendChild( linkElement );
                        }
                }
            }
            if ( headerLevel.equals( "h2" ) || headerLevel.equals( "h3" ) )
            {
                List<Element> subLinkContainers = tableOfContent.getElementsByClass( SUB_LINK_CONTAINER );
                if ( subLinkContainers.isEmpty( ) )
                {
                    Element subLinkContainer = new Element( "ul" );
                    subLinkContainer.addClass( SUB_LINK_CONTAINER );
                    subLinkContainer.appendChild( linkElement );
                    tableOfContent.appendChild( subLinkContainer );
                }
                else
                {
                    Element subLinkContainer = subLinkContainers.get( subLinkContainers.size( ) - 1 );
                    subLinkContainer.appendChild( linkElement );
                }
            }
        }
        return tableOfContent;
    }

}
