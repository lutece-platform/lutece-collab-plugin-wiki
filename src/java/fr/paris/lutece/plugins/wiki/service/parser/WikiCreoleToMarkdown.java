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

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.Arrays;
import java.util.List;

public class WikiCreoleToMarkdown
{
    private static final String DOLLAR_SPAN = "$$span";
    private static final String DOLLAR_DOLLAR = "$$";
    private static final String CLASS = "class";

    public static String renderCustomContent( String str )
    {
        str = str.replaceAll( "\\\\", "" );
        str = str.replaceAll( "\\[lt;", "<" );
        str = str.replaceAll( "\\[gt;", ">" );
        str = str.replaceAll( "\\[nbsp;", "&nbsp;" );
        str = str.replaceAll( "\\[quot;", "'" );
        str = str.replaceAll( "\\[amp;", "&" );
        str = str.replaceAll( "\\[hashmark;", "#" );
        str = str.replaceAll( "\\[codeQuote;", "`" );
        str = str.replaceAll( "\\[simpleQuote;", "'" );
        str = str.replaceAll( "badge badge-", "badge badge-badge bg-" );
        str = str.replaceAll( "label label-", "badge badge-badge bg-" );
        str = str.replaceAll( "glyphicon glyphicon-warning-sign", "fa fa-exclamation-triangle" );
        str = str.replaceAll( "glyphicon glyphicon-info-sign", "fa fa-info-circle" );
        str = str.replaceAll( "glyphicon glyphicon-question-sign", "fa fa-question-circle" );
        str = str.replaceAll( "glyphicon glyphicon-ok-sign", "fa fa-check-circle" );
        str = str.replaceAll( "glyphicon glyphicon-remove-sign", "fa fa-times-circle" );
        str = str.replaceAll( "glyphicon glyphicon-chevron-right", "fa fa-chevron-right" );
        str = str.replaceAll( "glyphicon glyphicon-chevron-left", "fa fa-chevron-left" );
        str = str.replaceAll( "glyphicon glyphicon-chevron-up", "fa fa-chevron-up" );
        return str;
    }

