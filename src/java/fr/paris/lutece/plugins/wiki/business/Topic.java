/*
 * Copyright (c) 2002-2023, City of Paris
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
package fr.paris.lutece.plugins.wiki.business;

import fr.paris.lutece.portal.business.page.Page;
import fr.paris.lutece.portal.service.resource.IExtendableResource;

import java.sql.Timestamp;

/**
 * This is the business class for the object Topic
 */
public class Topic implements IExtendableResource
{
    public static final String RESOURCE_TYPE = "WIKI";

    // Variables declarations
    private int _nIdTopic;
    private int _nNamespace;
    private String _strPageName;
    private String _strViewRole = Page.ROLE_NONE;
    private String _strEditRole = Page.ROLE_NONE;
    private String _strParentPageName;
    private String _strEditingUser;
    private Timestamp _dtLastUpdate;


    /**
     * Returns the IdTopic
     * 
     * @return The IdTopic
     */
    public int getIdTopic( )
    {
        return _nIdTopic;
    }

    /**
     * Sets the IdTopic
     * 
     * @param nIdTopic
     *            The IdTopic
     */
    public void setIdTopic( int nIdTopic )
    {
        _nIdTopic = nIdTopic;
    }

    /**
     * Returns the Namespace
     * 
     * @return The Namespace
     */
    public int getNamespace( )
    {
        return _nNamespace;
    }

    /**
     * Sets the Namespace
     * 
     * @param nNamespace
     *            The Namespace
     */
    public void setNamespace( int nNamespace )
    {
        _nNamespace = nNamespace;
    }

    /**
     * Returns the PageName
     * 
     * @return The PageName
     */
    public String getPageName( )
    {
        return _strPageName;
    }

    /**
     * Sets the PageName
     * 
     * @param strPageName
     *            The PageName
     */
    public void setPageName( String strPageName )
    {
        _strPageName = strPageName;
    }

    /**
     * Returns the ViewRole
     * 
     * @return The ViewRole
     */
    public String getViewRole( )
    {
        return _strViewRole;
    }

    /**
     * Sets the ViewRole
     * 
     * @param strViewRole
     *            The ViewRole
     */
    public void setViewRole( String strViewRole )
    {
        _strViewRole = strViewRole;
    }

    /**
     * Returns the EditRole
     * 
     * @return The EditRole
     */
    public String getEditRole( )
    {
        return _strEditRole;
    }

    /**
     * Sets the EditRole
     * 
     * @param strEditRole
     *            The EditRole
     */
    public void setEditRole( String strEditRole )
    {
        _strEditRole = strEditRole;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getIdExtendableResource( )
    {
        return Integer.toString( _nIdTopic );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getExtendableResourceType( )
    {
        return RESOURCE_TYPE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getExtendableResourceName( )
    {
        return _strPageName;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getExtendableResourceDescription( )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getExtendableResourceImageUrl( )
    {
        return null;
    }

    /**
     * Returns the ParentPageName
     * 
     * @return The ParentPageName
     */
    public String getParentPageName( )
    {
        return _strParentPageName;
    }

    /**
     * Sets the ParentPageName
     * 
     * @param strParentPageName
     *            The ParentPageName
     */
    public void setParentPageName( String strParentPageName )
    {
        _strParentPageName = strParentPageName;
    }

    /**
     * Returns the ModifyPageOpenLastBy
     *
     * @return The ModifyPageOpenLastBy
     */
    public String setEditingUser( )
    {
        return _strEditingUser;
    }

    /**
     * Sets _strEditingUser
     *
     * @param editingUser
     */
    public void setEditingUser(String editingUser )
    {
        _strEditingUser = editingUser;
    }

    /**
     * Returns the _dtLastUpdate
     *
     * @return The _dtLastUpdate
     */
    public Timestamp getLastUpdate( )
    {
        return _dtLastUpdate;
    }

    /**
     * Sets the ModifyPageOpenAt
     *
     * @param lastUpdate
     *            The ModifyPageOpenAt
     */
    public void setLastUpdate(Timestamp lastUpdate )
    {
        _dtLastUpdate = lastUpdate;
    }
}
