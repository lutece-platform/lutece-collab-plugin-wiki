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
package fr.paris.lutece.plugins.wiki.service.rbac;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemType;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemUserPermission;
import fr.paris.lutece.plugins.wiki.service.WikiPlugin;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;
import java.util.Locale;

public abstract class AbstractWikiResourceIdService extends ResourceIdService
{
    private static final String PROPERTY_LABEL_VIEW = "wiki.permission.label.view";
    private static final String PROPERTY_LABEL_EDIT = "wiki.permission.label.edit";

    protected AbstractWikiResourceIdService( )
    {
        super( );
        setPluginName( WikiPlugin.PLUGIN_NAME );
    }

    protected abstract WikiItemType getWikiItemType( );

    protected abstract String getResourceTypeLabelKey( );

    @Override
    public void register( )
    {
        WikiItemType itemType = getWikiItemType( );

        ResourceType resourceType = new ResourceType( );
        resourceType.setResourceIdServiceClass( getClass( ).getName( ) );
        resourceType.setPluginName( WikiPlugin.PLUGIN_NAME );
        resourceType.setResourceTypeKey( itemType.getCode( ) );
        resourceType.setResourceTypeLabelKey( getResourceTypeLabelKey( ) );

        Permission permissionView = new Permission( );
        permissionView.setPermissionKey( WikiItemUserPermission.PERMISSION_VIEW );
        permissionView.setPermissionTitleKey( PROPERTY_LABEL_VIEW );
        resourceType.registerPermission( permissionView );

        Permission permissionEdit = new Permission( );
        permissionEdit.setPermissionKey( WikiItemUserPermission.PERMISSION_EDIT );
        permissionEdit.setPermissionTitleKey( PROPERTY_LABEL_EDIT );
        resourceType.registerPermission( permissionEdit );

        ResourceTypeManager.registerResourceType( resourceType );
    }

    @Override
    public ReferenceList getResourceIdList( Locale locale )
    {
        List<AbstractWikiItem> listItems = WikiItemHome.getWikiItemsByType( getWikiItemType( ) );
        ReferenceList referenceList = new ReferenceList( );

        for ( AbstractWikiItem item : listItems )
        {
            ReferenceItem referenceItem = new ReferenceItem( );
            referenceItem.setCode( String.valueOf( item.getId( ) ) );
            referenceItem.setName( item.getCode( ) );
            referenceList.add( referenceItem );
        }

        return referenceList;
    }

    @Override
    public String getTitle( String strId, Locale locale )
    {
        try
        {
            int nId = Integer.parseInt( strId );
            return WikiItemHome.findByPrimaryKey( nId ).map( AbstractWikiItem::getCode ).orElse( "" );
        }
        catch( NumberFormatException e )
        {
            AppLogService.error( "Invalid " + getWikiItemType( ).name( ).toLowerCase( ) + " id: " + strId, e );
            return "";
        }
    }
}
