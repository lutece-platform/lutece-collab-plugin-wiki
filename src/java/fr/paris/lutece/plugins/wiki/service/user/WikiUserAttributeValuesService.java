/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.mylutece.modules.users.business.AttributeMapping;
import fr.paris.lutece.plugins.mylutece.modules.users.business.AttributeMappingHome;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * Holds the value combinations the searchable provider attributes take in the user directory, and
 * answers the suggestion requests of the permission panel from them.
 *
 * Combinations are kept, not plain value lists, because they let one field narrow another: the
 * offices suggested are those that coexist with the direction already chosen, whatever the depth of
 * the organisation tree. Directory values are messy — abbreviations, accents, spelling variants — so
 * suggesting the values that really exist is what makes those fields usable at all.
 *
 * The whole set stays on the server and is filtered per request, so a page never carries it: the
 * directory holds thousands of combinations once every level is mapped, and shipping them all would
 * weigh megabytes. The directory itself is read by a daemon, never on a request: walking it costs
 * seconds. The result is mirrored in the datastore so a restart does not leave the suggestions empty
 * until the next daemon run.
 */
public final class WikiUserAttributeValuesService
{
    private static final String DATASTORE_KEY = "wiki.userAttributeValues.snapshot";
    private static final ObjectMapper MAPPER = new ObjectMapper( );

    private static volatile Snapshot _snapshot;

    /**
     * Private constructor.
     */
    private WikiUserAttributeValuesService( )
    {
    }

    /**
     * Returns the values an attribute takes among the combinations still possible, given the values
     * already chosen for other attributes, and matching what the user typed. Matching ignores case
     * and accents, and looks anywhere in the value: directory labels are abbreviated and accented in
     * ways a user cannot reproduce.
     *
     * A context value the directory does not know is ignored rather than narrowing everything away,
     * so a field holding free text never empties the other lists.
     *
     * @param strAttributeName
     *            the attribute to suggest values for
     * @param strQuery
     *            what the user typed, may be null or empty
     * @param mapContext
     *            the values held by the other attribute fields, may be null
     * @param nLimit
     *            how many values to return at most
     * @return the matching values, sorted, empty when the attribute is unknown or holds no value
     */
    public static List<String> suggest( String strAttributeName, String strQuery, Map<String, String> mapContext, int nLimit )
    {
        Snapshot snapshot = getSnapshot( );
        int nColumn = strAttributeName != null ? snapshot.getAttributes( ).indexOf( strAttributeName.trim( ) ) : -1;

        if ( nColumn < 0 )
        {
            return new ArrayList<>( );
        }

        Map<Integer, String> mapConstraints = readConstraints( snapshot, mapContext, nColumn );
        String strNeedle = fold( strQuery );
        Set<String> setValues = new LinkedHashSet<>( );

        for ( int nRow = 0; nRow < snapshot.getCombinations( ).size( ); nRow++ )
        {
            List<String> listRow = snapshot.getCombinations( ).get( nRow );
            String strValue = listRow.get( nColumn );

            if ( strValue == null || ( !strNeedle.isEmpty( ) && !snapshot.getFoldedCombinations( ).get( nRow ).get( nColumn ).contains( strNeedle ) ) )
            {
                continue;
            }

            if ( mapConstraints.entrySet( ).stream( ).allMatch( constraint -> constraint.getValue( ).equals( listRow.get( constraint.getKey( ) ) ) ) )
            {
                setValues.add( strValue );
            }
        }

        return setValues.stream( ).sorted( ).limit( nLimit ).collect( Collectors.toList( ) );
    }

    /**
     * Resolves the context values to their directory spelling, keyed by the column they constrain.
     * A value the directory does not know is left out rather than narrowing everything away.
     *
     * @param snapshot
     *            the known combinations
     * @param mapContext
     *            the values held by the other attribute fields, may be null
     * @param nColumn
     *            the column being suggested, never constrained by itself
     * @return the canonical constraint value of each constrained column
     */
    private static Map<Integer, String> readConstraints( Snapshot snapshot, Map<String, String> mapContext, int nColumn )
    {
        Map<Integer, String> mapConstraints = new HashMap<>( );

        if ( mapContext == null )
        {
            return mapConstraints;
        }

        for ( Map.Entry<String, String> entry : mapContext.entrySet( ) )
        {
            int nOtherColumn = snapshot.getAttributes( ).indexOf( entry.getKey( ).trim( ) );

            if ( nOtherColumn >= 0 && nOtherColumn != nColumn && entry.getValue( ) != null && !entry.getValue( ).isBlank( ) )
            {
                String strCanonical = snapshot.findValue( nOtherColumn, entry.getValue( ).trim( ) );

                if ( strCanonical != null )
                {
                    mapConstraints.put( nOtherColumn, strCanonical );
                }
            }
        }

        return mapConstraints;
    }

    /**
     * Returns the searchable attribute names the last directory read covered.
     *
     * @return the attribute names, in mapping order
     */
    public static List<String> getAttributeNames( )
    {
        return getSnapshot( ).getAttributes( );
    }

