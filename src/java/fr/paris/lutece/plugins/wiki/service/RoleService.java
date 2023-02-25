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
package fr.paris.lutece.plugins.wiki.service;

import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.portal.business.page.Page;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

import javax.servlet.http.HttpServletRequest;

/**
 * Diff Service
 */
public final class RoleService
{
    private static final String DSKEY_ROLE_ADMIN = "wiki.site_property.role.admin";
    private static final String DEFAULT_ROLE_ADMIN = "wiki_admin";
    private static final String PROPERTY_DEFAULT_ROLE_CODE = "defaultRole.code";

    /** Private constuctor */
    private RoleService( )
    {
    }

    /**
     * Gets all the user's role
     * 
     * @param request
     *            The request
     * @return the reference list of all the user's role
     */
    public static ReferenceList getUserRoles( HttpServletRequest request )
    {
        ReferenceList refListRole = RoleHome.getRolesList( );

        if ( SecurityService.isAuthenticationEnable( ) )
        {
            ReferenceList refListUserRole = new ReferenceList( );

            for ( ReferenceItem item : refListRole )
            {
                if ( SecurityService.getInstance( ).isUserInRole( request, item.getCode( ) )
                        || item.getCode( ).equals( AppPropertiesService.getProperty( PROPERTY_DEFAULT_ROLE_CODE ) ) )
                {
                    refListUserRole.add( item );
                }
            }

            return refListUserRole;
        }

        return refListRole;
    }

    /**
     * Checks if the user has the edit role for the given topic
     * 
     * @param request
     *            The request
     * @param topic
     *            The topic
     * @return true if he has otherwise false
     */
    public static boolean hasEditRole( HttpServletRequest request, Topic topic )
    {
        if ( SecurityService.isAuthenticationEnable( ) )
        {
            if ( !Page.ROLE_NONE.equals( topic.getEditRole( ) ) )
            {
                return SecurityService.getInstance( ).isUserInRole( request, topic.getEditRole( ) );
            }
        }
        return true;
    }

    /**
     * Checks if the user has the admin role
     * 
     * @param request
     *            The HTTP request
     * @return true if he has otherwise false
     */
    public static boolean hasAdminRole( HttpServletRequest request )
    {
        if ( SecurityService.isAuthenticationEnable( ) )
        {
            String strAdminRole = DatastoreService.getDataValue( DSKEY_ROLE_ADMIN, DEFAULT_ROLE_ADMIN );
            return SecurityService.getInstance( ).isUserInRole( request, strAdminRole );
        }
        return false;
    }
}
