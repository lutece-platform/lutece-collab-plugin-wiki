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

import fr.paris.lutece.portal.service.util.AppLogService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.ccil.cowan.tagsoup.AttributesImpl;
import org.outerj.daisy.diff.HtmlCleaner;
import org.outerj.daisy.diff.XslFilter;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Diff Service
 */
public class DiffService
{

    private static final String XSL_OUTPUT = "fr/paris/lutece/plugins/wiki/service/output.xsl";

    public static String getDiff(String strOld, String strNew)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String[] css = new String[]
        {
        };

        try
        {
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

            TransformerHandler result = tf.newTransformerHandler();
            result.setResult(new StreamResult(baos));

            XslFilter filter = new XslFilter();

            ContentHandler postProcess = filter.xsl(result, XSL_OUTPUT);
            Locale locale = Locale.getDefault();
            String prefix = "diff";

            HtmlCleaner cleaner = new HtmlCleaner();

            InputSource oldSource = new InputSource(new StringReader(strOld));
            InputSource newSource = new InputSource(new StringReader(strNew));

            DomTreeBuilder oldHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(oldSource, oldHandler);
            System.out.print(".");
            TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

            DomTreeBuilder newHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(newSource, newHandler);
            System.out.print(".");
            TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

            postProcess.startDocument();
            postProcess.startElement("", "diffreport", "diffreport", new AttributesImpl());
            doCSS(css, postProcess);
            postProcess.startElement("", "diff", "diff", new AttributesImpl());
            HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess, prefix);

            HTMLDiffer differ = new HTMLDiffer(output);
            differ.diff(leftComparator, rightComparator);
            System.out.print(".");
            postProcess.endElement("", "diff", "diff");
            postProcess.endElement("", "diffreport", "diffreport");
            postProcess.endDocument();
        }
        catch (TransformerConfigurationException ex)
        {
            AppLogService.error( "DiffService Error : " + ex.getMessage() , ex );
        }
        catch (IOException ex)
        {
            AppLogService.error( "DiffService Error : " + ex.getMessage() , ex );
        }
        catch (SAXException ex)
        {
            AppLogService.error( "DiffService Error : " + ex.getMessage() , ex );
        }

        String strOutput = baos.toString();
        
        // Remove XML header
        strOutput = strOutput.substring( strOutput.indexOf( ">" ) + 1 );
        
        return strOutput;

    }

    private static void doCSS(String[] css, ContentHandler handler) throws SAXException
    {
        handler.startElement("", "css", "css",
                new AttributesImpl());
        for (String cssLink : css)
        {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "href", "href", "CDATA", cssLink);
            attr.addAttribute("", "type", "type", "CDATA", "text/css");
            attr.addAttribute("", "rel", "rel", "CDATA", "stylesheet");
            handler.startElement("", "link", "link",
                    attr);
            handler.endElement("", "link", "link");
        }

        handler.endElement("", "css", "css");

    }
}