    /**
     * Returns the value as the directory writes it, matching case and accents loosely: the directory
     * itself compares that way, so refusing "dsin" when it holds "DSIN" would be stricter than the
     * source of truth. Storing the directory spelling keeps one form in the database rather than as
     * many as there are ways to type it.
     *
     * @param strAttributeName
     *            the attribute name
     * @param strValue
     *            the value as it was typed
     * @return the directory spelling, null when the directory holds no such value
     */
    public static String canonicalValue( String strAttributeName, String strValue )
    {
        Snapshot snapshot = getSnapshot( );
        int nColumn = strAttributeName != null ? snapshot.getAttributes( ).indexOf( strAttributeName.trim( ) ) : -1;

        if ( nColumn < 0 || strValue == null || strValue.isBlank( ) )
        {
            return null;
        }

        return snapshot.findValue( nColumn, strValue.trim( ) );
    }

    /**
     * Reads the directory for every mapped attribute and replaces the known combinations. Called by
     * the daemon. An empty read is treated as a directory failure and leaves the previous
     * combinations in place: losing every suggestion is worse than serving slightly stale ones.
     */
    public static void refresh( )
    {
        List<String> listAttributes = readMappedAttributes( );

        if ( listAttributes.isEmpty( ) )
        {
            return;
        }

        List<Map<String, String>> listCombinations = ExternalUserSearchService.getInstance( ).getAttributeValues( listAttributes );

        if ( listCombinations.isEmpty( ) )
        {
            AppLogService.error( "The user directory returned no attribute value, keeping the previous ones" );
            return;
        }

        Set<List<String>> setRows = new LinkedHashSet<>( );
        for ( Map<String, String> mapCombination : listCombinations )
        {
            List<String> listRow = new ArrayList<>( listAttributes.size( ) );
            for ( String strAttribute : listAttributes )
            {
                listRow.add( mapCombination.get( strAttribute ) );
            }
            setRows.add( listRow );
        }

        Snapshot snapshot = new Snapshot( listAttributes, new ArrayList<>( setRows ) );
        _snapshot = snapshot;
        writeSnapshot( snapshot );
    }

    /**
     * Returns how many combinations are known. Meant for daemon logs.
     *
     * @return the combination count
     */
    public static int countCombinations( )
    {
        return getSnapshot( ).getCombinations( ).size( );
    }

    /**
     * Removes the accents and the case of a value, so a user finds a label without reproducing it.
     *
     * @param strValue
     *            the value, may be null
     * @return the folded value, empty when null
     */
    private static String fold( String strValue )
    {
        if ( strValue == null )
        {
            return "";
        }

        return StringUtils.stripAccents( strValue.trim( ) ).toLowerCase( Locale.FRENCH );
    }

    /**
     * Returns the combinations, loading them from the datastore on first use.
     *
     * @return the snapshot, empty when the directory was never read
     */
    private static Snapshot getSnapshot( )
    {
        Snapshot snapshot = _snapshot;

        if ( snapshot == null )
        {
            snapshot = readSnapshot( );
            _snapshot = snapshot;
        }

        return snapshot;
    }

    /**
     * Returns the searchable provider attributes, in mapping order. The mapping may carry stray
     * spaces, so names are trimmed to match what the search fields and the directory use.
     *
     * @return the attribute names, without duplicates
     */
    private static List<String> readMappedAttributes( )
    {
        List<String> listAttributes = new ArrayList<>( );

        for ( AttributeMapping mapping : AttributeMappingHome.getAttributeMappingsList( ) )
        {
            String strAttribute = mapping.getIdProviderAttribute( ) != null ? mapping.getIdProviderAttribute( ).trim( ) : null;

            if ( strAttribute != null && !strAttribute.isEmpty( ) && !listAttributes.contains( strAttribute ) )
            {
                listAttributes.add( strAttribute );
            }
        }

        return listAttributes;
    }

    /**
     * Reads the combinations stored by the last refresh.
     *
     * @return the stored snapshot, empty when nothing was stored or the snapshot is unreadable
     */
    private static Snapshot readSnapshot( )
    {
        String strStored = DatastoreService.getDataValue( DATASTORE_KEY, null );

        if ( strStored == null || strStored.isBlank( ) )
        {
            return new Snapshot( new ArrayList<>( ), new ArrayList<>( ) );
        }

        try
        {
            StoredSnapshot stored = MAPPER.readValue( strStored, StoredSnapshot.class );
            return new Snapshot( stored.attributes, stored.combinations );
        }
        catch( Exception e )
        {
            AppLogService.error( "Unreadable user attribute value snapshot, ignoring it", e );
            return new Snapshot( new ArrayList<>( ), new ArrayList<>( ) );
        }
    }

