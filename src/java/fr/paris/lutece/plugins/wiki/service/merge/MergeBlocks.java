/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.merge;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.merge.MergeAlgorithm;
import org.eclipse.jgit.merge.MergeChunk;
import org.eclipse.jgit.merge.MergeChunk.ConflictState;
import org.eclipse.jgit.merge.MergeResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs a JGit line-based three-way merge between a base, an "ours" and a "theirs" content
 * and reshapes the resulting chunks into a flat list of MergeBlock objects suitable for
 * UI rendering. The intent is to keep the JGit dependency local to this class.
 */
public final class MergeBlocks
{
    private static final String CONFLICT_OPEN_MARKER = "<<<<<<<";

    private MergeBlocks( )
    {
    }

    /**
     * Computes line-level additions and deletions between two contents, mirroring the way a {@code git diff} would count {@code +} and {@code -} lines.
     *
     * @param base
     *            the content before the change (may be null)
     * @param head
     *            the content after the change (may be null)
     * @return a two-element array: {@code [additions, deletions]}
     */
    public static int [ ] stats( String base, String head )
    {
        RawText rBase = new RawText( safe( base ).getBytes( StandardCharsets.UTF_8 ) );
        RawText rHead = new RawText( safe( head ).getBytes( StandardCharsets.UTF_8 ) );
        EditList edits = new HistogramDiff( ).diff( RawTextComparator.DEFAULT, rBase, rHead );
        int additions = 0;
        int deletions = 0;
        for ( Edit e : edits )
        {
            additions += e.getEndB( ) - e.getBeginB( );
            deletions += e.getEndA( ) - e.getBeginA( );
        }
        return new int [ ] { additions, deletions };
    }

    /**
     * Detects whether a content string still carries a leftover conflict marker (i.e. the
     * "&lt;&lt;&lt;&lt;&lt;&lt;&lt;" opening line emitted by the underlying three-way merge).
     * Used to refuse a reviewer's resolution that wasn't fully cleaned up.
     *
     * @param content
     *            the content to inspect (may be null)
     * @return true when at least one conflict marker is still present
     */
    public static boolean containsConflictMarker( String content )
    {
        return content != null && content.contains( CONFLICT_OPEN_MARKER );
    }

    /**
     * Computes the merge blocks for a three-way merge.
     *
     * @param base
     *            the common ancestor content
     * @param ours
     *            the current revision content
     * @param theirs
     *            the proposed content
     * @return the ordered list of CONTEXT and CONFLICT blocks
     */
    public static List<MergeBlock> compute( String base, String ours, String theirs )
    {
        RawText rBase = new RawText( safe( base ).getBytes( StandardCharsets.UTF_8 ) );
        RawText rOurs = new RawText( safe( ours ).getBytes( StandardCharsets.UTF_8 ) );
        RawText rTheirs = new RawText( safe( theirs ).getBytes( StandardCharsets.UTF_8 ) );

        MergeResult<RawText> result = new MergeAlgorithm( ).merge( RawTextComparator.DEFAULT, rBase, rOurs, rTheirs );
        RawText[ ] sequences = new RawText [ ] { rBase, rOurs, rTheirs };

        List<MergeBlock> blocks = new ArrayList<>( );
        List<String> contextBuffer = new ArrayList<>( );
        List<String> oursBuffer = new ArrayList<>( );
        List<String> theirsBuffer = new ArrayList<>( );
        boolean inConflict = false;
        int conflictSideIndex = -1;

        for ( MergeChunk chunk : result )
        {
            ConflictState state = chunk.getConflictState( );
            RawText seq = sequences [chunk.getSequenceIndex( )];
            List<String> chunkLines = readLines( seq, chunk.getBegin( ), chunk.getEnd( ) );

            if ( state == ConflictState.NO_CONFLICT )
            {
                if ( inConflict )
                {
                    blocks.add( new MergeBlock( MergeBlock.Type.CONFLICT, oursBuffer, theirsBuffer ) );
                    oursBuffer = new ArrayList<>( );
                    theirsBuffer = new ArrayList<>( );
                    inConflict = false;
                    conflictSideIndex = -1;
                }
                contextBuffer.addAll( chunkLines );
                continue;
            }

            if ( chunk.getSequenceIndex( ) == 0 )
            {
                continue;
            }

            if ( !contextBuffer.isEmpty( ) )
            {
                blocks.add( new MergeBlock( MergeBlock.Type.CONTEXT, contextBuffer, List.of( ) ) );
                contextBuffer = new ArrayList<>( );
            }

            if ( state == ConflictState.FIRST_CONFLICTING_RANGE )
            {
                if ( inConflict )
                {
                    blocks.add( new MergeBlock( MergeBlock.Type.CONFLICT, oursBuffer, theirsBuffer ) );
                    oursBuffer = new ArrayList<>( );
                    theirsBuffer = new ArrayList<>( );
                }
                inConflict = true;
                conflictSideIndex = chunk.getSequenceIndex( );
                oursBuffer.addAll( chunkLines );
            }
            else
            {
                inConflict = true;
                if ( chunk.getSequenceIndex( ) == conflictSideIndex )
                {
                    oursBuffer.addAll( chunkLines );
                }
                else
                {
                    theirsBuffer.addAll( chunkLines );
                }
            }
        }

        if ( inConflict )
        {
            blocks.add( new MergeBlock( MergeBlock.Type.CONFLICT, oursBuffer, theirsBuffer ) );
        }
        if ( !contextBuffer.isEmpty( ) )
        {
            blocks.add( new MergeBlock( MergeBlock.Type.CONTEXT, contextBuffer, List.of( ) ) );
        }
        return blocks;
    }

    /**
     * Checks whether the computed merge contains at least one CONFLICT block.
     *
     * @param blocks
     *            the parsed merge blocks
     * @return true when a conflict was detected
     */
    public static boolean hasConflict( List<MergeBlock> blocks )
    {
        for ( MergeBlock b : blocks )
        {
            if ( b.isConflict( ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Flattens a list of merge blocks into the merged content, joining CONTEXT lines and
     * keeping only the "ours" side of CONFLICT blocks. Used when no conflict is present to
     * produce the auto-merged content.
     *
     * @param blocks
     *            the parsed merge blocks
     * @return the flattened content
     */
    public static String flatten( List<MergeBlock> blocks )
    {
        StringBuilder sb = new StringBuilder( );
        for ( MergeBlock b : blocks )
        {
            for ( String line : b.getOurs( ) )
            {
                sb.append( line ).append( '\n' );
            }
        }
        return sb.toString( );
    }

    /**
     * Reads a [begin, end) range of lines from a RawText sequence.
     *
     * @param seq
     *            the source sequence
     * @param begin
     *            the start index (inclusive)
     * @param end
     *            the end index (exclusive)
     * @return the corresponding lines as a list of strings
     */
    private static List<String> readLines( RawText seq, int begin, int end )
    {
        List<String> lines = new ArrayList<>( end - begin );
        for ( int i = begin; i < end; i++ )
        {
            lines.add( seq.getString( i ) );
        }
        return lines;
    }

    /**
     * Null-safe accessor.
     *
     * @param value
     *            the input value
     * @return the value or an empty string when null
     */
    private static String safe( String value )
    {
        return value == null ? "" : value;
    }
}
