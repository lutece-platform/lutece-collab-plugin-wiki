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
package fr.paris.lutece.plugins.wiki.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.mylutece.service.search.MyLuteceSearchUser;
import fr.paris.lutece.plugins.mylutece.service.search.IUserSearchProvider;
import fr.paris.lutece.plugins.mylutece.modules.users.service.MyLuteceUserSearchService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;

/**
 * Service for searching external users via the MyLutece user provider
 */
public final class ExternalUserSearchService
{
    private final boolean _bIsAvailable;

    /**
     * Private constructor for singleton pattern
     */
    private ExternalUserSearchService( )
    {
        boolean bAvailable = false;
        try
        {
            IUserSearchProvider provider = MyLuteceUserSearchService.getInstance( );
            bAvailable = provider != null;
            AppLogService.info( "Using user provider: " + ( provider != null ? provider.getClass( ).getName( ) : "none" ) );
        }
        catch( Exception e )
        {
            AppLogService.error( "Error checking if user provider is available", e );
        }
        _bIsAvailable = bAvailable;
    }

    private static class SingletonHolder
    {
        static final ExternalUserSearchService INSTANCE = new ExternalUserSearchService( );
    }

    /**
     * Gets the singleton instance of ExternalUserSearchService
     *
     * @return the singleton instance
     */
    public static ExternalUserSearchService getInstance( )
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Search for users matching the given criteria
     *
     * @param strLastName
     *            the last name (can be null or empty)
     * @param strGivenName
     *            the given name (can be null or empty)
     * @param strEmail
     *            the email (can be null or empty)
     * @return the list of matching users
     */
    public List<MyLuteceSearchUser> searchUsers( String strLastName, String strGivenName, String strEmail )
    {
        return searchUsers( strLastName, strGivenName, strEmail, null );
    }

    /**
     * Search for users matching the given criteria including additional attributes. Null criteria
     * are passed to the provider as empty strings: some providers, LDAP among them, reject null.
     *
     * @param strLastName
     *            the last name (can be null or empty)
     * @param strGivenName
     *            the given name (can be null or empty)
     * @param strEmail
     *            the email (can be null or empty)
     * @param listProviderAttributes
     *            additional provider attributes to search by (can be null)
     * @return the list of matching users
     */
    public List<MyLuteceSearchUser> searchUsers( String strLastName, String strGivenName, String strEmail, ReferenceList listProviderAttributes )
    {
        if ( !_bIsAvailable )
        {
            return new ArrayList<>( );
        }

        try
        {
            IUserSearchProvider provider = MyLuteceUserSearchService.getInstance( );
            if ( provider != null )
            {
                List<MyLuteceSearchUser> users = provider.findUsers( StringUtils.defaultString( strLastName ), StringUtils.defaultString( strGivenName ),
                        StringUtils.defaultString( strEmail ), listProviderAttributes != null ? listProviderAttributes : new ReferenceList( ) );
                return users != null ? users : new ArrayList<>( );
            }
            else
            {
                AppLogService.error( "No user info provider configured for mylutece-users" );
            }
        }
        catch( Exception e )
        {
            AppLogService.error( "Error while searching for external users", e );
        }

        return new ArrayList<>( );
    }

    /**
     * Check if external user search is available
     *
     * @return true if a user provider is configured, false otherwise
     */
    public boolean isAvailable( )
    {
        return _bIsAvailable;
    }

    /**
     * Get a user by provider user ID
     *
     * @param strProviderUserId
     *            the provider user ID
     * @return the user, or null if not found
     */
    public MyLuteceSearchUser getUserByProviderUserId( String strProviderUserId )
    {
        if ( !_bIsAvailable || strProviderUserId == null || strProviderUserId.isEmpty( ) )
        {
            return null;
        }

        try
        {
            IUserSearchProvider provider = MyLuteceUserSearchService.getInstance( );
            if ( provider != null )
            {
                return provider.getUserById( strProviderUserId );
            }
        }
        catch( Exception e )
        {
            AppLogService.error( "Error while getting user by provider user ID", e );
        }

        return null;
    }

    /**
     * Get the distinct combinations of values the given attributes take in the user directory.
     * Providers unable to enumerate them return an empty list, in which case the attributes simply
     * get no suggestion.
     *
     * @param listAttributeNames
     *            the provider attribute names to enumerate together
     * @return the distinct combinations, empty when unavailable
     */
    public List<Map<String, String>> getAttributeValues( List<String> listAttributeNames )
    {
        if ( !_bIsAvailable || listAttributeNames == null || listAttributeNames.isEmpty( ) )
        {
            return new ArrayList<>( );
        }

        try
        {
            IUserSearchProvider provider = MyLuteceUserSearchService.getInstance( );
            if ( provider != null )
            {
                List<Map<String, String>> values = provider.getAttributeValues( listAttributeNames );
                return values != null ? values : new ArrayList<>( );
            }
        }
        catch( Exception e )
        {
            AppLogService.error( "Error while getting the values of attributes " + listAttributeNames, e );
        }

        return new ArrayList<>( );
    }

    /**
     * Get several users by provider user IDs, in a single provider call
     *
     * @param listProviderUserIds
     *            the provider user IDs
     * @return the users found, empty when none matches or the service is unavailable
     */
    public List<MyLuteceSearchUser> getUsersByProviderUserIds( List<String> listProviderUserIds )
    {
        if ( !_bIsAvailable || listProviderUserIds == null || listProviderUserIds.isEmpty( ) )
        {
            return new ArrayList<>( );
        }

        try
        {
            IUserSearchProvider provider = MyLuteceUserSearchService.getInstance( );
            if ( provider != null )
            {
                List<MyLuteceSearchUser> users = provider.getUsersByIds( listProviderUserIds );
                return users != null ? users : new ArrayList<>( );
            }
        }
        catch( Exception e )
        {
            AppLogService.error( "Error while getting users by provider user IDs", e );
        }

        return new ArrayList<>( );
    }

}