    /**
     * Stores the combinations so they survive a restart.
     *
     * @param snapshot
     *            the combinations to store
     */
    private static void writeSnapshot( Snapshot snapshot )
    {
        try
        {
            StoredSnapshot stored = new StoredSnapshot( );
            stored.attributes = snapshot.getAttributes( );
            stored.combinations = snapshot.getCombinations( );
            DatastoreService.setDataValue( DATASTORE_KEY, MAPPER.writeValueAsString( stored ) );
        }
        catch( Exception e )
        {
            AppLogService.error( "Could not store the user attribute values", e );
        }
    }

    /**
     * The datastore form of a snapshot.
     */
    private static final class StoredSnapshot
    {
        public List<String> attributes;
        public List<List<String>> combinations;
    }

    /**
     * The combinations of a directory read: the attributes giving the meaning of each column, and one
     * row of values per combination, a null standing for an attribute the entry did not carry.
     *
     * The folded form of every cell and a folded index of every column are built once here, so a
     * suggestion request is answered by lookups and substring scans, never by folding again.
     */
    private static final class Snapshot
    {
        private final List<String> _listAttributes;
        private final List<List<String>> _listCombinations;
        private final List<List<String>> _listFoldedCombinations;
        private final List<Set<String>> _listValuesByColumn;
        private final List<Map<String, String>> _listCanonicalByFoldedValue;

        /**
         * Builds the snapshot, padding ragged rows and indexing the values of each column.
         *
         * @param listAttributes
         *            the attribute names, in column order
         * @param listCombinations
         *            one row of values per combination
         */
        private Snapshot( List<String> listAttributes, List<List<String>> listCombinations )
        {
            _listAttributes = listAttributes != null ? listAttributes : new ArrayList<>( );
            _listCombinations = normalize( listCombinations, _listAttributes.size( ) );
            _listFoldedCombinations = new ArrayList<>( _listCombinations.size( ) );
            _listValuesByColumn = new ArrayList<>( _listAttributes.size( ) );
            _listCanonicalByFoldedValue = new ArrayList<>( _listAttributes.size( ) );

            for ( int nColumn = 0; nColumn < _listAttributes.size( ); nColumn++ )
            {
                _listValuesByColumn.add( new LinkedHashSet<>( ) );
                _listCanonicalByFoldedValue.add( new LinkedHashMap<>( ) );
            }

            for ( List<String> listRow : _listCombinations )
            {
                List<String> listFoldedRow = new ArrayList<>( listRow.size( ) );

                for ( int nColumn = 0; nColumn < listRow.size( ); nColumn++ )
                {
                    String strValue = listRow.get( nColumn );
                    String strFolded = strValue != null ? fold( strValue ) : null;
                    listFoldedRow.add( strFolded );

                    if ( strValue != null )
                    {
                        _listValuesByColumn.get( nColumn ).add( strValue );
                        _listCanonicalByFoldedValue.get( nColumn ).putIfAbsent( strFolded, strValue );
                    }
                }

                _listFoldedCombinations.add( listFoldedRow );
            }
        }

        /**
         * Pads or truncates every row to the column count, so consumers never bound-check.
         *
         * @param listCombinations
         *            the rows as read, possibly ragged
         * @param nColumns
         *            the expected column count
         * @return rows of exactly nColumns values
         */
        private static List<List<String>> normalize( List<List<String>> listCombinations, int nColumns )
        {
            List<List<String>> listRows = new ArrayList<>( listCombinations != null ? listCombinations.size( ) : 0 );

            if ( listCombinations != null )
            {
                for ( List<String> listRow : listCombinations )
                {
                    List<String> listNormalized = new ArrayList<>( nColumns );
                    for ( int nColumn = 0; nColumn < nColumns; nColumn++ )
                    {
                        listNormalized.add( listRow != null && nColumn < listRow.size( ) ? listRow.get( nColumn ) : null );
                    }
                    listRows.add( listNormalized );
                }
            }

            return listRows;
        }

        /**
         * Returns the attribute names, in column order.
         *
         * @return the attribute names
         */
        List<String> getAttributes( )
        {
            return _listAttributes;
        }

        /**
         * Returns the combinations.
         *
         * @return one row of values per combination
         */
        List<List<String>> getCombinations( )
        {
            return _listCombinations;
        }

        /**
         * Returns the folded form of every combination, aligned with {@link #getCombinations()}.
         *
         * @return one row of folded values per combination
         */
        List<List<String>> getFoldedCombinations( )
        {
            return _listFoldedCombinations;
        }

        /**
         * Returns a value of a column as the directory writes it, ignoring case and accents.
         *
         * @param nColumn
         *            the column
         * @param strValue
         *            the value as it was typed
         * @return the directory spelling, null when absent
         */
        String findValue( int nColumn, String strValue )
        {
            if ( nColumn < 0 || nColumn >= _listValuesByColumn.size( ) || strValue == null )
            {
                return null;
            }

            if ( _listValuesByColumn.get( nColumn ).contains( strValue ) )
            {
                return strValue;
            }

            return _listCanonicalByFoldedValue.get( nColumn ).get( fold( strValue ) );
        }
    }
}
