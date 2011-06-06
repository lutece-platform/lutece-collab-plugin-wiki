/*
 * Copyright (c) 2002-2011, Mairie de Paris
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


/**
 * This is the business class for the object Topic
 */
public class Topic
{
    // Variables declarations 
    private int _nIdTopic;
    private int _nNamespace;
    private String _strPageName;

    /**
     * Returns the IdTopic
     * @return The IdTopic
     */
    public int getIdTopic(  )
    {
        return _nIdTopic;
    }

    /**
     * Sets the IdTopic
     * @param nIdTopic The IdTopic
     */
    public void setIdTopic( int nIdTopic )
    {
        _nIdTopic = nIdTopic;
    }

    /**
     * Returns the Namespace
     * @return The Namespace
     */
    public int getNamespace(  )
    {
        return _nNamespace;
    }

    /**
     * Sets the Namespace
     * @param nNamespace The Namespace
     */
    public void setNamespace( int nNamespace )
    {
        _nNamespace = nNamespace;
    }

    /**
     * Returns the PageName
     * @return The PageName
     */
    public String getPageName(  )
    {
        return _strPageName;
    }

    /**
     * Sets the PageName
     * @param strPageName The PageName
     */
    public void setPageName( String strPageName )
    {
        _strPageName = strPageName;
    }
}
