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
package fr.paris.lutece.plugins.wiki.utils;

import fr.paris.lutece.plugins.wiki.service.WikiDiff;

import org.apache.commons.lang.StringUtils;

import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;

import java.util.ArrayList;
import java.util.List;


public class DiffUtils
{
    private static final int DIFF_UNCHANGED_LINE_DISPLAY = 2;

    public static List<WikiDiff> diff( String newVersion, String oldVersion )
    {
        String version1 = newVersion;
        String version2 = oldVersion;

        if ( version2 == null )
        {
            version2 = "";
        }

        version2 = StringUtils.remove( version2, '\r' );
        version1 = StringUtils.remove( version1, '\r' );

        List<WikiDiff> result = DiffUtils.process( version1, version2 );

        return result;
    }

    private static List<WikiDiff> process( String newVersion, String oldVersion )
    {
        if ( newVersion.equals( oldVersion ) )
        {
            return new ArrayList<WikiDiff>(  );
        }

        String[] oldArray = DiffUtils.split( oldVersion );
        String[] newArray = DiffUtils.split( newVersion );
        Diff<String> diffObject = new Diff<String>( oldArray, newArray );
        List<Difference> diffs = diffObject.diff(  );

        return DiffUtils.generateWikiDiffs( diffs, oldArray, newArray );
    }

    private static String[] split( String original )
    {
        if ( original == null )
        {
            return new String[0];
        }

        return original.split( "\n" );
    }

    private static List<WikiDiff> generateWikiDiffs( List<Difference> diffs, String[] oldArray, String[] newArray )
    {
        List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>(  );
        Difference previousDiff = null;
        Difference nextDiff = null;
        List<WikiDiff> changedLineWikiDiffs = null;
        String[] oldLineArray = null;
        String[] newLineArray = null;
        List<Difference> changedLineDiffs = null;
        List<WikiDiff> wikiSubDiffs = null;
        Difference nextLineDiff = null;
        int i = 0;

        for ( Difference currentDiff : diffs )
        {
            i++;
            wikiDiffs.addAll( DiffUtils.preBufferDifference( currentDiff, previousDiff, oldArray, newArray,
                    DIFF_UNCHANGED_LINE_DISPLAY ) );
            changedLineWikiDiffs = DiffUtils.processDifference( currentDiff, oldArray, newArray );

            // loop through the difference and diff the individual lines so that it is possible to highlight the exact
            // text that was changed
            for ( WikiDiff changedLineWikiDiff : changedLineWikiDiffs )
            {
                oldLineArray = DiffUtils.stringToArray( changedLineWikiDiff.getOldText(  ) );
                newLineArray = DiffUtils.stringToArray( changedLineWikiDiff.getNewText(  ) );
                changedLineDiffs = new Diff<String>( oldLineArray, newLineArray ).diff(  );
                wikiSubDiffs = new ArrayList<WikiDiff>(  );

                int j = 0;

                for ( Difference changedLineDiff : changedLineDiffs )
                {
                    // build sub-diff list, which is the difference for the individual
                    // line item
                    j++;

                    if ( j == 1 )
                    {
                        // pre-buffering is only necessary for the first element as post-buffering
                        // will handle all further buffering when bufferAmount is -1.
                        wikiSubDiffs.addAll( DiffUtils.preBufferDifference( changedLineDiff, null, oldLineArray,
                                newLineArray, -1 ) );
                    }

                    wikiSubDiffs.addAll( DiffUtils.processDifference( changedLineDiff, oldLineArray, newLineArray ) );
                    nextLineDiff = ( j < changedLineDiffs.size(  ) ) ? changedLineDiffs.get( j ) : null;
                    wikiSubDiffs.addAll( DiffUtils.postBufferDifference( changedLineDiff, nextLineDiff, oldLineArray,
                            newLineArray, -1 ) );
                }

                changedLineWikiDiff.setSubDiffs( wikiSubDiffs );
            }

            wikiDiffs.addAll( changedLineWikiDiffs );
            nextDiff = ( i < diffs.size(  ) ) ? diffs.get( i ) : null;
            wikiDiffs.addAll( DiffUtils.postBufferDifference( currentDiff, nextDiff, oldArray, newArray,
                    DIFF_UNCHANGED_LINE_DISPLAY ) );
            previousDiff = currentDiff;
        }

        return wikiDiffs;
    }

