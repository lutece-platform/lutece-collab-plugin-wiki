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
package fr.paris.lutece.plugins.wiki.business;

import fr.paris.lutece.plugins.wiki.web.Constants;

/**
 * WikiContent
 */
public class WikiContent
{
    // Variables declarations
    private String _strPageTitle;
    private String _strWikiContent;
    private String _strHtmlWikiContent;

    /**
     * Returns the PageTitle
     * 
     * @return The PageTitle
     */
    public String getPageTitle( )
    {
        return _strPageTitle;
    }

    /**
     * Sets the PageTitle
     * 
     * @param strPageTitle
     *            The PageTitle
     */
    public void setPageTitle( String strPageTitle )
    {
        _strPageTitle = strPageTitle;
    }

    /**
     * Returns the WikiContent
     * 
     * @return The WikiContent
     */
    public String getWikiContent( )
    {
         return _strWikiContent;
    }

    /**
     * Sets the WikiContent
     * 
     * @param strWikiContent
     *            The WikiContent
     */
    public void setWikiContent( String strWikiContent )
    {
        _strWikiContent = strWikiContent;
    }

    /**
     * Returns the HtmlWikiContent
     *
     * @return The HtmlWikiContent
     */
    public String getHtmlWikiContent( )
    {
        return _strHtmlWikiContent;
    }

    /**
     * Sets the HtmlWikiContent
     *
     * @param strHtmlWikiContent
     *            The HtmlWikiContent
     */
    public void setHtmlWikiContent( String strHtmlWikiContent )
    {
        _strHtmlWikiContent = strHtmlWikiContent;
    }


    /**
     * Set the WikiContent with labelling MarkdownLanguage
     * Labelling MarkdownLanguage is used to avoid to differentiate the content of with previous version of with wiki creole and the new one with markdown
     * @param strWikiContent
      *            The WikiContent
     */
    public void setContentLabellingMarkdownLanguage(String strWikiContent ){
        {
            _strWikiContent = Constants.MARKDOWN_TAG+strWikiContent;
        }
    }


}
