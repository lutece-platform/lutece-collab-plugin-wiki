/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
package fr.paris.lutece.plugins.wiki.web;


/**
 * COnstants
 */
public final class Constants
{
    public static final String PLUGIN_NAME = "wiki";
    public static final String PARAMETER_ACTION_MODIFY = "modify";
    public static final String PARAMETER_ACTION_CREATE = "create";
    public static final String PARAMETER_ACTION_VIEW = "view";
    public static final String PARAMETER_PAGE_NAME = "page_name";
    public static final String PARAMETER_ACTION = "action";
    public static final String PARAMETER_CONTENT = "content";
    public static final String PARAMETER_MODIFICATION_COMMENT = "modification_comment";
    public static final String PARAMETER_PREVIOUS_VERSION_ID = "previous_version_id";
    public static final String PARAMETER_TOPIC_ID = "topic_id";
    public static final String PARAMETER_NEW_VERSION = "new_version";
    public static final String PARAMETER_OLD_VERSION = "old_version";
    public static final Object PARAMETER_ACTION_VIEW_DIFF = "view_diff";
    public static final String PARAMETER_QUERY = "query";
    public static final String PARAMETER_PAGE_INDEX = "page_index";
    public static final String PARAMETER_ACTION_VIEW_HISTORY = "view_history";
    public static final String PARAMETER_ACTION_SEARCH = "search";
    public static final String PARAMETER_ROLE = "role";
    public static final String MESSAGE_PAGE_NOT_EXISTS = "wiki.message.accessDenied.pageNotExists";
    public static final String MESSAGE_PAGE_ALREADY_EXISTS = "wiki.message.accessDenied.pageAlreadyExists";
    public static final String MESSAGE_USER_NOT_IN_ROLE = "wiki.message.accessDenied.userNotInRole";

    /** Private constructor */
    private Constants(  )
    {
    }
}
