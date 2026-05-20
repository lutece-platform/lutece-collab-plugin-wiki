/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.merge;

import java.util.Collections;
import java.util.List;

/**
 * Immutable view of a contiguous piece of a three-way merge result. A block is either a
 * CONTEXT block (lines on which the merge produced no conflict) or a CONFLICT block
 * (lines for which the current revision and the proposed content diverge and require
 * a human decision).
 */
public final class MergeBlock
{
    /**
     * Kind of merge block produced by the parser.
     */
    public enum Type
    {
        CONTEXT, CONFLICT
    }

    private final Type _type;
    private final List<String> _ours;
    private final List<String> _theirs;

    /**
     * Builds an immutable merge block.
     *
     * @param type
     *            CONTEXT for non-conflicting lines, CONFLICT otherwise
     * @param ours
     *            lines from the current revision (for CONFLICT) or the merged content (for CONTEXT)
     * @param theirs
     *            lines from the proposed content (only meaningful for CONFLICT, empty for CONTEXT)
     */
    public MergeBlock( Type type, List<String> ours, List<String> theirs )
    {
        _type = type;
        _ours = ours == null ? Collections.emptyList( ) : List.copyOf( ours );
        _theirs = theirs == null ? Collections.emptyList( ) : List.copyOf( theirs );
    }

    /**
     * Returns the type of this block.
     *
     * @return the block type
     */
    public Type getType( )
    {
        return _type;
    }

    /**
     * Returns the lines from the current revision side. For a CONTEXT block these are
     * the merged lines to display as-is.
     *
     * @return the immutable list of lines on the "ours" side
     */
    public List<String> getOurs( )
    {
        return _ours;
    }

    /**
     * Returns the lines from the proposed side. Empty for CONTEXT blocks.
     *
     * @return the immutable list of lines on the "theirs" side
     */
    public List<String> getTheirs( )
    {
        return _theirs;
    }

    /**
     * Convenience flag for templates.
     *
     * @return true if this block is a conflict
     */
    public boolean isConflict( )
    {
        return _type == Type.CONFLICT;
    }
}
