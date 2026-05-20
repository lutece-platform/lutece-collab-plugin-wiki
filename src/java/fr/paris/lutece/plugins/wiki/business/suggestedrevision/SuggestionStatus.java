/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.suggestedrevision;

/**
 * Lifecycle of a suggested revision.
 */
public enum SuggestionStatus
{
    PENDING,
    APPROVED,
    REJECTED;

    /**
     * Resolves a status from its string representation, defaulting to PENDING when null or unknown.
     *
     * @param value
     *            the raw value (case-insensitive)
     * @return the matching SuggestionStatus, never null
     */
    public static SuggestionStatus fromString( String value )
    {
        if ( value == null )
        {
            return PENDING;
        }
        try
        {
            return SuggestionStatus.valueOf( value.toUpperCase( ) );
        }
        catch ( IllegalArgumentException e )
        {
            return PENDING;
        }
    }
}
