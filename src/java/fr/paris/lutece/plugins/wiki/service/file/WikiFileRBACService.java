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
package fr.paris.lutece.plugins.wiki.service.file;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.IFileRBACService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.util.AppLogService;

@ApplicationScoped
@Named( "wiki.wikiFileRBACService" )
public class WikiFileRBACService implements IFileRBACService
{
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_UNAUTHORIZED = "Unauthorized access to wiki file";
    private static final String MESSAGE_MISSING_RESOURCE_ID = "Wiki file access: missing resource_id";
    private static final String MESSAGE_INVALID_RESOURCE_ID = "Wiki file access: invalid resource_id ";
    private static final String MESSAGE_ITEM_NOT_FOUND = "Wiki file access: item not found ";

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAccessRights( Map<String, String> fileData, User user ) throws AccessDeniedException, UserNotSignedException
    {
        String strResourceId = fileData.get( FileService.PARAMETER_RESOURCE_ID );

        if ( strResourceId == null || strResourceId.isEmpty( ) )
        {
            AppLogService.error( MESSAGE_MISSING_RESOURCE_ID );
            throw new AccessDeniedException( MESSAGE_UNAUTHORIZED );
        }

        int nItemId;
        try
        {
            nItemId = Integer.parseInt( strResourceId );
        }
        catch( NumberFormatException e )
        {
            AppLogService.error( "{}{}", MESSAGE_INVALID_RESOURCE_ID, strResourceId, e );
            throw new AccessDeniedException( MESSAGE_UNAUTHORIZED );
        }

        AbstractWikiItem item = WikiItemHome.findByPrimaryKey( nItemId ).orElse( null );

        if ( item == null )
        {
            AppLogService.error( "{}{}", MESSAGE_ITEM_NOT_FOUND, nItemId );
            throw new AccessDeniedException( MESSAGE_UNAUTHORIZED );
        }

        LuteceUser luteceUser = user instanceof LuteceUser ? (LuteceUser) user : null;

        if ( !WikiAccessControlService.canView( luteceUser, item ) )
        {
            if ( luteceUser == null && !item.isPublished( ) )
            {
                throw new UserNotSignedException( );
            }
            throw new AccessDeniedException( MESSAGE_UNAUTHORIZED );
        }
    }
}
