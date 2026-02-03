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
package fr.paris.lutece.plugins.wiki.rs;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.wiki.business.item.WikiItemHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * WikiCodeValidationRest
 */
@ApplicationScoped
@Path( "wiki/validate_code" )
public class WikiCodeValidationRest
{
    private static final String PARAMETER_CODE = "code";
    private static final String PROPERTY_CODE_PATTERN = "wiki.code.pattern";
    private static final String DEFAULT_CODE_PATTERN = "^[a-zA-Z0-9_-]+$";

    @Context
    private HttpServletRequest _request;

    /**
     * Validate wiki page code
     *
     * @param strCode
     *            the code to validate
     * @return Response containing validation result
     */
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response validateCode( @QueryParam( PARAMETER_CODE ) String strCode )
    {
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( _request );
        if ( user == null )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).build( );
        }

        Map<String, Object> result = new HashMap<>( );
        result.put( "valid", true );

        if ( StringUtils.isBlank( strCode ) )
        {
            result.put( "valid", false );
            result.put( "message", I18nService.getLocalizedString( "wiki.validation.wikiitem.code.notEmpty", _request.getLocale( ) ) );
            return Response.ok( result ).build( );
        }

        String strPattern = AppPropertiesService.getProperty( PROPERTY_CODE_PATTERN, DEFAULT_CODE_PATTERN );
        if ( !Pattern.matches( strPattern, strCode ) )
        {
            result.put( "valid", false );
            result.put( "message", I18nService.getLocalizedString( "wiki.validation.wikiitem.code.pattern", _request.getLocale( ) ) );
            return Response.ok( result ).build( );
        }

        if ( WikiItemHome.findByCode( strCode ).isPresent( ) )
        {
            result.put( "valid", false );
            result.put( "message", I18nService.getLocalizedString( "wiki.validation.wikiitem.code.duplicate", new String [ ] {
                    strCode
            }, _request.getLocale( ) ) );
            return Response.ok( result ).build( );
        }

        return Response.ok( result ).build( );
    }
}
