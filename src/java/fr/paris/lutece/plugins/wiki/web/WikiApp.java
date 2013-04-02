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
package fr.paris.lutece.plugins.wiki.web;

import fr.paris.lutece.plugins.wiki.utils.auth.WikiAnonymousUser;
import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.plugins.wiki.business.TopicVersion;
import fr.paris.lutece.plugins.wiki.business.TopicVersionHome;
import fr.paris.lutece.plugins.wiki.utils.Constants;
import fr.paris.lutece.plugins.wiki.service.WikiDiff;
import fr.paris.lutece.plugins.wiki.service.parser.LuteceWikiParser;
import fr.paris.lutece.plugins.wiki.utils.DiffUtils;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.search.SearchEngine;
import fr.paris.lutece.portal.service.search.SearchResult;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.web.xpages.XPageApplication;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides a simple implementation of an XPage
 */
public class WikiApp implements XPageApplication
{
    public static final String PARAMETER_ACTION_MODIFY = "modify";
    public static final String PARAMETER_ACTION_CREATE = "create";
    public static final String PARAMETER_ACTION_VIEW = "view";

    private static final String PARAMETER_PAGE_NAME = "page_name";
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_CONTENT = "content";
    private static final String PARAMETER_PREVIEW = "preview";
    private static final String PARAMETER_QUERY = "query";
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    private static final String PARAMETER_ACTION_DO_CREATE = "do_create";
    private static final String PARAMETER_ACTION_DO_MODIFY = "do_modify";
    private static final String PARAMETER_ACTION_VIEW_HISTORY = "view_history";
    private static final String PARAMETER_ACTION_SEARCH = "search";
    private static final String TEMPLATE_MODIFY_WIKI = "skin/plugins/wiki/modify_page.html";
    private static final String TEMPLATE_CREATE_WIKI = "skin/plugins/wiki/create_page.html";
    private static final String TEMPLATE_VIEW_WIKI = "skin/plugins/wiki/view_page.html";
    private static final String TEMPLATE_PREVIEW_WIKI = "skin/plugins/wiki/preview_page.html";
    private static final String TEMPLATE_VIEW_HISTORY_WIKI = "skin/plugins/wiki/history_page.html";
    private static final String TEMPLATE_VIEW_DIFF_TOPIC_WIKI = "skin/plugins/wiki/diff_topic.html";
    private static final String TEMPLATE_LIST_WIKI = "skin/plugins/wiki/list_wiki.html";
    private static final String TEMPLATE_SEARCH_WIKI = "skin/plugins/wiki/search_wiki.html";
    private static final String BEAN_SEARCH_ENGINE = "wiki.wikiSearchEngine";
    private static final String PROPERTY_PAGE_PATH = "wiki.pagePathLabel";
    private static final String PROPERTY_PAGE_TITLE = "wiki.pageTitle";
    private static final String PROPERTY_DEFAULT_RESULT_PER_PAGE = "wiki.search_wiki.itemsPerPage";
    private static final String MESSAGE_ACCESS_CREATION_DENIED = "wiki.message.accessDenied.create.requiresAuthentification";
    private static final String MESSAGE_ACCESS_MODIFICATION_DENIED = "wiki.message.accessDenied.modify.requiresAuthentification";
    private static final String MESSAGE_PAGE_NOT_EXISTS = "wiki.message.accessDenied.pageNotExists";
    private static final String MESSAGE_PAGE_ALREADY_EXISTS = "wiki.message.accessDenied.pageAlreadyExists";
    private static final String PARAMETER_MODIFICATION_COMMENT = "modification_comment";
    private static final String PARAMETER_PREVIOUS_VERSION_ID = "previous_version_id";
    private static final String PARAMETER_PREVIEW_CONTENT = "preview_content";
    private static final String PARAMETER_TOPIC_ID = "topic_id";
    private static final String PARAMETER_NEW_VERSION = "new_version";
    private static final String PARAMETER_OLD_VERSION = "old_version";
    private static final Object PARAMETER_ACTION_VIEW_DIFF = "view_diff";
    private static final String MARK_TOPIC = "topic";
    private static final String MARK_TOPIC_NAME = "topic_name";
    private static final String MARK_LIST_TOPIC = "list_topic";
    private static final String MARK_LATEST_VERSION = "lastVersion";
    private static final String MARK_PREVIEWED_VERSION = "preview_content";
    private static final String MARK_LIST_DIFFS = "listDiffs";
    private static final String MARK_RESULT = "result";
    private static final String MARK_LIST_TOPIC_VERSION = "listTopicVersion";
    private static final String MARK_PREVIEW_CONTENT = "preview_content";
    private static final String MARK_QUERY = "query";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final int ACTION_NONE = 0;
    private static final int ACTION_VIEW = 1;
    private static final int ACTION_VIEW_HISTORY = 2;
    private static final int ACTION_VIEW_DIFF = 3;
    private static final int ACTION_CREATE = 4;
    private static final int ACTION_MODIFY = 5;
    private static final int ACTION_DO_CREATE = 6;
    private static final int ACTION_DO_MODIFY = 7;
    private static final int ACTION_SEARCH = 8;
    
