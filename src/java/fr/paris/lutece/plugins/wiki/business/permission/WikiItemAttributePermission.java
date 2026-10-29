/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.permission;

/**
 * A permission granted to everyone sharing a directory attribute value, rather than to named users.
 *
 * A population such as "everyone in DSIN" changes as people join and leave, so listing its members
 * would go stale the day it is written. The rule is stored instead, and evaluated against the
 * attributes the signed-in user carries.
 */
public class WikiItemAttributePermission
{
    private int _nIdItem;
    private String _strAttributeName;
    private String _strAttributeValue;
    private String _strAttributeLabel;
    private String _strPermissionType;

    /**
     * Returns the wiki item identifier.
     *
     * @return the item identifier
     */
    public int getIdItem( )
    {
        return _nIdItem;
    }

    /**
     * Sets the wiki item identifier.
     *
     * @param nIdItem
     *            the item identifier
     */
    public void setIdItem( int nIdItem )
    {
        _nIdItem = nIdItem;
    }

    /**
     * Returns the directory attribute the rule applies to.
     *
     * @return the attribute name
     */
    public String getAttributeName( )
    {
        return _strAttributeName;
    }

    /**
     * Sets the directory attribute the rule applies to.
     *
     * @param strAttributeName
     *            the attribute name
     */
    public void setAttributeName( String strAttributeName )
    {
        _strAttributeName = strAttributeName;
    }

    /**
     * Returns the attribute value a user must carry to be granted.
     *
     * @return the attribute value
     */
    public String getAttributeValue( )
    {
        return _strAttributeValue;
    }

    /**
     * Sets the attribute value a user must carry to be granted.
     *
     * @param strAttributeValue
     *            the attribute value
     */
    public void setAttributeValue( String strAttributeValue )
    {
        _strAttributeValue = strAttributeValue;
    }

    /**
     * Returns the label of the attribute, as shown to whoever granted the rule.
     *
     * @return the attribute label
     */
    public String getAttributeLabel( )
    {
        return _strAttributeLabel;
    }

    /**
     * Sets the label of the attribute.
     *
     * @param strAttributeLabel
     *            the attribute label
     */
    public void setAttributeLabel( String strAttributeLabel )
    {
        _strAttributeLabel = strAttributeLabel;
    }

    /**
     * Returns the permission type, VIEW or EDIT.
     *
     * @return the permission type
     */
    public String getPermissionType( )
    {
        return _strPermissionType;
    }

    /**
     * Sets the permission type.
     *
     * @param strPermissionType
     *            the permission type
     */
    public void setPermissionType( String strPermissionType )
    {
        _strPermissionType = strPermissionType;
    }
}
