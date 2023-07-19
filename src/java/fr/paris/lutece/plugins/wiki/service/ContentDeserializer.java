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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ContentDeserializer
{
    private static final long serialVersionUID = -2287035947644920508L;
    private Integer topicVersion;
    private String parentPageName;
    private int topicId;
    private String topicTitle;

    private String topicContent;

    private String language;

    private String wikiHtmlContent;

    private String wikiPageUrl;

    private String createVersion;

    private String publishVersion;

    private String editComment;

    private String viewRole;

    private String editRole;

    private String topicPageName;


    public Integer getTopicVersion( )
    {
        return this.topicVersion;
    }

    public int getTopicId( )
    {
        return this.topicId;
    }

    public String getTopicTitle( )
    {
        return  this.topicTitle;
    }

    public String getTopicContent( )
    {
        return  this.topicContent;
    }

    public String getParentPageName( )
    {
        return  this.parentPageName;
    }

    public String getLanguage( )
    {
        return  this.language;
    }

    public String getWikiHtmlContent( )
    {
        return  this.wikiHtmlContent;
    }

    public String getWikiPageUrl( )
    {
        return  this.wikiPageUrl;
    }

    public Boolean getIsCreateVersion( )
    {
        return Boolean.parseBoolean(  this.createVersion );
    }

    public Boolean getIsPublishVersion( )
    {
        return Boolean.parseBoolean(  this.publishVersion );
    }

    public String getEditComment( )
    {
        return  this.editComment;
    }

    public String getViewRole( )
    {
        return  this.viewRole;
    }

    public String getEditRole( )
    {
        return  this.editRole;
    }

    public String getTopicPageName( )
    {
        return  this.topicPageName;
    }

    public static ContentDeserializer deserializeWikiContent( String requestBody )
    {
        final Gson gson = new GsonBuilder( ).create( );
        final ContentDeserializer content = gson.fromJson( requestBody, ContentDeserializer.class );
        return content;
    }
}