    // private fields
    private Plugin _plugin;
    private boolean _bInit;
    private String _strCurrentPageIndex;
    private int _nDefaultItemsPerPage;
    private int _nItemsPerPage;

    /**
     * " Returns the content of the page wiki.
     *
     * @param request   The http request
     * @param nMode  The current mode
     * @param plugin The plugin object
     * @throws fr.paris.lutece.portal.service.message.SiteMessageException
     * Message displayed if an exception occurs
     */
    @Override
    public XPage getPage( HttpServletRequest request, int nMode, Plugin plugin )
            throws SiteMessageException, UserNotSignedException
    {
        init( request );
             
        String strPluginName = plugin.getName(  );
        String strPageName = request.getParameter( PARAMETER_PAGE_NAME );
        String strContent = request.getParameter( PARAMETER_CONTENT );


        XPage page = new XPage();
        page.setTitle( AppPropertiesService.getProperty( PROPERTY_PAGE_TITLE ) );
        page.setPathLabel( AppPropertiesService.getProperty( PROPERTY_PAGE_PATH ) );
        switch ( getAction( request ) )
        {
            case ACTION_NONE:
                home( page, request );
                break;
            case ACTION_VIEW:
                view( page, request, strPageName );
                break;
            case ACTION_VIEW_HISTORY:
                viewHistory( page, request, strPageName );
                break;
            case ACTION_VIEW_DIFF:
                viewDiff( page, request, strPageName );
                break;
            case ACTION_CREATE:
                checkUser( request );
                create( page, request, strPageName );
                break;
            case ACTION_MODIFY:
                checkUser( request );
                modify( page, request, strPageName );
                break;
            case ACTION_DO_CREATE:

                doCreate( page, request, strPageName, checkUser( request ), strContent );
                break;
            case ACTION_DO_MODIFY:
                doModify( page, request, strPageName, checkUser( request ), strContent );
                break;
            case ACTION_SEARCH:
            	search( page, request, strPluginName );
            	break;
        }

       return page;
    }

