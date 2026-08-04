/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.user;

import fr.paris.lutece.plugins.mylutece.service.search.MyLuteceSearchUser;
import fr.paris.lutece.portal.service.security.LuteceUser;

/**
 * Builds the name the wiki displays for a user, whatever the source that user comes from.
 *
 * A readable first and last name is preferred, then the email, then the technical identifier, so
 * the same person is always rendered the same way.
 */
public final class WikiUserDisplayName
{
    /**
     * Private constructor.
     */
    private WikiUserDisplayName( )
    {
    }

    /**
     * Returns the name to display for a signed-in user.
     *
     * @param user
     *            the Lutece user
     * @return the display name, null only when the user is null
     */
    public static String of( LuteceUser user )
    {
        if ( user == null )
        {
            return null;
        }

        return firstNonBlank( join( user.getFirstName( ), user.getLastName( ) ), user.getEmail( ), user.getName( ) );
    }

    /**
     * Returns the name to display for a user found through the user search service.
     *
     * @param user
     *            the user found
     * @return the display name, null only when the user is null
     */
    public static String of( MyLuteceSearchUser user )
    {
        if ( user == null )
        {
            return null;
        }

        return firstNonBlank( join( user.getGivenName( ), user.getLastName( ) ), user.getEmail( ), user.getLogin( ) );
    }

    /**
     * Joins a first and a last name, tolerating either being missing.
     *
     * @param strFirstName
     *            the first name
     * @param strLastName
     *            the last name
     * @return the joined name, blank when both are missing
     */
    private static String join( String strFirstName, String strLastName )
    {
        return ( ( strFirstName != null ? strFirstName : "" ) + " " + ( strLastName != null ? strLastName : "" ) ).trim( );
    }

    /**
     * Returns the first candidate that carries something readable.
     *
     * @param strCandidates
     *            the candidates, in order of preference
     * @return the first non blank candidate, null when they are all blank
     */
    private static String firstNonBlank( String... strCandidates )
    {
        for ( String strCandidate : strCandidates )
        {
            if ( strCandidate != null && !strCandidate.isBlank( ) )
            {
                return strCandidate;
            }
        }

        return null;
    }
}
