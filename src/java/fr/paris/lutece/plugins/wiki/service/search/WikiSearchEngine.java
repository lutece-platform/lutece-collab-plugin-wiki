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
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.impl.Book;
import fr.paris.lutece.plugins.wiki.business.item.impl.Page;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.search.IndexationService;
import fr.paris.lutece.portal.service.search.SearchEngine;
import fr.paris.lutece.portal.service.search.SearchItem;
import fr.paris.lutece.portal.service.search.SearchResult;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

@ApplicationScoped
public class WikiSearchEngine implements SearchEngine
{

    private static final String PROPERTY_ANALYZER_CLASS_NAME = "search.lucene.analyser.className";
    private static final String PROPERTY_FUZZY_ENABLED = "wiki.search.fuzzy.enabled";
    private static final String PROPERTY_FUZZY_MIN_RESULTS = "wiki.search.fuzzy.minResultsBeforeFuzzy";
    private static final String PROPERTY_FUZZY_EDIT_DISTANCE = "wiki.search.fuzzy.editDistance";

    private static final String PARAMETER_BOOK = "book";
    private static final String PARAMETER_SPACE = "space";
    private static final String FIELD_ROLE_NONE = "none";
    private static final String HTML_MARK_OPEN = "<mark>";
    private static final String HTML_MARK_CLOSE = "</mark>";
    private static final String FRAGMENT_SEPARATOR = "... ";
    private static final String ERROR_SEARCHING_WIKI = "Error searching wiki content";
    private static final String ERROR_PARSING_DATE = "Error parsing date for document ";
    private static final String ERROR_HIGHLIGHTING = "Error highlighting search result";
    private static final String ERROR_CREATING_ANALYZER = "Error creating analyzer";

    private static final int DEFAULT_MAX_RESULTS = 100;
    private static final int FRAGMENT_SIZE = 150;
    private static final int MAX_FRAGMENTS = 3;

    private static final boolean FUZZY_SEARCH_ENABLED = AppPropertiesService.getPropertyBoolean( PROPERTY_FUZZY_ENABLED, true );
    private static final int MIN_RESULTS_BEFORE_FUZZY = AppPropertiesService.getPropertyInt( PROPERTY_FUZZY_MIN_RESULTS, 5 );
    private static final int FUZZY_EDIT_DISTANCE = AppPropertiesService.getPropertyInt( PROPERTY_FUZZY_EDIT_DISTANCE, 2 );

    WikiSearchEngine( )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SearchResult> getSearchResults( String strQuery, HttpServletRequest request )
    {
        List<SearchResult> listResults = new ArrayList<>( );

        if ( strQuery == null || strQuery.trim( ).isEmpty( ) )
        {
            return listResults;
        }

        String strBookCode = request.getParameter( PARAMETER_BOOK );
        String strSpaceCode = request.getParameter( PARAMETER_SPACE );

        try
        {
            Directory directory = IndexationService.getDirectoryIndex( );

            if ( !DirectoryReader.indexExists( directory ) )
            {
                return listResults;
            }

            try ( IndexReader reader = DirectoryReader.open( directory ) )
            {
                IndexSearcher searcher = new IndexSearcher( reader );
                Analyzer analyzer = getAnalyzer( );

                Query exactQuery = buildQuery( strQuery, strBookCode, strSpaceCode, request, analyzer );
                TopDocs exactTopDocs = searcher.search( exactQuery, DEFAULT_MAX_RESULTS );

                Highlighter exactHighlighter = createHighlighter( exactQuery );

                for ( ScoreDoc scoreDoc : exactTopDocs.scoreDocs )
                {
                    Document doc = searcher.doc( scoreDoc.doc );
                    WikiSearchResult result = createSearchResult( doc, exactHighlighter, analyzer );
                    if ( result != null && hasHierarchicalAccess( result, request ) )
                    {
                        listResults.add( result );
                    }
                }

                if ( FUZZY_SEARCH_ENABLED && listResults.size( ) < MIN_RESULTS_BEFORE_FUZZY && !containsFuzzyOperator( strQuery ) )
                {
                    addFuzzyResults( searcher, strQuery, strBookCode, strSpaceCode, request, analyzer, listResults );
                }
            }
        }
        catch( IOException | org.apache.lucene.queryparser.classic.ParseException e )
        {
            AppLogService.error( ERROR_SEARCHING_WIKI, e );
        }

        return listResults;
    }