    private void search(XPage page, HttpServletRequest request, String strPluginName )
    {
    	String strQuery = request.getParameter( PARAMETER_QUERY );
    	String strPortalUrl = AppPathService.getPortalUrl(  );
    	
        UrlItem urlWikiXpage = new UrlItem( strPortalUrl );
        urlWikiXpage.addParameter( XPageAppService.PARAM_XPAGE_APP, strPluginName );
        urlWikiXpage.addParameter( PARAMETER_ACTION, PARAMETER_ACTION_SEARCH );

    	_strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
    	_nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_RESULT_PER_PAGE, 10 );
    	_nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );
    	
    	SearchEngine engine = (SearchEngine) SpringContextService.getPluginBean( strPluginName, BEAN_SEARCH_ENGINE );
        List<SearchResult> listResults = engine.getSearchResults( strQuery, request );
        
        Paginator paginator = new Paginator( listResults, _nItemsPerPage, urlWikiXpage.getUrl(  ), PARAMETER_PAGE_INDEX,
        		_strCurrentPageIndex );

        HashMap<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_RESULT, paginator.getPageItems(  ) );
        model.put( MARK_QUERY, strQuery );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );
        
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_SEARCH_WIKI, Locale.getDefault(  ), model );
        page.setContent( template.getHtml(  ) );
		
	}

	private LuteceUser checkUser( HttpServletRequest request ) throws UserNotSignedException
    {
        LuteceUser luteceUser;

        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            luteceUser = SecurityService.getInstance(  ).getRemoteUser( request );
        }
        else
        {
            luteceUser = new WikiAnonymousUser(  );
        }
        return luteceUser;
    }

    private int getAction( HttpServletRequest request )
    {
        String strAction = request.getParameter( PARAMETER_ACTION );
        int nAction = ACTION_NONE;
        if ( strAction != null )
        {

            if ( strAction.equals( PARAMETER_ACTION_VIEW ) )
            {
                nAction = ACTION_VIEW;
            }
            else if ( strAction.equals( PARAMETER_ACTION_VIEW_HISTORY ) )
            {
                nAction = ACTION_VIEW_HISTORY;
            }
            else if ( strAction.equals( PARAMETER_ACTION_VIEW_DIFF ) )
            {
                nAction = ACTION_VIEW_DIFF;
            }
            else if ( strAction.equals( PARAMETER_ACTION_CREATE ) )
            {
                nAction = ACTION_CREATE;
            }
            else if ( strAction.equals( PARAMETER_ACTION_MODIFY ) )
            {
                nAction = ACTION_MODIFY;
            }
            else if ( strAction.equals( PARAMETER_ACTION_DO_CREATE ) )
            {
                nAction = ACTION_DO_CREATE;
            }
            else if ( strAction.equals( PARAMETER_ACTION_DO_MODIFY ) )
            {
                nAction = ACTION_DO_MODIFY;
            }
            else if ( strAction.equals( PARAMETER_ACTION_SEARCH ) )
            {
            	nAction = ACTION_SEARCH;
            }
        }
        return nAction;
    }

    private void home( XPage page, HttpServletRequest request )
    {
        page.setContent( displayAll() );

    }

    private void view( XPage page, HttpServletRequest request, String strPageName ) throws SiteMessageException
    {
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );

        if ( topic == null )
        {
            SiteMessageService.setMessage( request, MESSAGE_PAGE_NOT_EXISTS, SiteMessage.TYPE_STOP );
        }

        page.setContent( viewPageContent( strPageName ) );
        page.setPathLabel( getPathLabel( strPageName ));
        page.setTitle( getPageTitle( strPageName ));

    }

    private void viewHistory( XPage page, HttpServletRequest request, String strPageName ) throws SiteMessageException
    {
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );

        if ( topic == null )
        {
            SiteMessageService.setMessage( request, MESSAGE_PAGE_NOT_EXISTS, SiteMessage.TYPE_STOP );
        }

        page.setContent( viewPageHistory( strPageName ) );
        page.setPathLabel( getPathLabel( strPageName ));
        page.setTitle( getPageTitle( strPageName ));
    }

    private void viewDiff( XPage page, HttpServletRequest request, String strPageName ) throws SiteMessageException
    {
        String strNewVersion = request.getParameter( PARAMETER_NEW_VERSION );
        String strOldVersion = request.getParameter( PARAMETER_OLD_VERSION );
        int nNewTopicVersion = Integer.parseInt( strNewVersion );
        int nOldTopicVersion = Integer.parseInt( strOldVersion );

        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );

        if ( topic == null )
        {
            SiteMessageService.setMessage( request, MESSAGE_PAGE_NOT_EXISTS, SiteMessage.TYPE_STOP );
        }

        page.setContent( viewTopicDiff( strPageName, nNewTopicVersion, nOldTopicVersion ) );
        page.setPathLabel( getPathLabel( strPageName ));
        page.setTitle( getPageTitle( strPageName ));
    }

    private void create( XPage page, HttpServletRequest request, String strPageName ) throws SiteMessageException
    {
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );

        if ( topic != null )
        {
            SiteMessageService.setMessage( request, MESSAGE_PAGE_ALREADY_EXISTS, SiteMessage.TYPE_STOP );
        }


        page.setContent( createPageContent( strPageName ) );
    }

    private void modify( XPage page, HttpServletRequest request, String strPageName ) throws SiteMessageException
    {
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );

        if ( topic == null )
        {
            SiteMessageService.setMessage( request, MESSAGE_PAGE_NOT_EXISTS, SiteMessage.TYPE_STOP );
        }

        page.setContent( modifyPageContent( strPageName ) );
        page.setPathLabel( getPathLabel( strPageName ));
        page.setTitle( getPageTitle( strPageName ));
    }

    private void doCreate( XPage page, HttpServletRequest request, String strPageName, LuteceUser luteceUser, String strContent ) throws SiteMessageException
    {
        if ( TopicHome.findByPrimaryKey( strPageName, _plugin ) != null )
        {
            SiteMessageService.setMessage( request, MESSAGE_PAGE_ALREADY_EXISTS, SiteMessage.TYPE_STOP );
        }
        doCreatePage( strPageName, luteceUser, strContent );
        page.setContent( viewPageContent( strPageName ) );
    }

    private void doModify( XPage page, HttpServletRequest request, String strPageName, LuteceUser user, String strContent )
    {
        String strPreviousVersionId = request.getParameter( PARAMETER_PREVIOUS_VERSION_ID );
        String strTopicId = request.getParameter( PARAMETER_TOPIC_ID );
        String strComment = request.getParameter( PARAMETER_MODIFICATION_COMMENT );
        int nPreviousVersionId = Integer.parseInt( strPreviousVersionId );
        int nTopicId = Integer.parseInt( strTopicId );
        doModifyPage( nTopicId, user, strComment, strContent, nPreviousVersionId, _plugin );
        page.setContent( viewPageContent( strPageName ) );
        page.setPathLabel( getPathLabel( strPageName ));
        page.setTitle( getPageTitle( strPageName ));
    }

    private String displayAll()
    {
        Map<String, Object> model = new HashMap<String, Object>();
        Collection<Topic> listTopic = TopicHome.getTopicsList( _plugin );

        model.put( MARK_LIST_TOPIC, listTopic );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_LIST_WIKI, Locale.getDefault(), model );
        return template.getHtml();
    }

    /**
     *
     * @param strPageName
     * @return
     */
    public String viewPageContent( String strPageName )
    {
        Map<String, Object> model = new HashMap<String, Object>();
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );
        String strContent = TopicVersionHome.findLastVersion( topic.getIdTopic(), _plugin ).getWikiContent();

        String strWikiResult = new LuteceWikiParser( strContent ).toString();
        model.put( MARK_RESULT, strWikiResult );
        model.put( MARK_TOPIC, topic );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_WIKI, Locale.getDefault(), model );

        return template.getHtml();
    }

    public String previewPageContent( String strTopicName, String strInput )
    {
        Map<String, Object> model = new HashMap<String, Object>();

        String strWikiResult = new LuteceWikiParser( strInput ).toString();
        model.put( MARK_RESULT, strWikiResult );
        model.put( MARK_PREVIEW_CONTENT, strInput );
        model.put( MARK_TOPIC_NAME, strTopicName );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_PREVIEW_WIKI, Locale.getDefault(), model );

        return template.getHtml();
    }

    /**
     *
     * @param strPageName
     * @return
     */
    public String viewPageHistory( String strPageName )
    {
        Map<String, Object> model = new HashMap<String, Object>();
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );
        Collection<TopicVersion> listTopicVersions = TopicVersionHome.findAllVersions( topic.getIdTopic(), _plugin );

        model.put( MARK_LIST_TOPIC_VERSION, listTopicVersions );
        model.put( MARK_TOPIC, topic );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_HISTORY_WIKI, Locale.getDefault(), model );

        return template.getHtml();
    }

    /**
     *
     * @param strPageName
     * @param nNewTopicVersion
     * @param nOldTopicVersion
     * @return
     */
    public String viewTopicDiff( String strPageName, int nNewTopicVersion, int nOldTopicVersion )
    {
        if ( nOldTopicVersion == 0 )
        {
            nOldTopicVersion = nNewTopicVersion;
        }
        TopicVersion newTopicVersion = TopicVersionHome.findByPrimaryKey( nNewTopicVersion, _plugin );
        TopicVersion oldTopicVersion = TopicVersionHome.findByPrimaryKey( nOldTopicVersion, _plugin );

        List<WikiDiff> listDiffs = DiffUtils.diff( newTopicVersion.getWikiContent(),
                oldTopicVersion.getWikiContent() );

        Map<String, Object> model = new HashMap<String, Object>();
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );
        model.put( MARK_LIST_DIFFS, listDiffs );
        model.put( MARK_TOPIC, topic );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_DIFF_TOPIC_WIKI, Locale.getDefault(),
                model );

        return template.getHtml();
    }

    /**
     *
     * @param nTopicId
     * @param user
     * @param strComment
     * @param strContent
     * @param nPreviousVersion
     * @param plugin
     */
    public void doModifyPage( int nTopicId, LuteceUser user, String strComment, String strContent,
            int nPreviousVersion, Plugin plugin )
    {
        TopicVersionHome.modifyContentOnly( nTopicId, user.getName(), strComment, strContent, nPreviousVersion,
                _plugin );
    }

    /**
     *
     * @param strPageName
     * @param strContent
     */
    public void doCreatePage( String strPageName, LuteceUser user, String strContent )
    {
        Topic topic = new Topic();
        topic.setPageName( strPageName );


        Topic newTopic = TopicHome.create( topic, _plugin );
        TopicVersion version = new TopicVersion();
        version.setEditComment("");
        String strUser = ( user != null ) ? user.getName() : "anonymous";
        version.setLuteceUserId( strUser );
        TopicVersionHome.create( version , _plugin );
        TopicVersionHome.modifyContentOnly( newTopic.getIdTopic(), strUser, "", strContent, 0, _plugin );
    }

    /**
     *
     * @param strPageName
     * @return
     */
    public String modifyPageContent( String strPageName )
    {
        Map<String, Object> model = new HashMap<String, Object>();
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );
        TopicVersion topicVersion = TopicVersionHome.findLastVersion( topic.getIdTopic(), _plugin );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_LATEST_VERSION, topicVersion );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_WIKI, Locale.getDefault(), model );

        return template.getHtml();
    }

    /**
     *
     * @param strPageName
     * @return
     */
    public String createPageContent( String strPageName )
    {
        Map<String, Object> model = new HashMap<String, Object>();
        Topic topic = new Topic();
        topic.setPageName( strPageName );
        model.put( MARK_TOPIC, topic );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_WIKI, Locale.getDefault(), model );

        return template.getHtml();
    }

    private void init( HttpServletRequest request )
    {
        if( !_bInit )
        {
            _plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );
            String strWebappUrl = AppPathService.getBaseUrl(request) + AppPathService.getPortalUrl();
            LuteceWikiParser.setPortalUrl(strWebappUrl);
            _bInit = true;
        }
    }

    private String getPathLabel(String strPageName)
    {
        StringBuilder sbPath = new StringBuilder();
        sbPath.append( AppPropertiesService.getProperty( PROPERTY_PAGE_PATH ) );
        sbPath.append( " "  );
        sbPath.append( strPageName );
        
        return sbPath.toString();
    }

    private String getPageTitle(String strPageName)
    {
        StringBuilder sbPath = new StringBuilder();
        sbPath.append( AppPropertiesService.getProperty( PROPERTY_PAGE_TITLE ) );
        sbPath.append( " "  );
        sbPath.append( strPageName );
        
        return sbPath.toString();
    }
}
