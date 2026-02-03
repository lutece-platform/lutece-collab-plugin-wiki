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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.FileServiceException;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.upload.MultipartItem;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.web.upload.IAsynchronousUploadHandler2;

@ApplicationScoped
public class WikiAsynchronousUploadHandler implements IAsynchronousUploadHandler2
{
    @Inject
    @Named( "wiki.fileStoreServiceProvider" )
    private IFileStoreServiceProvider _fileStoreProvider;

    private static final String HANDLER_NAME = "wikiAsynchronousUploadHandler";
    private static final String PARAMETER_HANDLER = "handler";
    private static final String PARAMETER_ITEM_ID = "itemId";
    private static final String RESOURCE_TYPE_WIKI = "WIKI";
    private static final String JSON_KEY_FILE_ID = "fileId";
    private static final String JSON_KEY_FILE_URL = "fileUrl";
    private static final String JSON_KEY_SUCCESS = "success";
    private static final String JSON_KEY_ERROR = "error";
    private static final String JSON_KEY_FILES = "uploadedFiles";
    private static final String JSON_KEY_FILE_NAME = "fileName";
    private static final String ERROR_MESSAGE_FAILED_STORE_FILE = "Failed to store file: ";
    private static final String ERROR_MESSAGE_NO_FILE = "No file provided";
    private static final String ERROR_MESSAGE_NO_ITEM_ID = "No item ID provided";
    private static final String ERROR_MESSAGE_ITEM_NOT_FOUND = "Wiki item not found";
    private static final String ERROR_MESSAGE_ACCESS_DENIED = "Access denied: you do not have permission to upload files to this item";

    /**
     * {@inheritDoc}
     */
    @Override
    public void process( HttpServletRequest request, HttpServletResponse response, Map<String, Object> mainObject, List<MultipartItem> fileItems )
    {
        String strItemId = request.getParameter( PARAMETER_ITEM_ID );

        if ( strItemId == null || strItemId.isEmpty( ) )
        {
            mainObject.put( JSON_KEY_SUCCESS, false );
            mainObject.put( JSON_KEY_ERROR, ERROR_MESSAGE_NO_ITEM_ID );
            return;
        }

        int nItemId;
        try
        {
            nItemId = Integer.parseInt( strItemId );
        }
        catch( NumberFormatException e )
        {
            mainObject.put( JSON_KEY_SUCCESS, false );
            mainObject.put( JSON_KEY_ERROR, ERROR_MESSAGE_NO_ITEM_ID );
            return;
        }

        Optional<AbstractWikiItem> optItem = WikiItemHome.findByPrimaryKey( nItemId );
        if ( !optItem.isPresent( ) )
        {
            mainObject.put( JSON_KEY_SUCCESS, false );
            mainObject.put( JSON_KEY_ERROR, ERROR_MESSAGE_ITEM_NOT_FOUND );
            return;
        }

        AbstractWikiItem item = optItem.get( );
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );
        if ( !WikiAccessControlService.canEdit( user, item ) )
        {
            mainObject.put( JSON_KEY_SUCCESS, false );
            mainObject.put( JSON_KEY_ERROR, ERROR_MESSAGE_ACCESS_DENIED );
            return;
        }

        if ( fileItems != null && !fileItems.isEmpty( ) )
        {
            List<Map<String, String>> uploadedFiles = new ArrayList<>( );
            boolean allSuccess = true;

            for ( MultipartItem fileItem : fileItems )
            {
                Map<String, String> fileResult = new HashMap<>( );

                try
                {
                    File file = new File( );
                    file.setTitle( fileItem.getName( ) );
                    file.setSize( (int) fileItem.getSize( ) );
                    file.setMimeType( fileItem.getContentType( ) );

                    PhysicalFile physicalFile = new PhysicalFile( );
                    physicalFile.setValue( fileItem.get( ) );
                    file.setPhysicalFile( physicalFile );

                    String strFileKey = _fileStoreProvider.storeFile( file );

                    Map<String, String> additionalData = new HashMap<>( );
                    additionalData.put( FileService.PARAMETER_RESOURCE_ID, String.valueOf( nItemId ) );
                    additionalData.put( FileService.PARAMETER_RESOURCE_TYPE, RESOURCE_TYPE_WIKI );

                    String strFileUrl = _fileStoreProvider.getFileDownloadUrlFO( strFileKey, additionalData );

                    fileResult.put( JSON_KEY_FILE_ID, strFileKey );
                    fileResult.put( JSON_KEY_FILE_URL, strFileUrl );
                    fileResult.put( JSON_KEY_FILE_NAME, fileItem.getName( ) );
                    uploadedFiles.add( fileResult );
                }
                catch( FileServiceException e )
                {
                    AppLogService.error( "{}{}", ERROR_MESSAGE_FAILED_STORE_FILE, fileItem.getName( ), e );
                    allSuccess = false;
                    fileResult.put( JSON_KEY_ERROR, ERROR_MESSAGE_FAILED_STORE_FILE + fileItem.getName( ) );
                    uploadedFiles.add( fileResult );
                }
            }

            mainObject.put( JSON_KEY_SUCCESS, allSuccess );
            mainObject.put( JSON_KEY_FILES, uploadedFiles );
        }
        else
        {
            mainObject.put( JSON_KEY_SUCCESS, false );
            mainObject.put( JSON_KEY_ERROR, ERROR_MESSAGE_NO_FILE );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInvoked( HttpServletRequest request )
    {
        String strHandler = request.getParameter( PARAMETER_HANDLER );
        return HANDLER_NAME.equals( strHandler );
    }
}