    /**
     * Creates the highlighter for search results
     *
     * @param query
     *            the search query
     * @return the configured highlighter
     */
    private Highlighter createHighlighter( Query query )
    {
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter( HTML_MARK_OPEN, HTML_MARK_CLOSE );
        QueryScorer scorer = new QueryScorer( query );
        Highlighter highlighter = new Highlighter( formatter, scorer );
        highlighter.setTextFragmenter( new SimpleSpanFragmenter( scorer, FRAGMENT_SIZE ) );
        return highlighter;
    }

    /**
     * Builds the search query based on search terms and filters
     *
     * @param strQuery
     *            the search query string
     * @param strBookCode
     *            the book code filter (optional)
     * @param request
     *            the HTTP request
     * @param analyzer
     *            the Lucene analyzer
     * @return the built query
     * @throws ParseException
     *             if query parsing fails
     */
    private Query buildQuery( String strQuery, String strBookCode, String strSpaceCode, HttpServletRequest request, Analyzer analyzer )
            throws org.apache.lucene.queryparser.classic.ParseException
    {

        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder( );

        String [ ] fields = {
                SearchItem.FIELD_TITLE, WikiSearchIndexer.FIELD_DESCRIPTION, SearchItem.FIELD_CONTENTS
        };
        MultiFieldQueryParser parser = new MultiFieldQueryParser( fields, analyzer );
        parser.setDefaultOperator( QueryParser.Operator.OR );

        Query searchQuery = parser.parse( strQuery );

        queryBuilder.add( searchQuery, BooleanClause.Occur.MUST );

        if ( strBookCode != null && !strBookCode.isEmpty( ) )
        {
            Query bookFilter = new TermQuery( new Term( WikiSearchIndexer.FIELD_BOOK_CODE, strBookCode ) );
            queryBuilder.add( bookFilter, BooleanClause.Occur.MUST );
        }

        if ( strSpaceCode != null && !strSpaceCode.isEmpty( ) )
        {
            Query spaceFilter = new TermQuery( new Term( WikiSearchIndexer.FIELD_SPACE_CODE, strSpaceCode ) );
            queryBuilder.add( spaceFilter, BooleanClause.Occur.MUST );
        }

        Query typeFilter = buildTypeFilter( );
        queryBuilder.add( typeFilter, BooleanClause.Occur.MUST );

        Query roleFilter = buildRoleFilter( request );
        if ( roleFilter != null )
        {
            queryBuilder.add( roleFilter, BooleanClause.Occur.MUST );
        }

        return queryBuilder.build( );
    }

    /**
     * Builds the type filter for wiki resource types
     *
     * @return the type filter query
     */
    private Query buildTypeFilter( )
    {
        BooleanQuery.Builder typeBuilder = new BooleanQuery.Builder( );

        typeBuilder.add( new TermQuery( new Term( SearchItem.FIELD_TYPE, Book.RESOURCE_TYPE ) ), BooleanClause.Occur.SHOULD );
        typeBuilder.add( new TermQuery( new Term( SearchItem.FIELD_TYPE, Page.RESOURCE_TYPE ) ), BooleanClause.Occur.SHOULD );

        return typeBuilder.build( );
    }

    /**
     * Builds the role filter based on user permissions
     *
     * @param request
     *            the HTTP request
     * @return the role filter query, or null if authentication is disabled
     */
    private Query buildRoleFilter( HttpServletRequest request )
    {
        if ( !SecurityService.isAuthenticationEnable( ) )
        {
            return null;
        }

        BooleanQuery.Builder roleBuilder = new BooleanQuery.Builder( );
        roleBuilder.add( new TermQuery( new Term( SearchItem.FIELD_ROLE, FIELD_ROLE_NONE ) ), BooleanClause.Occur.SHOULD );

        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );
        if ( user != null )
        {
            String [ ] userRoles = SecurityService.getInstance( ).getRolesByUser( user );
            if ( userRoles != null )
            {
                for ( String role : userRoles )
                {
                    roleBuilder.add( new TermQuery( new Term( SearchItem.FIELD_ROLE, role ) ), BooleanClause.Occur.SHOULD );
                }
            }
        }

