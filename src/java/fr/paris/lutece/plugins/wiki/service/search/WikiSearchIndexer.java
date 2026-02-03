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
package fr.paris.lutece.plugins.wiki.service.search;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.business.item.impl.Book;
import fr.paris.lutece.plugins.wiki.business.item.impl.Chapter;
import fr.paris.lutece.plugins.wiki.business.item.impl.Page;
import fr.paris.lutece.plugins.wiki.business.item.impl.Space;
import fr.paris.lutece.plugins.wiki.business.revision.Revision;
import fr.paris.lutece.plugins.wiki.business.revision.RevisionHome;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.WikiUrlService;

import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.search.IndexationService;
import fr.paris.lutece.portal.service.search.SearchIndexer;
import fr.paris.lutece.portal.service.search.SearchItem;
import fr.paris.lutece.portal.service.util.AppLogService;

public class WikiSearchIndexer implements SearchIndexer
{
    public static final String INDEXER_NAME = "WikiSearchIndexer";
    private static final String INDEXER_VERSION = "1.0.0";
    private static final String INDEXER_DESCRIPTION = "Wiki content indexer";

    public static final String FIELD_BOOK_ID = "book_id";
    public static final String FIELD_BOOK_CODE = "book_code";
    public static final String FIELD_PAGE_ID = "page_id";
    public static final String FIELD_PAGE_CODE = "page_code";
    public static final String FIELD_SPACE_CODE = "space_code";
    public static final String FIELD_ENTITY_TYPE = "entity_type";
    public static final String FIELD_DESCRIPTION = "description";

    private static final String LOG_ERROR_INDEXING = "Error indexing wiki item ";
    private static final String UNDERSCORE_SEPARATOR = "_";
    private static final String SPACE = " ";
    private static final String NONE_ROLE = "none";
    private static final String PORTAL_WIKI_URL = "Portal.jsp?page=wiki";

    private static final Pattern PATTERN_BASE64_IMAGE = Pattern.compile( "!\\[.*?\\]\\(data:image/[^;]+;base64,[^)]+\\)", Pattern.DOTALL );
    private static final Pattern PATTERN_IMAGE_TAG = Pattern.compile( "<img[^>]*src=[\"']data:image/[^;]+;base64,[^\"']+[\"'][^>]*>", Pattern.DOTALL );

