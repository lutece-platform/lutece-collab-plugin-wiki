/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.permission;

import java.io.Serializable;

/**
 * Permission granted to a single user on a wiki item.
 */
public class WikiItemUserPermission implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String PERMISSION_VIEW = "VIEW";
    public static final String PERMISSION_EDIT = "EDIT";

    private int _nIdItem;
    private String _strUserGuid;
    private String _strUserDisplayName;
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
     * Returns the user identifier, as returned by LuteceUser#getName.
     *
     * @return the user guid
     */
    public String getUserGuid( )
    {
        return _strUserGuid;
    }

    /**
     * Sets the user identifier.
     *
     * @param strUserGuid
     *            the user guid
     */
    public void setUserGuid( String strUserGuid )
    {
        _strUserGuid = strUserGuid;
    }

    /**
     * Returns the user name to display.
     *
     * @return the display name
     */
    public String getUserDisplayName( )
    {
        return _strUserDisplayName;
    }

    /**
     * Sets the user name to display.
     *
     * @param strUserDisplayName
     *            the display name
     */
    public void setUserDisplayName( String strUserDisplayName )
    {
        _strUserDisplayName = strUserDisplayName;
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
     * Sets the permission type, VIEW or EDIT.
     *
     * @param strPermissionType
     *            the permission type
     */
    public void setPermissionType( String strPermissionType )
    {
        _strPermissionType = strPermissionType;
    }

}
