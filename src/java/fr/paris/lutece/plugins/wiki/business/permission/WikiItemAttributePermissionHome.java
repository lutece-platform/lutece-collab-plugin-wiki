/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.permission;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;
import java.util.Map;

/**
 * Home for permissions granted on a directory attribute value.
 */
public final class WikiItemAttributePermissionHome
{
    private static final IWikiItemAttributePermissionDAO _dao = SpringContextService.getBean( "wiki.wikiItemAttributePermissionDAO" );
    private static final Plugin _plugin = PluginService.getPlugin( "wiki" );

    /**
     * Private constructor.
     */
    private WikiItemAttributePermissionHome( )
    {
    }

    /**
     * Grants a permission to everyone carrying an attribute value. Does nothing when already granted.
     *
     * @param nIdItem
     *            the item identifier
     * @param strAttributeName
     *            the attribute name
     * @param strAttributeValue
     *            the attribute value
     * @param strAttributeLabel
     *            the attribute label, as shown to whoever granted the rule
     * @param strPermissionType
     *            the permission type
     */
    public static void create( int nIdItem, String strAttributeName, String strAttributeValue, String strAttributeLabel, String strPermissionType )
    {
        WikiItemAttributePermission permission = new WikiItemAttributePermission( );
        permission.setIdItem( nIdItem );
        permission.setAttributeName( strAttributeName );
        permission.setAttributeValue( strAttributeValue );
        permission.setAttributeLabel( strAttributeLabel != null ? strAttributeLabel : strAttributeName );
        permission.setPermissionType( strPermissionType );
        _dao.insert( permission, _plugin );
    }

    /**
     * Revokes a rule of an item.
     *
     * @param nIdItem
     *            the item identifier
     * @param strAttributeName
     *            the attribute name
     * @param strAttributeValue
     *            the attribute value
     * @param strPermissionType
     *            the permission type
     */
    public static void remove( int nIdItem, String strAttributeName, String strAttributeValue, String strPermissionType )
    {
        _dao.delete( nIdItem, strAttributeName, strAttributeValue, strPermissionType, _plugin );
    }

    /**
     * Returns the rules of an item for a permission type.
     *
     * @param nIdItem
     *            the item identifier
     * @param strPermissionType
     *            the permission type
     * @return the rules, ordered by label
     */
    public static List<WikiItemAttributePermission> findByItemAndType( int nIdItem, String strPermissionType )
    {
        return _dao.selectByItemAndType( nIdItem, strPermissionType, _plugin );
    }

    /**
     * Returns every rule matching one of the attribute values a user carries.
     *
     * @param mapUserAttributes
     *            the attribute values the user carries, keyed by attribute name
     * @return the matching rules
     */
    public static List<WikiItemAttributePermission> findByAttributeValues( Map<String, String> mapUserAttributes )
    {
        return _dao.selectByAttributeValues( mapUserAttributes, _plugin );
    }
}