    public static String wikiCreoleToMd( String strWikiText, String strPageName, String strPageUrl, String strLanguage )
    {


        String htmlContent = new LuteceWikiParser( strWikiText, strPageName, strPageUrl, strLanguage ).toString( );
        Document htmlDocument = Jsoup.parse( htmlContent );
        Element docBody = htmlDocument.body( );
        List<Element> elements = docBody.getAllElements( );
        for ( int i = 0; i < elements.size( ); i++ )
        {
            Element element = elements.get( i );
            if ( element.className( ).equals( "well" ) )
            {
                String toc =  DOLLAR_SPAN +  "<span class='toc'></span>" +  DOLLAR_DOLLAR ;
                toc = SpecialChar.reverseRender( toc );
                Element p = new Element("p");
                p.text(toc);
                docBody.prependChild(p);

            }
            else if ( element.className( ).equals( "jumbotron" ) )
                {
                    Element jumbotron = element;
                    String jumbotronTitle = jumbotron.select( "h1" ).text( );
                    String jumbotronText = jumbotron.select( "p" ).text( );
                    Element container = new Element( "span" );
                    container.attr( CLASS, "h-100 p-5 text-bg-light rounded-3" );
                    container.attr( "style", "display: block;" );
                    if ( !jumbotron.select( "img" ).isEmpty() )
                    {
                        Element img = jumbotron.select( "img" ).first( );
                        Element figure = new Element( "figure" );
                        figure.attr( CLASS, "figure" );
                        figure.appendChild( img );
                        container.appendChild( figure );
                    }
                    container.appendChild( new Element( "h1" ).attr( CLASS, "text-dark" ).text( jumbotronTitle ) );
                    container.appendChild( new Element( "p" ).attr( CLASS, "text-muted" ).text( jumbotronText ) );
                    String bootStrap5Jumbotron =  DOLLAR_SPAN +  container +  DOLLAR_DOLLAR ;
                    bootStrap5Jumbotron = SpecialChar.reverseRender( bootStrap5Jumbotron );
                    Element p = new Element("p");
                    p.text(bootStrap5Jumbotron);
                    jumbotron.replaceWith( p );

                }
                else if ( !element.className( ).isEmpty( ) )
                    {
                        String [ ] classNamesToSkip = {
                                "null", "table", "tbody", "thead", "tr", "td", "th"
                        };
                        if ( Arrays.asList( classNamesToSkip ).contains( element.className( ) ) )
                        {
                            i++;
                        }
                        else
                        {
                            if ( element.parent( ).tagName( ).equals( "p" ) )
                            {
                                int subDivClassToSkip = element.parent( ).children( ).size( );
                                String parent = element.parent( ).outerHtml( );
                                String customElement = SpecialChar.reverseRender(parent);
                                customElement =  DOLLAR_SPAN +   customElement +  DOLLAR_DOLLAR  ;
                                Element p = new Element("p");
                                p.text(customElement);
                                element.parent( ).replaceWith( p );
                                i = i + subDivClassToSkip - 1;
                            }
                            else
                            {
                                String customElement = SpecialChar.reverseRender(element.outerHtml( ));
                                customElement =  DOLLAR_SPAN +   customElement +  DOLLAR_DOLLAR  ;
                                Element p = new Element("div");
                                p.append(customElement);
                                element.replaceWith( p );
                            }

                        }
                    }
                    else if (element.tagName().equals("img")) {
                            if (element.parent().tagName().equals("p")) {
                                String parent = element.parent().outerHtml();
                                String customElement = SpecialChar.reverseRender(parent);
                                customElement =  DOLLAR_SPAN + customElement + DOLLAR_DOLLAR  ;
                                Element p = new Element("p");
                                p.text(customElement);
                                element.parent().replaceWith(p);
                            }
                             if ((element.hasAttr(CLASS) && !element.className().isEmpty())
                                     || (element.hasAttr("width") && (!element.getElementsByAttribute("width").isEmpty()))
                                     || (element.hasAttr("height") && (!element.getElementsByAttribute("height").isEmpty()))
                                     || (element.hasAttr("align") && (!element.getElementsByAttribute("align").isEmpty()))) {
                                String customElement = SpecialChar.reverseRender(element.outerHtml());
                                customElement = DOLLAR_SPAN + customElement + DOLLAR_DOLLAR ;
                                 Element p = new Element("p");
                                p.text(customElement);
                                element.replaceWith(p);
                            }
                        }
                    }
        MutableDataSet options = new MutableDataSet();

        options.set(HtmlRenderer.HARD_BREAK, "<br />\n");
        options.set(FlexmarkHtmlConverter.BR_AS_PARA_BREAKS, false);
        options.set(FlexmarkHtmlConverter.OUTPUT_ATTRIBUTES_ID, false);
        options.set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false);
        options.set(FlexmarkHtmlConverter.OUTPUT_UNKNOWN_TAGS, true);
        String markdown = FlexmarkHtmlConverter.builder( options).build( ).convert( docBody.toString( ) );
        markdown = renderCustomContent( markdown );
        StringBuilder newMarkdown = new StringBuilder();


        for ( String line : markdown.split( System.lineSeparator( ) ) )
        {
          if ( line.contains( DOLLAR_SPAN ) )
            {
                line = line.replace( DOLLAR_SPAN, "" );
                line = line.replace( DOLLAR_DOLLAR, "" );
                String reFormatedLine =  line;
                newMarkdown.append(System.getProperty("line.separator"));

                newMarkdown.append(DOLLAR_SPAN);
                newMarkdown.append(System.getProperty("line.separator"));

                newMarkdown.append( reFormatedLine );
                newMarkdown.append(System.getProperty("line.separator"));

                newMarkdown.append(DOLLAR_DOLLAR);
                newMarkdown.append(System.getProperty("line.separator"));

            }
            else
            {
                newMarkdown.append(line);
                newMarkdown.append(System.getProperty("line.separator"));

            }
        }
        return newMarkdown.toString();
    }

}