        return roleBuilder.build( );
    }

    /**
     * Creates a search result from a Lucene document
     *
     * @param doc
     *            the Lucene document
     * @param highlighter
     *            the text highlighter
     * @param analyzer
     *            the Lucene analyzer
     * @return the search result, or null if creation fails
     */
    private WikiSearchResult createSearchResult( Document doc, Highlighter highlighter, Analyzer analyzer )
    {
        WikiSearchResult result = new WikiSearchResult( );

        result.setId( doc.get( SearchItem.FIELD_UID ) );
        result.setTitle( doc.get( SearchItem.FIELD_TITLE ) );
        result.setType( doc.get( SearchItem.FIELD_TYPE ) );
        result.setUrl( doc.get( SearchItem.FIELD_URL ) );

        parseResultDate( doc, result );
        generateResultSummary( doc, result, highlighter, analyzer );
        setResultRoles( doc, result );
        setResultWikiMetadata( doc, result );

        return result;
    }

    /**
     * Parses and sets the date for the search result
     *
     * @param doc
     *            the Lucene document
     * @param result
     *            the search result to update
     */
    private void parseResultDate( Document doc, WikiSearchResult result )
    {
        String strDate = doc.get( SearchItem.FIELD_DATE );
        if ( strDate != null )
        {
            try
            {
                Date date = org.apache.lucene.document.DateTools.stringToDate( strDate );
                result.setDate( date );
            }
            catch( ParseException e )
            {
                AppLogService.error( "{}{}", ERROR_PARSING_DATE, result.getId( ), e );
            }
        }
    }

    /**
     * Generates the summary for the search result with highlighting
     *
     * @param doc
     *            the Lucene document
     * @param result
     *            the search result to update
     * @param highlighter
     *            the text highlighter
     * @param analyzer
     *            the Lucene analyzer
     */
    private void generateResultSummary( Document doc, WikiSearchResult result, Highlighter highlighter, Analyzer analyzer )
    {
        String content = doc.get( SearchItem.FIELD_CONTENTS );
        if ( content != null )
        {
            try
            {
                String [ ] bestFragments = highlighter.getBestFragments( analyzer, SearchItem.FIELD_CONTENTS, content, MAX_FRAGMENTS );

                if ( bestFragments != null && bestFragments.length > 0 )
                {
                    StringBuilder summary = new StringBuilder( );
                    for ( String fragment : bestFragments )
                    {
                        if ( fragment != null && !fragment.trim( ).isEmpty( ) )
                        {
                            summary.append( fragment ).append( FRAGMENT_SEPARATOR );
                        }
                    }
                    result.setSummary( summary.toString( ) );
                }
                else
                {
                    result.setSummary( getTruncatedContent( content ) );
                }
            }
            catch( IOException | InvalidTokenOffsetsException e )
            {
                AppLogService.error( ERROR_HIGHLIGHTING, e );
                result.setSummary( getTruncatedContent( content ) );
            }
        }
    }

    /**
     * Gets truncated content for summary when highlighting fails
     *
     * @param content
     *            the original content
     * @return the truncated content
     */
    private String getTruncatedContent( String content )
    {
        return content.length( ) > FRAGMENT_SIZE ? content.substring( 0, FRAGMENT_SIZE ) + FRAGMENT_SEPARATOR : content;
    }

    /**
     * Sets the roles for the search result
     *
     * @param doc
     *            the Lucene document
     * @param result
     *            the search result to update
     */
    private void setResultRoles( Document doc, WikiSearchResult result )
    {
        List<String> roles = new ArrayList<>( );
        String role = doc.get( SearchItem.FIELD_ROLE );
        if ( role != null )
        {
            roles.add( role );
        }
        result.setRole( roles );
    }

    /**
     * Sets wiki-specific metadata for the search result
     *
     * @param doc
     *            the Lucene document
     * @param result
     *            the search result to update
     */
    private void setResultWikiMetadata( Document doc, WikiSearchResult result )
    {
        result.setBookCode( doc.get( WikiSearchIndexer.FIELD_BOOK_CODE ) );
        result.setPageCode( doc.get( WikiSearchIndexer.FIELD_PAGE_CODE ) );
    }

    /**
     * Checks if the user has hierarchical access to view the search result
     *
     * @param result
     *            the search result to check
     * @param request
     *            the HTTP request containing user information
     * @return true if the user has access, false otherwise
     */
    private boolean hasHierarchicalAccess( WikiSearchResult result, HttpServletRequest request )
    {
        String entityType = result.getType( );
        if ( entityType == null )
        {
            return false;
        }

        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        String itemCode = getItemCodeByType( result, entityType );
        if ( itemCode == null )
        {
            return false;
        }

        AbstractWikiItem item = WikiItemService.findByCode( itemCode );
        if ( item == null )
        {
            return false;
        }

        return WikiAccessControlService.canView( user, item );
    }

    /**
     * Gets the item code based on the entity type
     *
     * @param result
     *            the search result
     * @param entityType
     *            the entity type
     * @return the item code, or null if not found
     */
    private String getItemCodeByType( WikiSearchResult result, String entityType )
    {
        switch( entityType )
        {
            case Book.RESOURCE_TYPE:
                return result.getBookCode( );
            case Page.RESOURCE_TYPE:
                return result.getPageCode( );
            default:
                return null;
        }
    }

    private boolean containsFuzzyOperator( String query )
    {
        return query != null && query.contains( "~" );
    }

    private void addFuzzyResults( IndexSearcher searcher, String strQuery, String strBookCode, String strSpaceCode, HttpServletRequest request,
            Analyzer analyzer, List<SearchResult> existingResults ) throws IOException, org.apache.lucene.queryparser.classic.ParseException
    {

        String fuzzyQuery = buildFuzzyQuery( strQuery );
        Query query = buildQuery( fuzzyQuery, strBookCode, strSpaceCode, request, analyzer );
        TopDocs fuzzyTopDocs = searcher.search( query, DEFAULT_MAX_RESULTS );

        Highlighter fuzzyHighlighter = createHighlighter( query );

        java.util.Set<String> existingIds = new java.util.HashSet<>( );
        for ( SearchResult result : existingResults )
        {
            existingIds.add( result.getId( ) );
        }

        for ( ScoreDoc scoreDoc : fuzzyTopDocs.scoreDocs )
        {
            if ( existingResults.size( ) >= DEFAULT_MAX_RESULTS )
            {
                break;
            }

            Document doc = searcher.doc( scoreDoc.doc );
            String docId = doc.get( SearchItem.FIELD_UID );

            if ( !existingIds.contains( docId ) )
            {
                WikiSearchResult result = createSearchResult( doc, fuzzyHighlighter, analyzer );
                if ( result != null && hasHierarchicalAccess( result, request ) )
                {
                    existingResults.add( result );
                    existingIds.add( docId );
                }
            }
        }
    }

    private String buildFuzzyQuery( String query )
    {
        if ( query == null || query.trim( ).isEmpty( ) )
        {
            return query;
        }

        String [ ] terms = query.trim( ).split( "\\s+" );
        StringBuilder fuzzyQuery = new StringBuilder( );

        for ( String term : terms )
        {
            if ( term.startsWith( "\"" ) || term.startsWith( "+" ) || term.startsWith( "-" ) || term.contains( "*" ) || term.contains( "?" )
                    || term.contains( "~" ) )
            {
                fuzzyQuery.append( term );
            }
            else
            {
                fuzzyQuery.append( term ).append( "~" ).append( FUZZY_EDIT_DISTANCE );
            }
            fuzzyQuery.append( " " );
        }

        return fuzzyQuery.toString( ).trim( );
    }

    private Analyzer getAnalyzer( )
    {
        Analyzer indexationAnalyzer = IndexationService.getAnalyser( );
        if ( indexationAnalyzer != null )
        {
            return indexationAnalyzer;
        }

        String strAnalyzerClassName = AppPropertiesService.getProperty( PROPERTY_ANALYZER_CLASS_NAME );
        if ( strAnalyzerClassName != null )
        {
            try
            {
                Analyzer analyzer = (Analyzer) Class.forName( strAnalyzerClassName ).getDeclaredConstructor( ).newInstance( );
                return analyzer;
            }
            catch( ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e )
            {
                AppLogService.error( ERROR_CREATING_ANALYZER, e );
            }
        }

        return new SimpleAnalyzer( );
    }
}
