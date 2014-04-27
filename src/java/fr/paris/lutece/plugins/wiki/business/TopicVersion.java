/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import java.sql.Timestamp;


/**
 * This is the business class for the object TopicVersion
 */
public class TopicVersion
{
    // Variables declarations 
    private int _nIdTopicVersion;
    private String _strEditComment;
    private int _nIdTopic;
    private String _strLuteceUserId;
    private Timestamp _strDateEdition;
    private int _nIdTopicVersionPrevious;
    private String _strWikiContent;
    private String _strUserPseudo;
    private String _strUserAvatarUrl;
    private String _strUserName;

    /**
     * Returns the IdTopicVersion
     * @return The IdTopicVersion
     */
    public int getIdTopicVersion(  )
    {
        return _nIdTopicVersion;
    }

    /**
     * Sets the IdTopicVersion
     * @param nIdTopicVersion The IdTopicVersion
     */
    public void setIdTopicVersion( int nIdTopicVersion )
    {
        _nIdTopicVersion = nIdTopicVersion;
    }

    /**
     * Returns the EditComment
     * @return The EditComment
     */
    public String getEditComment(  )
    {
        return _strEditComment;
    }

    /**
     * Sets the EditComment
     * @param strEditComment The EditComment
     */
    public void setEditComment( String strEditComment )
    {
        _strEditComment = strEditComment;
    }

    /**
     * Returns the EditComment
     * @return The EditComment
     */
    public String getWikiContent(  )
    {
        return _strWikiContent;
    }

    /**
     * Sets the EditComment
     * @param strWikiContent The content
     */
    public void setWikiContent( String strWikiContent )
    {
        _strWikiContent = strWikiContent;
    }

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
     * Returns the LuteceUserId
     * @return The LuteceUserId
     */
    public String getLuteceUserId(  )
    {
        return _strLuteceUserId;
    }

    /**
     * Sets the LuteceUserId
     * @param strLuteceUserId The LuteceUserId
     */
    public void setLuteceUserId( String strLuteceUserId )
    {
        _strLuteceUserId = strLuteceUserId;
    }

    /**
     * Returns the DateEdition
     * @return The DateEdition
     */
    public Timestamp getDateEdition(  )
    {
        return _strDateEdition;
    }

    /**
     * Sets the DateEdition
     * @param timestamp The DateEdition
     */
    public void setDateEdition( Timestamp timestamp )
    {
        _strDateEdition = timestamp;
    }

    /**
     * Returns the IdTopicVersionPrevious
     * @return The IdTopicVersionPrevious
     */
    public int getIdTopicVersionPrevious(  )
    {
        return _nIdTopicVersionPrevious;
    }

    /**
     * Sets the IdTopicVersionPrevious
     * @param nIdTopicVersionPrevious The IdTopicVersionPrevious
     */
    public void setIdTopicVersionPrevious( int nIdTopicVersionPrevious )
    {
        _nIdTopicVersionPrevious = nIdTopicVersionPrevious;
    }
    
    
    /**
     * Returns the UserPseudo
     * @return The UserPseudo
     */
    public String getUserPseudo(  )
    {
        return _strUserPseudo;
    }

    /**
     * Sets the UserPseudo
     * @param strUserPseudo The UserPseudo
     */
    public void setUserPseudo( String strUserPseudo )
    {
        _strUserPseudo = strUserPseudo;
    }

    /**
     * Returns the UserName
     * @return The UserName
     */
    public String getUserName(  )
    {
        return _strUserName;
    }

    /**
     * Sets the UserName
     * @param strUserName The UserName
     */
    public void setUserName( String strUserName )
    {
        _strUserName = strUserName;
    }

    /**
     * Returns the UserAvatarUrl
     * @return The UserAvatarUrl
     */
    public String getUserAvatarUrl(  )
    {
        return _strUserAvatarUrl;
    }

    /**
     * Sets the UserAvatarUrl
     * @param strUserAvatarUrl The UserAvatarUrl
     */
    public void setUserAvatarUrl( String strUserAvatarUrl )
    {
        _strUserAvatarUrl = strUserAvatarUrl;
    }



}
