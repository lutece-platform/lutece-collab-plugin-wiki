/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.user;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.mylutece.service.search.MyLuteceSearchUser;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceItem;

/**
 * Reads the directory attributes a signed-in user carries, from the information the authentication
 * put in their session. Nothing is asked to the directory: an access check must stay cheap.
 *
 * The authentication stores those attributes under its own keys, so an attribute is looked up by the
 * last segment of the key, which the mapping usually names after the directory attribute itself. A
 * property declares the exceptions:
 *
 * <pre>
 * wiki.userAttribute.alias.mdpDirOperationnelle=user.business-info.organizationUnit
 * </pre>
 */
public final class WikiUserAttributes
{
    private static final String PROPERTY_ALIAS_PREFIX = "wiki.userAttribute.alias.";
    private static final String PROPERTY_CACHE_DURATION = "wiki.userAttribute.cacheDurationSeconds";
    private static final int DEFAULT_CACHE_DURATION = 1800;

    private static final Map<String, CachedAttributes> _mapCache = new ConcurrentHashMap<>( );

    /**
     * Private constructor.
     */
    private WikiUserAttributes( )
    {
    }

    /**
     * Returns the values a user carries for every searchable attribute, ready to be matched against
     * the rules granting a permission to a whole population.
     *
     * Attributes the session does not carry are read from the directory once and remembered for a
     * while: an authentication rarely maps every attribute, and the levels it leaves out are exactly
     * the ones holding the finest affectations.
     *
     * @param user
     *            the signed-in user
     * @return the values keyed by attribute name, attributes the user does not carry left out
     */
    public static Map<String, String> getValues( LuteceUser user )
    {
        Map<String, String> mapValues = new LinkedHashMap<>( );

        if ( user == null )
        {
            return mapValues;
        }

        Map<String, String> mapFromDirectory = null;

        for ( String strAttributeName : WikiUserAttributeValuesService.getAttributeNames( ) )
        {
            String strAttribute = strAttributeName.trim( );
            String strValue = getValue( user, strAttribute );

            if ( strValue == null )
            {
                if ( mapFromDirectory == null )
                {
                    mapFromDirectory = readFromDirectory( user.getName( ) );
                }
                strValue = mapFromDirectory.get( strAttribute );
            }

            if ( strValue != null )
            {
                mapValues.put( strAttribute, strValue );
            }
        }

        return mapValues;
    }

    /**
     * Returns the value a user session carries for a directory attribute.
     *
     * @param user
     *            the signed-in user
     * @param strAttribute
     *            the directory attribute name, trimmed
     * @return the value, null when the session carries none
     */
    private static String getValue( LuteceUser user, String strAttribute )
    {
        Map<String, String> mapInfos = user.getUserInfos( );

        if ( strAttribute.isEmpty( ) || mapInfos == null || mapInfos.isEmpty( ) )
        {
            return null;
        }

        String strAlias = AppPropertiesService.getProperty( PROPERTY_ALIAS_PREFIX + strAttribute );

        if ( strAlias != null && !strAlias.isBlank( ) )
        {
            return StringUtils.trimToNull( mapInfos.get( strAlias.trim( ) ) );
        }

        for ( Map.Entry<String, String> entry : mapInfos.entrySet( ) )
        {
            if ( lastSegment( entry.getKey( ) ).equalsIgnoreCase( strAttribute ) )
            {
                return StringUtils.trimToNull( entry.getValue( ) );
            }
        }

        return null;
    }

    /**
     * Returns the attributes the directory holds for a user, remembered for a while so an access
     * check never waits on the directory twice.
     *
     * @param strUserGuid
     *            the user identifier
     * @return the attribute values keyed by attribute name, empty when the user is unknown
     */
    private static Map<String, String> readFromDirectory( String strUserGuid )
    {
        if ( strUserGuid == null || strUserGuid.isBlank( ) )
        {
            return Collections.emptyMap( );
        }

        CachedAttributes cached = _mapCache.get( strUserGuid );

        if ( cached != null && !cached.isExpired( ) )
        {
            return cached.getAttributes( );
        }

        Map<String, String> mapAttributes = new LinkedHashMap<>( );
        MyLuteceSearchUser directoryUser = ExternalUserSearchService.getInstance( ).getUserByProviderUserId( strUserGuid );

        if ( directoryUser != null && directoryUser.getAttributes( ) != null )
        {
            for ( ReferenceItem item : directoryUser.getAttributes( ) )
            {
                if ( item.getName( ) != null && item.getCode( ) != null && !item.getCode( ).isBlank( ) )
                {
                    mapAttributes.put( item.getName( ).trim( ), item.getCode( ).trim( ) );
                }
            }
        }

        _mapCache.put( strUserGuid, new CachedAttributes( mapAttributes ) );
        return mapAttributes;
    }

    /**
     * Returns the last dot separated segment of a key.
     *
     * @param strKey
     *            the key
     * @return the last segment, the key itself when it has no dot
     */
    private static String lastSegment( String strKey )
    {
        return strKey != null ? strKey.substring( strKey.lastIndexOf( '.' ) + 1 ) : "";
    }

    /**
     * The attributes read from the directory for one user, and when they were read. An access check
     * happens on nearly every request, so it must not wait on the directory each time.
     */
    private static final class CachedAttributes
    {
        private final Map<String, String> _mapAttributes;
        private final long _lReadAt;

        /**
         * Remembers the attributes as of now.
         *
         * @param mapAttributes
         *            the attributes read
         */
        private CachedAttributes( Map<String, String> mapAttributes )
        {
            _mapAttributes = mapAttributes;
            _lReadAt = System.currentTimeMillis( );
        }

        /**
         * Returns the remembered attributes.
         *
         * @return the attribute values keyed by attribute name
         */
        Map<String, String> getAttributes( )
        {
            return _mapAttributes;
        }

        /**
         * Tells whether the attributes are old enough to be read again.
         *
         * @return true when they expired
         */
        boolean isExpired( )
        {
            long lDuration = AppPropertiesService.getPropertyInt( PROPERTY_CACHE_DURATION, DEFAULT_CACHE_DURATION ) * 1000L;
            return System.currentTimeMillis( ) - _lReadAt > lDuration;
        }
    }
}
