/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
package fr.paris.lutece.plugins.wiki.utils.auth;

import fr.paris.lutece.portal.service.security.LoginRedirectException;
import fr.paris.lutece.portal.service.security.LuteceAuthentication;
import fr.paris.lutece.portal.service.security.LuteceUser;

import java.util.Collection;

import javax.security.auth.login.LoginException;

import javax.servlet.http.HttpServletRequest;


/**
 * WikiAnonymousAuthentication
 */
public class WikiAnonymousAuthentication implements LuteceAuthentication
{
    /**
     * {@inheritDoc }
     */
    @Override
    public String getAuthServiceName(  )
    {
        return "Anonymous Authentication";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getAuthType( HttpServletRequest hsr )
    {
        return "Anonymous Authentication";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LuteceUser login( String string, String string1, HttpServletRequest hsr )
        throws LoginException, LoginRedirectException
    {
        return new WikiAnonymousUser(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void logout( LuteceUser lu )
    {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LuteceUser getAnonymousUser(  )
    {
        return new WikiAnonymousUser(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isUserInRole( LuteceUser lu, HttpServletRequest hsr, String string )
    {
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String[] getRolesByUser( LuteceUser lu )
    {
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isExternalAuthentication(  )
    {
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isDelegatedAuthentication(  )
    {
        return false;
    }

    /**
     * {@inheritDoc }
     * @param hsr
     */
    @Override
    public LuteceUser getHttpAuthenticatedUser( HttpServletRequest hsr )
    {
        return new WikiAnonymousUser(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getLoginPageUrl(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getDoLoginUrl(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getDoLogoutUrl(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getNewAccountPageUrl(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getViewAccountPageUrl(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getLostPasswordPageUrl(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getAccessDeniedTemplate(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getAccessControledTemplate(  )
    {
        return "";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isUsersListAvailable(  )
    {
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<LuteceUser> getUsers(  )
    {
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LuteceUser getUser( String string )
    {
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isMultiAuthenticationSupported(  )
    {
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getIconUrl(  )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName(  )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getPluginName(  )
    {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean findResetPassword( HttpServletRequest request, String strLogin )
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResetPasswordPageUrl( HttpServletRequest request )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLostLoginPageUrl(  )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDateLastLogin( LuteceUser user, HttpServletRequest request )
    {
    }
}
