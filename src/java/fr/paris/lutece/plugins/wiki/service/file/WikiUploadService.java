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
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNERS OR CONTRIBUTORS BE
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

import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * Upload settings of the wiki.
 */
public final class WikiUploadService
{
    private static final String PROPERTY_MAX_SIZE = "wiki.upload.maxSize";
    private static final long DEFAULT_MAX_SIZE = 52_428_800L;

    /**
     * Private constructor.
     */
    private WikiUploadService( )
    {
    }

    /**
     * Returns the maximum size of an uploaded file.
     *
     * @return the maximum size, in bytes
     */
    public static long getMaxSize( )
    {
        return AppPropertiesService.getPropertyLong( PROPERTY_MAX_SIZE, DEFAULT_MAX_SIZE );
    }

    /**
     * Tells whether a file exceeds the maximum upload size.
     *
     * @param lSize
     *            the file size, in bytes
     * @return true when the file is too large
     */
    public static boolean isTooLarge( long lSize )
    {
        return lSize > getMaxSize( );
    }
}