    /**
     * Constructor
     */
    public WikiSearchIndexer( )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexDocuments( ) throws IOException, InterruptedException, SiteMessageException
    {
        List<AbstractWikiItem> listBooks = WikiItemService.getPublishedItemsByType( WikiItemType.BOOK );

        for ( AbstractWikiItem book : listBooks )
        {
            indexItem( book );
            indexItemChildren( book );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Document> getDocuments( String strIdDocument ) throws IOException, InterruptedException, SiteMessageException
    {
        List<Document> listDocuments = new ArrayList<>( );
        String [ ] parts = strIdDocument.split( UNDERSCORE_SEPARATOR );

        if ( parts.length >= 2 )
        {
            int entityId = Integer.parseInt( parts [1] );
            Document doc = getDocument( entityId );

            if ( doc != null )
            {
                listDocuments.add( doc );
            }
        }

        return listDocuments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return INDEXER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion( )
    {
        return INDEXER_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription( )
    {
        return INDEXER_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnable( )
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getListType( )
    {
        List<String> listTypes = new ArrayList<>( );
        listTypes.add( Book.RESOURCE_TYPE );
        listTypes.add( Page.RESOURCE_TYPE );
        return listTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpecificSearchAppUrl( )
    {
        return PORTAL_WIKI_URL;
    }

    /**
     * Index all children of a wiki item recursively
     * 
     * @param parent
     *            the parent item
     * @throws IOException
     *             if an error occurs during indexing
     */
    private void indexItemChildren( AbstractWikiItem parent ) throws IOException
    {
        List<AbstractWikiItem> children = WikiItemService.getPublishedItemsByParent( parent.getId( ) );

        for ( AbstractWikiItem child : children )
        {
            indexItem( child );
            indexItemChildren( child );
        }
    }

    /**
     * Index a wiki item
     * 
     * @param item
     *            the wiki item to index
     */
    private void indexItem( AbstractWikiItem item )
    {
        try
        {
            Document doc = createDocument( item );
            if ( doc != null )
            {
                IndexationService.write( doc );
            }
        }
        catch( IOException e )
        {
            AppLogService.error( "{}{}", LOG_ERROR_INDEXING, item.getId( ), e );
        }
    }

    /**
     * Get a document by entity ID
     * 
     * @param entityId
     *            the entity ID
     * @return the document or null if not found or not published
     */
    private Document getDocument( int entityId )
    {
        AbstractWikiItem item = WikiItemService.findById( entityId );

        if ( item != null && item.isPublished( ) && isHierarchyPublished( item ) )
        {
            return createDocument( item );
        }

        return null;
    }

    /**
     * Check if the entire hierarchy of an item is published
     * 
     * @param item
     *            the wiki item
     * @return true if the entire hierarchy is published, false otherwise
     */
    private boolean isHierarchyPublished( AbstractWikiItem item )
    {
        AbstractWikiItem current = item;

        while ( current.getIdParent( ) != null )
        {
            AbstractWikiItem parent = WikiItemService.findById( current.getIdParent( ) );
            if ( parent == null || !parent.isPublished( ) )
            {
                return false;
            }
            current = parent;
        }

        return true;
    }

    /**
     * Create a Lucene document from a wiki item
     * 
     * @param item
     *            the wiki item
     * @return the created document or null if no revision exists
     */
    private Document createDocument( AbstractWikiItem item )
    {
        Revision revision = RevisionHome.getCurrentRevision( item.getId( ) );

        if ( revision == null )
        {
            return null;
        }

        Document doc = new Document( );

        addCommonFields( doc, item, revision );
        addHierarchyFields( doc, item );
        addDateField( doc, item.getDateCreation( ) );
        addContentField( doc, revision );
        addUrlField( doc, item );

        return doc;
    }

    /**
     * Add common fields to the document
     * 
     * @param doc
     *            the Lucene document
     * @param item
     *            the wiki item
     * @param revision
     *            the current revision
     */
    private void addCommonFields( Document doc, AbstractWikiItem item, Revision revision )
    {
        String strId = item.getResourceType( ) + UNDERSCORE_SEPARATOR + item.getId( );
        doc.add( new StringField( SearchItem.FIELD_UID, strId, Field.Store.YES ) );
        doc.add( new StringField( SearchItem.FIELD_TYPE, item.getResourceType( ), Field.Store.YES ) );

        String strTitle = revision.getTitle( ) != null ? revision.getTitle( ) : item.getCode( );
        doc.add( new TextField( SearchItem.FIELD_TITLE, strTitle, Field.Store.YES ) );

        String strDescription = revision.getDescription( ) != null ? revision.getDescription( ) : "";
        doc.add( new TextField( FIELD_DESCRIPTION, strDescription, Field.Store.YES ) );

        String viewRole = getViewRole( item );
        doc.add( new StringField( SearchItem.FIELD_ROLE, viewRole, Field.Store.YES ) );
        doc.add( new StringField( FIELD_ENTITY_TYPE, item.getResourceType( ), Field.Store.YES ) );
    }

    /**
     * Get the view role for an item by traversing up to the book level
     * 
     * @param item
     *            the wiki item
     * @return the view role or "none" if not found
     */
    private String getViewRole( AbstractWikiItem item )
    {
        AbstractWikiItem current = item;

        while ( current != null )
        {
            if ( current instanceof Book )
            {
                return current.getViewRole( ) != null ? current.getViewRole( ) : NONE_ROLE;
            }
            current = current.getIdParent( ) != null ? WikiItemService.findById( current.getIdParent( ) ) : null;
        }

        return NONE_ROLE;
    }

    /**
     * Add hierarchy-specific fields based on item type
     * 
     * @param doc
     *            the Lucene document
     * @param item
     *            the wiki item
     */
    private void addHierarchyFields( Document doc, AbstractWikiItem item )
    {
        String resourceType = item.getResourceType( );

        switch( resourceType )
        {
            case Book.RESOURCE_TYPE:
                addBookIdentifiers( doc, (Book) item );
                break;

            case Page.RESOURCE_TYPE:
                Page page = (Page) item;
                addPageIdentifiers( doc, page );
                AbstractWikiItem parentItem = WikiItemService.findById( page.getIdParent( ) );
                if ( parentItem instanceof Chapter )
                {
                    Chapter chapterParent = (Chapter) parentItem;
                    AbstractWikiItem bookItem = WikiItemService.findById( chapterParent.getIdParent( ) );
                    if ( bookItem instanceof Book )
                    {
                        addBookIdentifiers( doc, (Book) bookItem );
                    }
                }
                else if ( parentItem instanceof Book )
                {
                    addBookIdentifiers( doc, (Book) parentItem );
                }
                break;
        }

        addSpaceCode( doc, item );
    }

    /**
     * Add book identifier fields to the document
     * 
     * @param doc
     *            the Lucene document
     * @param book
     *            the book item
     */
    private void addBookIdentifiers( Document doc, Book book )
    {
        doc.add( new StringField( FIELD_BOOK_ID, String.valueOf( book.getId( ) ), Field.Store.YES ) );
        doc.add( new StringField( FIELD_BOOK_CODE, book.getCode( ), Field.Store.YES ) );
    }

    /**
     * Add page identifier fields to the document
     * 
     * @param doc
     *            the Lucene document
     * @param page
     *            the page item
     */
    private void addPageIdentifiers( Document doc, Page page )
    {
        doc.add( new StringField( FIELD_PAGE_ID, String.valueOf( page.getId( ) ), Field.Store.YES ) );
        doc.add( new StringField( FIELD_PAGE_CODE, page.getCode( ), Field.Store.YES ) );
    }

    /**
     * Find the space code by walking up the parent chain and add it to the document
     *
     * @param doc
     *            the Lucene document
     * @param item
     *            the wiki item
     */
    private void addSpaceCode( Document doc, AbstractWikiItem item )
    {
        AbstractWikiItem current = item;

        while ( current != null )
        {
            if ( current instanceof Space )
            {
                doc.add( new StringField( FIELD_SPACE_CODE, current.getCode( ), Field.Store.YES ) );
                return;
            }
            current = current.getIdParent( ) != null ? WikiItemService.findById( current.getIdParent( ) ) : null;
        }
    }

    /**
     * Add content field to the document
     * 
     * @param doc
     *            the Lucene document
     * @param revision
     *            the current revision
     */
    private void addContentField( Document doc, Revision revision )
    {
        if ( revision.getContent( ) == null )
        {
            return;
        }

        StringBuilder contents = new StringBuilder( );
        String cleanedDescription = cleanMarkdownContent( revision.getDescription( ) );
        String cleanedContent = cleanMarkdownContent( revision.getContent( ) );

        contents.append( cleanedDescription ).append( SPACE ).append( cleanedContent );
        doc.add( new TextField( SearchItem.FIELD_CONTENTS, contents.toString( ), Field.Store.YES ) );
    }

    /**
     * Add date field to the document
     * 
     * @param doc
     *            the Lucene document
     * @param date
     *            the date to add
     */
    private void addDateField( Document doc, Timestamp date )
    {
        if ( date != null )
        {
            doc.add( new StringField( SearchItem.FIELD_DATE, DateTools.dateToString( date, DateTools.Resolution.DAY ), Field.Store.YES ) );
        }
    }

    /**
     * Add URL field to the document
     *
     * @param doc
     *            the Lucene document
     * @param item
     *            the wiki item
     */
    private void addUrlField( Document doc, AbstractWikiItem item )
    {
        String url = WikiUrlService.buildViewUrl( item );
        doc.add( new StringField( SearchItem.FIELD_URL, url, Field.Store.YES ) );
    }

    /**
     * Clean markdown content by removing base64 images and extra whitespace
     * 
     * @param content
     *            the content to clean
     * @return the cleaned content
     */
    private String cleanMarkdownContent( String content )
    {
        if ( content == null )
        {
            return "";
        }

        String cleaned = PATTERN_BASE64_IMAGE.matcher( content ).replaceAll( "" );
        cleaned = PATTERN_IMAGE_TAG.matcher( cleaned ).replaceAll( "" );
        cleaned = cleaned.replaceAll( "\\s+", " " ).trim( );

        return cleaned;
    }
}
