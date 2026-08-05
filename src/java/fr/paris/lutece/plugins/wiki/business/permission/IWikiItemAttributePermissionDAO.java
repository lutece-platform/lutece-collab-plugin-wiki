/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.permission;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;
import java.util.Map;

/**
 * Data access interface for permissions granted on a directory attribute value.
 */
public interface IWikiItemAttributePermissionDAO
{
    /**
     * Inserts a rule, ignoring duplicates.
     *
     * @param permission
     *            the rule to insert
     * @param plugin
     *            the plugin
     */
    void insert( WikiItemAttributePermission permission, Plugin plugin );

    /**
     * Deletes one rule of an item.
     *
     * @param nIdItem
     *            the item identifier
     * @param strAttributeName
     *            the attribute name
     * @param strAttributeValue
     *            the attribute value
     * @param strPermissionType
     *            the permission type
     * @param plugin
     *            the plugin
     */
    void delete( int nIdItem, String strAttributeName, String strAttributeValue, String strPermissionType, Plugin plugin );

    /**
     * Selects the rules of an item for a permission type.
     *
     * @param nIdItem
     *            the item identifier
     * @param strPermissionType
     *            the permission type
     * @param plugin
     *            the plugin
     * @return the rules, ordered by label
     */
    List<WikiItemAttributePermission> selectByItemAndType( int nIdItem, String strPermissionType, Plugin plugin );

    /**
     * Selects every rule matching one of the attribute values a user carries. Meant to be called
     * once per user, so their whole set of rule-granted items is known in a single query.
     *
     * @param mapUserAttributes
     *            the attribute values the user carries, keyed by attribute name
     * @param plugin
     *            the plugin
     * @return the matching rules
     */
    List<WikiItemAttributePermission> selectByAttributeValues( Map<String, String> mapUserAttributes, Plugin plugin );
}
