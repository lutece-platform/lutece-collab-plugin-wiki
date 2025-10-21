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
package fr.paris.lutece.plugins.wiki.service;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.impl.Book;
import fr.paris.lutece.plugins.wiki.business.item.impl.Category;
import fr.paris.lutece.plugins.wiki.business.item.impl.Chapter;
import fr.paris.lutece.plugins.wiki.business.item.impl.Page;
import fr.paris.lutece.plugins.wiki.business.item.impl.Space;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.url.UrlItem;

/**
 * Service for generating Wiki navigation URLs
 */
public final class WikiUrlService
{
    private static final String XPAGE_WIKI = "wiki";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_VIEW = "view";
    private static final String PARAM_SPACE = "space";
    private static final String PARAM_BOOK = "book";
    private static final String PARAM_CHAPTER = "chapter";
    private static final String PARAM_WIKI_PAGE = "wiki_page";

    private static final String VIEW_LIST_WIKI = "listWiki";
    private static final String VIEW_SPACE = "viewSpace";
    private static final String VIEW_BOOK = "viewBook";
    private static final String VIEW_PAGE = "viewPage";

    private static final String JSP_PORTAL = "jsp/site/Portal.jsp";

    private WikiUrlService( )
    {
    }

    /**
     * Gets the portal base URL including context path
     *
     * @return the portal base URL
     */
    private static String getPortalBaseUrl( )
    {
        return AppPathService.getBaseUrl( ) + JSP_PORTAL;
    }

    /**
     * Builds the view URL for an item
     *
     * @param item
     *            the wiki item
     * @return the view URL
     */
    public static String buildViewUrl( AbstractWikiItem item )
    {
        if ( item == null )
        {
            return buildListUrl( );
        }

        if ( item instanceof Space )
        {
            return buildSpaceUrl( (Space) item );
        }
        else if ( item instanceof Category )
        {
            return buildCategoryUrl( (Category) item );
        }
        else if ( item instanceof Book )
        {
            return buildBookUrl( (Book) item );
        }
        else if ( item instanceof Chapter )
        {
            return buildChapterUrl( (Chapter) item );
        }
        else if ( item instanceof Page )
        {
            return buildPageUrl( (Page) item );
        }

        return buildListUrl( );
    }

    /**
     * Builds the view URL for the parent of an item
     *
     * @param item
     *            the wiki item
     * @return the parent view URL
     */
    public static String buildParentViewUrl( AbstractWikiItem item )
    {
        if ( item == null )
        {
            return buildListUrl( );
        }

        AbstractWikiItem parent = item.getParent( );
        if ( parent != null )
        {
            return buildViewUrl( parent );
        }

        return buildListUrl( );
    }

    /**
     * Builds the wiki list URL
     *
     * @return the list URL
     */
    public static String buildListUrl( )
    {
        UrlItem url = new UrlItem( getPortalBaseUrl( ) );
        url.addParameter( PARAM_PAGE, XPAGE_WIKI );
        url.addParameter( PARAM_VIEW, VIEW_LIST_WIKI );
        return url.getUrl( );
    }

    /**
     * Builds the space view URL
     *
     * @param space
     *            the space
     * @return the space URL
     */
    private static String buildSpaceUrl( Space space )
    {
        UrlItem url = new UrlItem( getPortalBaseUrl( ) );
        url.addParameter( PARAM_PAGE, XPAGE_WIKI );
        url.addParameter( PARAM_VIEW, VIEW_SPACE );
        url.addParameter( PARAM_SPACE, space.getCode( ) );
        return url.getUrl( );
    }

    /**
     * Builds the category view URL (redirects to space with anchor)
     *
     * @param category
     *            the category
     * @return the category URL
     */
    private static String buildCategoryUrl( Category category )
    {
        AbstractWikiItem space = category.getParent( );
        if ( space != null )
        {
            return buildSpaceUrl( (Space) space ) + "#category-" + category.getId( );
        }
        return buildListUrl( );
    }

    /**
     * Builds the book view URL
     *
     * @param book
     *            the book
     * @return the book URL
     */
    private static String buildBookUrl( Book book )
    {
        UrlItem url = new UrlItem( getPortalBaseUrl( ) );
        url.addParameter( PARAM_PAGE, XPAGE_WIKI );
        url.addParameter( PARAM_VIEW, VIEW_BOOK );
        url.addParameter( PARAM_BOOK, book.getCode( ) );
        return url.getUrl( );
    }

    /**
     * Builds the chapter view URL (redirects to book)
     *
     * @param chapter
     *            the chapter
     * @return the chapter URL
     */
    private static String buildChapterUrl( Chapter chapter )
    {
        AbstractWikiItem book = chapter.getParent( );
        if ( book != null )
        {
            return buildBookUrl( (Book) book ) + "#chapter-" + chapter.getId( );
        }
        return buildListUrl( );
    }

    /**
     * Builds the page view URL
     *
     * @param page
     *            the page
     * @return the page URL
     */
    private static String buildPageUrl( Page page )
    {
        UrlItem url = new UrlItem( getPortalBaseUrl( ) );
        url.addParameter( PARAM_PAGE, XPAGE_WIKI );
        url.addParameter( PARAM_VIEW, VIEW_PAGE );

        addNavigationParams( url, page.getParent( ) );

        url.addParameter( PARAM_WIKI_PAGE, page.getCode( ) );
        return url.getUrl( );
    }

    /**
     * Adds navigation parameters by traversing up the hierarchy
     *
     * @param url
     *            the URL to add parameters to
     * @param item
     *            the starting item
     */
    private static void addNavigationParams( UrlItem url, AbstractWikiItem item )
    {
        AbstractWikiItem current = item;
        while ( current != null )
        {
            switch( current.getType( ) )
            {
                case CHAPTER:
                    url.addParameter( PARAM_CHAPTER, current.getCode( ) );
                    break;
                case BOOK:
                    url.addParameter( PARAM_BOOK, current.getCode( ) );
                    return;
                case SPACE:
                    url.addParameter( PARAM_SPACE, current.getCode( ) );
                    return;
                case CATEGORY:
                    break;
                default:
                    break;
            }
            current = current.getParent( );
        }
    }

}