    private static List<WikiDiff> processDifference( Difference currentDiff, String[] oldArray, String[] newArray )
    {
        List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>(  );

        // if text was deleted then deletedCurrent represents the starting position of the deleted text.
        int deletedCurrent = currentDiff.getDeletedStart(  );

        // if text was added then addedCurrent represents the starting position of the added text.
        int addedCurrent = currentDiff.getAddedStart(  );

        // count is simply used to ensure that the loop is not infinite, which should never happen
        int count = 0;

        // the text of the element that changed
        String oldText = null;

        // the text of what the element was changed to
        String newText = null;

        while ( hasMoreDiffInfo( addedCurrent, deletedCurrent, currentDiff ) )
        {
            // the position within the diff array (line number, character, etc) at which the change
            // started (starting at 0)
            int position = ( ( deletedCurrent < 0 ) ? 0 : deletedCurrent );
            oldText = null;
            newText = null;

            if ( ( currentDiff.getDeletedEnd(  ) >= 0 ) && ( currentDiff.getDeletedEnd(  ) >= deletedCurrent ) )
            {
                oldText = oldArray[deletedCurrent];
                deletedCurrent++;
            }

            if ( ( currentDiff.getAddedEnd(  ) >= 0 ) && ( currentDiff.getAddedEnd(  ) >= addedCurrent ) )
            {
                newText = newArray[addedCurrent];
                addedCurrent++;
            }

            wikiDiffs.add( new WikiDiff( oldText, newText, position ) );
            // FIXME - this shouldn't be necessary
            count++;

            if ( count > 5000 )
            {
                break;
            }
        }

        return wikiDiffs;
    }

    private static List<WikiDiff> postBufferDifference( Difference currentDiff, Difference nextDiff, String[] oldArray,
        String[] newArray, int bufferAmount )
    {
        List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>(  );

        if ( bufferAmount == 0 )
        {
            // do not buffer
            return wikiDiffs;
        }

        int deletedCurrent = ( currentDiff.getDeletedEnd(  ) == -1 ) ? currentDiff.getDeletedStart(  )
                                                                     : ( currentDiff.getDeletedEnd(  ) + 1 );
        int addedCurrent = ( currentDiff.getAddedEnd(  ) == -1 ) ? currentDiff.getAddedStart(  )
                                                                 : ( currentDiff.getAddedEnd(  ) + 1 );
        int numIterations = bufferAmount;

        if ( bufferAmount == -1 )
        {
            // buffer everything
            numIterations = ( nextDiff != null )
                ? Math.max( nextDiff.getAddedStart(  ) - addedCurrent, nextDiff.getDeletedStart(  ) - deletedCurrent )
                : Math.max( oldArray.length - deletedCurrent, newArray.length - addedCurrent );
        }

        String oldText = null;
        String newText = null;

        for ( int i = 0; i < numIterations; i++ )
        {
            int position = ( deletedCurrent < 0 ) ? 0 : deletedCurrent;
            oldText = null;
            newText = null;

            if ( canPostBuffer( nextDiff, deletedCurrent, oldArray, false ) )
            {
                oldText = oldArray[deletedCurrent];
                deletedCurrent++;
            }

            if ( canPostBuffer( nextDiff, addedCurrent, newArray, true ) )
            {
                newText = newArray[addedCurrent];
                addedCurrent++;
            }

            if ( ( oldText == null ) && ( newText == null ) )
            {
                break;
            }

            wikiDiffs.add( new WikiDiff( oldText, newText, position ) );
        }

        return wikiDiffs;
    }

    private static String[] stringToArray( String original )
    {
        if ( original == null )
        {
            return new String[0];
        }

        String[] result = new String[original.length(  )];

        for ( int i = 0; i < result.length; i++ )
        {
            result[i] = String.valueOf( original.charAt( i ) );
        }

        return result;
    }

    private static List<WikiDiff> preBufferDifference( Difference currentDiff, Difference previousDiff,
        String[] oldArray, String[] newArray, int bufferAmount )
    {
        List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>(  );

        if ( bufferAmount == 0 )
        {
            return wikiDiffs;
        }

        if ( ( bufferAmount == -1 ) && ( previousDiff != null ) )
        {
            // when buffering everything, only pre-buffer for the first element as the post-buffer code
            // will handle everything else.
            return wikiDiffs;
        }

        // deletedCurrent is the current position in oldArray to start buffering from
        int deletedCurrent = ( ( bufferAmount == -1 ) || ( bufferAmount > currentDiff.getDeletedStart(  ) ) ) ? 0
                                                                                                              : ( currentDiff.getDeletedStart(  ) -
            bufferAmount );

        // addedCurrent is the current position in newArray to start buffering from
        int addedCurrent = ( ( bufferAmount == -1 ) || ( bufferAmount > currentDiff.getAddedStart(  ) ) ) ? 0
                                                                                                          : ( currentDiff.getAddedStart(  ) -
            bufferAmount );

        if ( previousDiff != null )
        {
            // if there was a previous diff make sure that it is not being overlapped
            deletedCurrent = Math.max( previousDiff.getDeletedEnd(  ) + 1, deletedCurrent );
            addedCurrent = Math.max( previousDiff.getAddedEnd(  ) + 1, addedCurrent );
        }

        // number of iterations is number of loops required to fully buffer the added and deleted diff
        int numIterations = Math.max( currentDiff.getDeletedStart(  ) - deletedCurrent,
                currentDiff.getAddedStart(  ) - addedCurrent );
        String oldText = null;
        String newText = null;

        for ( int i = 0; i < numIterations; i++ )
        {
            int position = ( deletedCurrent < 0 ) ? 0 : deletedCurrent;
            oldText = null;
            newText = null;

            // if diffs are close together, do not allow buffers to overlap
            if ( canPreBuffer( previousDiff, deletedCurrent, currentDiff.getDeletedStart(  ), oldArray, bufferAmount,
                        false ) )
            {
                oldText = oldArray[deletedCurrent];
                deletedCurrent++;
            }

            if ( canPreBuffer( previousDiff, addedCurrent, currentDiff.getAddedStart(  ), newArray, bufferAmount, true ) )
            {
                newText = newArray[addedCurrent];
                addedCurrent++;
            }

            if ( ( oldText == null ) && ( newText == null ) )
            {
                break;
            }

            wikiDiffs.add( new WikiDiff( oldText, newText, position ) );
        }

        return wikiDiffs;
    }

    private static boolean canPostBuffer( Difference nextDiff, int current, String[] replacementArray, boolean adding )
    {
        if ( ( current < 0 ) || ( current >= replacementArray.length ) )
        {
            // if out of a valid range, don't buffer
            return false;
        }

        if ( nextDiff == null )
        {
            // if in a valid range and no next diff, buffer away
            return true;
        }

        int nextStart = ( adding ) ? nextDiff.getAddedStart(  ) : nextDiff.getDeletedStart(  );

        // if in a valid range and the next diff starts several lines away, buffer away.  otherwise
        // the default is not to diff.
        return ( nextStart > current );
    }

    private static boolean hasMoreDiffInfo( int addedCurrent, int deletedCurrent, Difference currentDiff )
    {
        if ( addedCurrent == -1 )
        {
            addedCurrent = 0;
        }

        if ( deletedCurrent == -1 )
        {
            deletedCurrent = 0;
        }

        return ( ( addedCurrent <= currentDiff.getAddedEnd(  ) ) || ( deletedCurrent <= currentDiff.getDeletedEnd(  ) ) );
    }

    /**
     * Utility method for determining whether or not to prepend lines of context around a diff.
     */
    private static boolean canPreBuffer( Difference previousDiff, int current, int currentStart,
        String[] replacementArray, int bufferAmount, boolean adding )
    {
        if ( ( current < 0 ) || ( current >= replacementArray.length ) )
        {
            // current position is out of range for buffering
            return false;
        }

        if ( previousDiff == null )
        {
            // if no previous diff, buffer away
            return true;
        }

        if ( bufferAmount == -1 )
        {
            // if everything is being buffered and there was a previous diff do not pre-buffer
            return false;
        }

        int previousEnd = ( adding ) ? previousDiff.getAddedEnd(  ) : previousDiff.getDeletedEnd(  );

        if ( previousEnd != -1 )
        {
            // if there was a previous diff but it was several lines previous, buffer away.
            // if there was a previous diff, and it overlaps with the current diff, don't buffer.
            return ( current > ( previousEnd + bufferAmount ) );
        }

        int previousStart = ( adding ) ? previousDiff.getAddedStart(  ) : previousDiff.getDeletedStart(  );

        if ( current <= ( previousStart + bufferAmount ) )
        {
            // the previous diff did not specify an end, and the current diff would overlap with
            // buffering from its start, don't buffer
            return false;
        }

        // the previous diff did not specify an end, and the current diff will not overlap
        // with buffering from its start, buffer away.  otherwise the default is not to buffer.
        return ( currentStart > current );
    }
}
