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
package fr.paris.lutece.plugins.wiki.web;

import fr.paris.lutece.plugins.avatar.service.AvatarService;
import fr.paris.lutece.plugins.wiki.business.Image;
import fr.paris.lutece.plugins.wiki.business.ImageHome;
import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.plugins.wiki.business.TopicVersion;
import fr.paris.lutece.plugins.wiki.business.TopicVersionHome;
import fr.paris.lutece.plugins.wiki.business.WikiContent;
import fr.paris.lutece.plugins.wiki.service.DiffService;
import fr.paris.lutece.plugins.wiki.service.RoleService;
import fr.paris.lutece.plugins.wiki.service.WikiLocaleService;
import fr.paris.lutece.plugins.wiki.service.WikiService;
import fr.paris.lutece.plugins.wiki.service.WikiUtils;
import fr.paris.lutece.plugins.wiki.service.parser.LuteceWikiParser;
import fr.paris.lutece.plugins.wiki.utils.auth.WikiAnonymousUser;
import fr.paris.lutece.portal.business.page.Page;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.prefs.UserPreferencesService;
import fr.paris.lutece.portal.service.search.SearchEngine;
import fr.paris.lutece.portal.service.search.SearchResult;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import fr.paris.lutece.portal.web.resource.ExtendableResourcePluginActionManager;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.i18n.LocaleString;

/**
 * This class provides a simple implementation of an XPage
 */
@Controller( xpageName = "wiki", pageTitleProperty = "wiki.pageTitle", pagePathProperty = "wiki.pagePathLabel" )
public class WikiApp extends MVCApplication
{
    private static final String TEMPLATE_MODIFY_WIKI = "skin/plugins/wiki/modify_page.html";
    private static final String TEMPLATE_PREVIEW_WIKI = "skin/plugins/wiki/preview_page.html";
    private static final String TEMPLATE_VIEW_WIKI = "skin/plugins/wiki/view_page.html";
    private static final String TEMPLATE_VIEW_HISTORY_WIKI = "skin/plugins/wiki/history_page.html";
    private static final String TEMPLATE_VIEW_DIFF_TOPIC_WIKI = "skin/plugins/wiki/diff_topic.html";
    private static final String TEMPLATE_LIST_WIKI = "skin/plugins/wiki/list_wiki.html";
    private static final String TEMPLATE_MAP_WIKI = "skin/plugins/wiki/map_wiki.html";
    private static final String TEMPLATE_SEARCH_WIKI = "skin/plugins/wiki/search_wiki.html";

    private static final String BEAN_SEARCH_ENGINE = "wiki.wikiSearchEngine";

    private static final String PROPERTY_PAGE_PATH = "wiki.pagePathLabel";
    private static final String PROPERTY_PAGE_TITLE = "wiki.pageTitle";
    private static final String PROPERTY_DEFAULT_RESULT_PER_PAGE = "wiki.search_wiki.itemsPerPage";
    private static final String PROPERTY_PATH_MAP = "wiki.path.map";
    private static final String PROPERTY_TITLE_MAP = "wiki.title.map";
    private static final String PROPERTY_PATH_LIST = "wiki.path.list";
    private static final String PROPERTY_TITLE_LIST = "wiki.title.list";
    private static final String PROPERTY_PATH_SEARCH = "wiki.path.search";
    private static final String PROPERTY_TITLE_SEARCH = "wiki.title.search";

    private static final String MARK_TOPIC = "topic";
    private static final String MARK_TOPIC_TITLE = "topic_title";
    private static final String MARK_TOPIC_NAME = "topic_name";
    private static final String MARK_TOPIC_CONTENT_HTML = "topic_content_html";
    private static final String MARK_REFLIST_TOPIC = "reflist_topic";
    private static final String MARK_MAP_TOPIC_TITLE = "map_topic_title";
    private static final String MARK_MAP_TOPIC_CHILDREN = "map_topic_children";
    private static final String MARK_WIKI_ROOT_PAGE_NAME = "wiki_root_page_name";
    private static final String MARK_LATEST_VERSION = "lastVersion";
    private static final String MARK_DIFF_HTML = "diff_html";
    private static final String MARK_DIFF_SOURCE = "diff_source";
    private static final String MARK_RESULT = "result";
    private static final String MARK_LIST_TOPIC_VERSION = "listTopicVersion";
    private static final String MARK_PAGE_ROLES_LIST = "page_roles_list";
    private static final String MARK_QUERY = "query";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_EDIT_ROLE = "has_edit_role";
    private static final String MARK_ADMIN_ROLE = "has_admin_role";
    private static final String MARK_EXTEND = "isExtendInstalled";
    private static final String MARK_LANGUAGES_LIST = "languages_list";
    private static final String MARK_CURRENT_LANGUAGE = "current_language";

    private static final String PARAMETER_LANGUAGE = "language";

    private static final String VIEW_HOME = "home";
    private static final String VIEW_LIST = "list";
    private static final String VIEW_MAP = "map";
    private static final String VIEW_PAGE = "page";
    private static final String VIEW_MODIFY_PAGE = "modifyPage";
    private static final String VIEW_PREVIEW = "preview";
    private static final String VIEW_HISTORY = "history";
    private static final String VIEW_SEARCH = "search";
    private static final String VIEW_DIFF = "diff";
    private static final String VIEW_LIST_IMAGES = "listImages";

    private static final String ACTION_NEW_PAGE = "newPage";
    private static final String ACTION_MODIFY_PAGE = "modifyPage";
    private static final String ACTION_DELETE_PAGE = "deletePage";
    private static final String ACTION_REMOVE_IMAGE = "removeImage";
    private static final String ACTION_CONFIRM_REMOVE_IMAGE = "confirmRemoveImage";
    private static final String ACTION_REMOVE_VERSION = "removeVersion";
    private static final String ACTION_CONFIRM_REMOVE_VERSION = "confirmRemoveVersion";
    private static final String ACTION_UPLOAD_IMAGE = "uploadImage";
    private static final String ACTION_CHANGE_LANGUAGE = "changeLanguage";

    private static final String MESSAGE_IMAGE_REMOVED = "wiki.message.image.removed";
    private static final String MESSAGE_CONFIRM_REMOVE_IMAGE = "wiki.message.confirmRemoveImage";
    private static final String MESSAGE_NAME_MANDATORY = "wiki.message.error.name.notNull";
    private static final String MESSAGE_FILE_MANDATORY = "wiki.message.error.file.notNull";
    private static final String MESSAGE_CONFIRM_REMOVE_VERSION = "wiki.message.confirmRemoveVersion";
    private static final String MESSAGE_VERSION_REMOVED = "wiki.message.version.removed";
    private static final String MESSAGE_AUTHENTICATION_REQUIRED = "wiki.message.authenticationRequired";
    private static final String MESSAGE_PATH_HIDDEN = "wiki.message.path.hidden";

    private static final String ANCHOR_IMAGES = "#images";

    private static final String DSKEY_WIKI_ROOT_LABEL = "wiki.site_property.path.rootLabel";
    private static final String DSKEY_WIKI_ROOT_PAGENAME = "wiki.site_property.path.rootPageName";

    private static final String PAGE_DEFAULT = "home";
    private static final String URL_VIEW_PAGE = "page=wiki&amp;view=page&amp;page_name=";

    private static final String PLUGIN_EXTEND = "extend";

    public static final int ACTION_VIEW = 1;

    private static final int MODE_VIEW = 0;
    private static final int MODE_EDIT = 1;

    // private fields
    private String _strCurrentPageIndex;
    private int _nDefaultItemsPerPage;
    private int _nItemsPerPage;

    /**
     * Gets the Home page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     */
    @View( value = VIEW_HOME, defaultView = true )
    public XPage getHome( HttpServletRequest request )
    {
        String strWikiRootPageName = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_PAGENAME, "" );
        if ( !"".equals( strWikiRootPageName ) )
        {
            Map<String, String> mapParameters = new ConcurrentHashMap<>( );
            mapParameters.put( Constants.PARAMETER_PAGE_NAME, strWikiRootPageName );

            return redirect( request, VIEW_PAGE, mapParameters );
        }
        return redirectView( request, VIEW_LIST );
    }

    /**
     * Gets list page of all wiki pages
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( VIEW_LIST )
    public XPage getTopicsList( HttpServletRequest request )
    {
        List<Topic> listTopic = getTopicsForUser( request );

        Map<String, String> mapTopicTitle = new HashMap( );

        for ( Topic topic : listTopic )
        {
            mapTopicTitle.put( topic.getPageName( ), getTopicTitle( request, topic ) );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_MAP_TOPIC_TITLE, mapTopicTitle );
        model.put( MARK_LANGUAGES_LIST, WikiLocaleService.getLanguages( ) );
        model.put( MARK_CURRENT_LANGUAGE, getLanguage( request ) );

        XPage page = getXPage( TEMPLATE_LIST_WIKI, getLocale( request ), model );
        page.setTitle( getPageTitle( I18nService.getLocalizedString( PROPERTY_TITLE_LIST, LocaleService.getContextUserLocale( request ) ) ) );
        page.setExtendedPathLabel( getViewExtendedPath( I18nService.getLocalizedString( PROPERTY_PATH_LIST, LocaleService.getContextUserLocale( request ) ) ) );

        return page;
    }

    /**
     * Gets maps page of the wiki
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( VIEW_MAP )
    public XPage getTopicsMap( HttpServletRequest request )
    {
        Collection<Topic> listTopic = getTopicsForUser( request );

        String strWikiRootPageName = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_PAGENAME, AppPropertiesService.getProperty( PAGE_DEFAULT ) );

        Map<String, String> mapTopicTitle = new HashMap( );
        Map<String, List<Topic>> mapTopicChildren = new HashMap( );

        for ( Topic topic : listTopic )
        {
            mapTopicTitle.put( topic.getPageName( ), getTopicTitle( request, topic ) );

            String strParentPageName = topic.getParentPageName( );
            if ( strParentPageName != null && !topic.getPageName( ).isEmpty( ) )
            {
                if ( mapTopicChildren.containsKey( strParentPageName ) )
                {
                    mapTopicChildren.get( strParentPageName ).add( topic );
                }
                else
                {
                    List<Topic> listChildren = new ArrayList<>( );
                    listChildren.add( topic );
                    mapTopicChildren.put( strParentPageName, listChildren );
                }
            }
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_MAP_TOPIC_TITLE, mapTopicTitle );
        model.put( MARK_MAP_TOPIC_CHILDREN, mapTopicChildren );
        model.put( MARK_WIKI_ROOT_PAGE_NAME, strWikiRootPageName );
        model.put( MARK_LANGUAGES_LIST, WikiLocaleService.getLanguages( ) );
        model.put( MARK_CURRENT_LANGUAGE, getLanguage( request ) );

        XPage page = getXPage( TEMPLATE_MAP_WIKI, getLocale( request ), model );
        page.setTitle( getPageTitle( I18nService.getLocalizedString( PROPERTY_TITLE_MAP, LocaleService.getContextUserLocale( request ) ) ) );
        page.setExtendedPathLabel( getViewExtendedPath( I18nService.getLocalizedString( PROPERTY_PATH_MAP, LocaleService.getContextUserLocale( request ) ) ) );

        return page;
    }

    /**
     * Gets the search page
     *
     * @param request
     *            The request
     * @return Thye XPage
     */
    @View( VIEW_SEARCH )
    public XPage getSearch( HttpServletRequest request )
    {
        String strQuery = request.getParameter( Constants.PARAMETER_QUERY );
        String strPortalUrl = AppPathService.getPortalUrl( );

        UrlItem urlWikiXpage = new UrlItem( strPortalUrl );
        urlWikiXpage.addParameter( XPageAppService.PARAM_XPAGE_APP, Constants.PLUGIN_NAME );
        urlWikiXpage.addParameter( Constants.PARAMETER_VIEW, VIEW_SEARCH );

        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_RESULT_PER_PAGE, 10 );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        SearchEngine engine = (SearchEngine) SpringContextService.getBean( BEAN_SEARCH_ENGINE );
        List<SearchResult> listResults = engine.getSearchResults( strQuery, request );

        Paginator paginator = new Paginator( listResults, _nItemsPerPage, urlWikiXpage.getUrl( ), Constants.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

        Map<String, Object> model = getModel( );
        model.put( MARK_RESULT, paginator.getPageItems( ) );
        model.put( MARK_QUERY, strQuery );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, String.valueOf( _nItemsPerPage ) );

        XPage page = getXPage( TEMPLATE_SEARCH_WIKI, getLocale( request ), model );
        page.setTitle( getPageTitle( I18nService.getLocalizedString( PROPERTY_TITLE_SEARCH, getLocale( request ) ) ) );
        page.setExtendedPathLabel( getViewExtendedPath( I18nService.getLocalizedString( PROPERTY_PATH_SEARCH, getLocale( request ) ) ) );

        return page;
    }

    /**
     * Gets a wiki page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an error occurs
     */
    @View( VIEW_PAGE )
    public XPage getTopic( HttpServletRequest request ) throws SiteMessageException, UserNotSignedException
    {
        if ( !SecurityService.isAuthenticationEnable( ) )
        {
            SiteMessageService.setMessage( request, MESSAGE_AUTHENTICATION_REQUIRED, SiteMessage.TYPE_ERROR );
        }
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName, MODE_VIEW );
        TopicVersion version = TopicVersionHome.findLastVersion( topic.getIdTopic( ) );
        if ( version == null )
        {
            UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + AppPathService.getPortalUrl( ) );
            url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
            url.addParameter( Constants.PARAMETER_ACTION, ACTION_NEW_PAGE );
            url.addParameter( Constants.PARAMETER_PAGE_NAME, strPageName );
            return redirect( request, url.getUrl( ) );
        }
        fillUserData( version );
        String strWikiPage = WikiService.instance( ).getWikiPage( strPageName, version, getPageUrl( request ), getLanguage( request ) );
        Map<String, Object> model = getModel( );
        model.put( MARK_RESULT, strWikiPage );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_TOPIC_TITLE, getTopicTitle( request, topic ) );
        model.put( MARK_LATEST_VERSION, version );
        model.put( MARK_EDIT_ROLE, RoleService.hasEditRole( request, topic ) );
        model.put( MARK_ADMIN_ROLE, RoleService.hasAdminRole( request ) );
        model.put( MARK_EXTEND, isExtend( ) );
        model.put( MARK_LANGUAGES_LIST, WikiLocaleService.getLanguages( ) );
        model.put( MARK_CURRENT_LANGUAGE, getLanguage( request ) );
        XPage page = getXPage( TEMPLATE_VIEW_WIKI, getLocale( request ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        page.setExtendedPathLabel( getPageExtendedPath( topic, request ) );

        return page;
    }

    /**
     * Creates a new Page
     *
     * @param request
     *            The request
     * @return The XPage
     * @throws UserNotSignedException
     *             if the user is not signed
     * @throws java.io.UnsupportedEncodingException
     *             if an encoding exception occurs
     */
    @Action( ACTION_NEW_PAGE )
    public XPage doCreateTopic( HttpServletRequest request ) throws UserNotSignedException, UnsupportedEncodingException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strParentPageName = request.getParameter( Constants.PARAMETER_PARENT_PAGE_NAME );
        String strPageTitle = strPageName;
        strPageName = WikiUtils.normalize( strPageName );

        Topic topic = TopicHome.findByPrimaryKey( strPageName );
        if ( topic == null )
        {
            topic = new Topic( );
            topic.setPageName( strPageName );
            topic.setViewRole( Page.ROLE_NONE );
            topic.setEditRole( Page.ROLE_NONE );
            topic.setParentPageName( strParentPageName );

            TopicHome.create( topic );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName );
        mapParameters.put( Constants.PARAMETER_PAGE_TITLE, URLEncoder.encode( strPageTitle, "UTF-8" ) );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
     * Displays the form to update a wiki page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an exception occurs
     */
    @View( VIEW_MODIFY_PAGE )
    public XPage getModifyTopic( HttpServletRequest request ) throws SiteMessageException, UserNotSignedException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );

        Topic topic;
        Topic topicSession = (Topic) request.getSession( ).getAttribute( MARK_TOPIC );
        if ( topicSession != null && topicSession.getPageName( ).equals( strPageName ) )
        {
            topic = topicSession;
            request.getSession( ).removeAttribute( MARK_TOPIC );
        }
        else
        {
            topic = getTopic( request, strPageName, MODE_EDIT );
        }

        TopicVersion topicVersion;
        TopicVersion topicVersionSession = (TopicVersion) request.getSession( ).getAttribute( MARK_LATEST_VERSION );
        if ( topicVersionSession != null && topicVersionSession.getIdTopic( ) == topic.getIdTopic( ) )
        {
            topicVersion = topicVersionSession;
            request.getSession( ).removeAttribute( MARK_LATEST_VERSION );
        }
        else
        {
            topicVersion = TopicVersionHome.findLastVersion( topic.getIdTopic( ) );
            if ( topicVersion != null )
            {
                String strLanguage = getLanguage( request );
                WikiContent content = topicVersion.getWikiContent( strLanguage );
                content.setWikiContent( WikiService.renderEditor( topicVersion, strLanguage ) );
            }
        }

        ReferenceList topicRefList = getTopicsReferenceListForUser( request, true );
        topicRefList.removeIf( x -> x.getCode( ).equals( topic.getPageName( ) ) );

        Map<String, Object> model = getModel( );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_LATEST_VERSION, topicVersion );
        model.put( MARK_PAGE_ROLES_LIST, RoleService.getUserRoles( request ) );
        model.put( MARK_EDIT_ROLE, RoleService.hasEditRole( request, topic ) );
        model.put( MARK_ADMIN_ROLE, RoleService.hasAdminRole( request ) );
        model.put( MARK_LANGUAGES_LIST, WikiLocaleService.getLanguages( ) );
        model.put( MARK_REFLIST_TOPIC, topicRefList );

        ExtendableResourcePluginActionManager.fillModel( request, null, model, Integer.toString( topic.getIdTopic( ) ), Topic.RESOURCE_TYPE );

        XPage page = getXPage( TEMPLATE_MODIFY_WIKI, request.getLocale( ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        page.setExtendedPathLabel( getPageExtendedPath( topic, request ) );

        return page;
    }

    /**
     * Modifies a wiki page
     *
     * @param request
     *            The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException
     *             If user not connected
     */
    @Action( ACTION_MODIFY_PAGE )
    public XPage doModifyTopic( HttpServletRequest request ) throws UserNotSignedException
    {
        LuteceUser user = checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName );

        if ( RoleService.hasEditRole( request, topic ) )
        {
            String strPreviousVersionId = request.getParameter( Constants.PARAMETER_PREVIOUS_VERSION_ID );
            String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
            String strComment = request.getParameter( Constants.PARAMETER_MODIFICATION_COMMENT );
            String strViewRole = request.getParameter( Constants.PARAMETER_VIEW_ROLE );
            String strEditRole = request.getParameter( Constants.PARAMETER_EDIT_ROLE );
            String strParentPageName = request.getParameter( Constants.PARAMETER_PARENT_PAGE_NAME );
            int nPreviousVersionId = Integer.parseInt( strPreviousVersionId );
            int nTopicId = Integer.parseInt( strTopicId );

            TopicVersion topicVersion = new TopicVersion( );
            topicVersion.setIdTopic( nTopicId );
            topicVersion.setUserName( user.getName( ) );
            topicVersion.setEditComment( strComment );
            topicVersion.setIdTopicVersionPrevious( nPreviousVersionId );
            for ( String strLanguage : WikiLocaleService.getLanguages( ) )
            {
                String strPageTitle = request.getParameter( Constants.PARAMETER_PAGE_TITLE + "_" + strLanguage );
                String strContent = request.getParameter( Constants.PARAMETER_CONTENT + "_" + strLanguage );
                WikiContent content = new WikiContent( );
                content.setPageTitle( strPageTitle );
                content.setWikiContent( strContent );
                topicVersion.addLocalizedWikiContent( strLanguage, content );
            }

            TopicVersionHome.addTopicVersion( topicVersion );

            topic.setViewRole( strViewRole );
            topic.setEditRole( strEditRole );
            topic.setParentPageName( strParentPageName );
            TopicHome.update( topic );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName );

        return redirect( request, VIEW_PAGE, mapParameters );
    }

    /**
     * Displays the preview of a wiki page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an exception occurs
     */
    @View( VIEW_PREVIEW )
    public XPage getPreviewTopic( HttpServletRequest request ) throws SiteMessageException, UserNotSignedException
    {
        LuteceUser user = checkUser( request );

        String strPreviousVersionId = request.getParameter( Constants.PARAMETER_PREVIOUS_VERSION_ID );
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        String strComment = request.getParameter( Constants.PARAMETER_MODIFICATION_COMMENT );
        String strViewRole = request.getParameter( Constants.PARAMETER_VIEW_ROLE );
        String strEditRole = request.getParameter( Constants.PARAMETER_EDIT_ROLE );
        String strParentPageName = request.getParameter( Constants.PARAMETER_PARENT_PAGE_NAME );
        int nPreviousVersionId = Integer.parseInt( strPreviousVersionId );
        int nTopicId = Integer.parseInt( strTopicId );

        TopicVersion topicVersion = new TopicVersion( );
        topicVersion.setIdTopic( nTopicId );
        topicVersion.setUserName( user.getName( ) );
        topicVersion.setEditComment( strComment );
        topicVersion.setIdTopicVersionPrevious( nPreviousVersionId );
        for ( String strLanguage : WikiLocaleService.getLanguages( ) )
        {
            String strPageTitle = request.getParameter( Constants.PARAMETER_PAGE_TITLE + "_" + strLanguage );
            String strContent = request.getParameter( Constants.PARAMETER_CONTENT + "_" + strLanguage );
            WikiContent content = new WikiContent( );
            content.setPageTitle( strPageTitle );
            content.setWikiContent( LuteceWikiParser.renderWiki( strContent ) );
            topicVersion.addLocalizedWikiContent( strLanguage, content );
        }

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName );
        topic.setViewRole( strViewRole );
        topic.setEditRole( strEditRole );
        topic.setParentPageName( strParentPageName );

        request.getSession( ).setAttribute( MARK_TOPIC, topic );
        request.getSession( ).setAttribute( MARK_LATEST_VERSION, topicVersion );

        String strLanguage = getLanguage( request );
        String strContent = request.getParameter( Constants.PARAMETER_CONTENT + "_" + strLanguage );
        String strPageContent = new LuteceWikiParser( strContent, strPageName, null, strLanguage ).toString( );
        String strPageTitle = request.getParameter( Constants.PARAMETER_PAGE_TITLE + "_" + strLanguage );

        Map<String, Object> model = getModel( );
        model.put( MARK_RESULT, strPageContent );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_LATEST_VERSION, topicVersion );
        model.put( MARK_TOPIC_TITLE, strPageTitle );

        XPage page = getXPage( TEMPLATE_PREVIEW_WIKI, request.getLocale( ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        page.setExtendedPathLabel( getPageExtendedPath( topic, request ) );

        return page;
    }

    /**
     * Displays the history page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an error occurs
     */
    @View( VIEW_HISTORY )
    public XPage getHistory( HttpServletRequest request ) throws SiteMessageException, UserNotSignedException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName, MODE_VIEW );
        Map<String, Object> model = getModel( );
        Collection<TopicVersion> listTopicVersions = TopicVersionHome.findAllVersions( topic.getIdTopic( ) );

        fillUsersData( listTopicVersions );
        model.put( MARK_LIST_TOPIC_VERSION, listTopicVersions );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_EDIT_ROLE, RoleService.hasEditRole( request, topic ) );
        model.put( MARK_ADMIN_ROLE, RoleService.hasAdminRole( request ) );
        model.put( MARK_LANGUAGES_LIST, WikiLocaleService.getLanguages( ) );
        model.put( MARK_CURRENT_LANGUAGE, getLanguage( request ) );

        XPage page = getXPage( TEMPLATE_VIEW_HISTORY_WIKI, request.getLocale( ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        page.setExtendedPathLabel( getPageExtendedPath( topic, request ) );

        return page;
    }

    /**
     * Displays the diff page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an error occurs
     */
    @View( VIEW_DIFF )
    public XPage getDiff( HttpServletRequest request ) throws SiteMessageException, UserNotSignedException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName, MODE_VIEW );

        String strNewVersion = request.getParameter( Constants.PARAMETER_NEW_VERSION );
        String strOldVersion = request.getParameter( Constants.PARAMETER_OLD_VERSION );
        int nNewTopicVersion = Integer.parseInt( strNewVersion );
        int nOldTopicVersion = Integer.parseInt( strOldVersion );
        int nPrevTopicVersion = ( nOldTopicVersion == 0 ) ? nNewTopicVersion : nOldTopicVersion;

        TopicVersion newTopicVersion = TopicVersionHome.findByPrimaryKey( nNewTopicVersion );
        TopicVersion oldTopicVersion = TopicVersionHome.findByPrimaryKey( nPrevTopicVersion );

        String strLanguage = getLanguage( request );
        String strNewHtml = WikiService.instance( ).getWikiPage( strPageName, newTopicVersion, strLanguage );
        String strOldHtml = WikiService.instance( ).getWikiPage( strPageName, oldTopicVersion, strLanguage );
        String strNewSource = WikiService.instance( ).getPageSource( strPageName, newTopicVersion, strLanguage );
        String strOldSource = WikiService.instance( ).getPageSource( strPageName, oldTopicVersion, strLanguage );
        String strDiffHtml = DiffService.getDiff( strOldHtml, strNewHtml );
        String strDiffSource = DiffService.getDiff( strOldSource, strNewSource );

        Map<String, Object> model = getModel( );
        model.put( MARK_DIFF_HTML, strDiffHtml );
        model.put( MARK_DIFF_SOURCE, strDiffSource );
        model.put( MARK_TOPIC, topic );

        XPage page = getXPage( TEMPLATE_VIEW_DIFF_TOPIC_WIKI, request.getLocale( ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        page.setExtendedPathLabel( getPageExtendedPath( topic, request ) );

        return page;
    }

    /**
     * Deletes a wiki page
     *
     * @param request
     *            The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException
     *             if user not connected
     */
    @Action( ACTION_DELETE_PAGE )
    public XPage doDeleteTopic( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName );

        // Requires Admin role
        if ( RoleService.hasAdminRole( request ) )
        {
            TopicHome.remove( topic.getIdTopic( ) );
        }

        return redirectView( request, VIEW_HOME );
    }

    /**
     * Uploads an image
     * 
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws UserNotSignedException
     *             if the user is not signed
     */
    @Action( ACTION_UPLOAD_IMAGE )
    public XPage doUploadImage( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strName = request.getParameter( Constants.PARAMETER_IMAGE_NAME );
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        FileItem fileItem = multipartRequest.getFile( Constants.PARAMETER_IMAGE_FILE );
        Image image = new Image( );
        boolean bError = false;

        if ( ( fileItem == null ) || ( fileItem.getName( ) == null ) || "".equals( fileItem.getName( ) ) )
        {
            bError = true;
            addError( MESSAGE_FILE_MANDATORY, request.getLocale( ) );
        }

        if ( ( strName == null ) || strName.trim( ).equals( "" ) )
        {
            bError = true;
            addError( MESSAGE_NAME_MANDATORY, request.getLocale( ) );
        }

        if ( !bError )
        {
            image.setName( strName );
            image.setTopicId( Integer.parseInt( strTopicId ) );

            if ( ( fileItem != null ) && ( fileItem.getName( ) != null ) && !"".equals( fileItem.getName( ) ) )
            {
                image.setValue( fileItem.get( ) );
                image.setMimeType( fileItem.getContentType( ) );
            }
            else
            {
                image.setValue( null );
            }

            image.setWidth( 500 );
            image.setHeight( 500 );

            ImageHome.create( image );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName + ANCHOR_IMAGES );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
     * Manages the removal form of a image whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     * @throws SiteMessageException
     *             A site message
     */
    @Action( ACTION_CONFIRM_REMOVE_IMAGE )
    public XPage getConfirmRemoveImage( HttpServletRequest request ) throws SiteMessageException
    {
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_IMAGE_ID ) );
        UrlItem url = new UrlItem( AppPathService.getPortalUrl( ) );
        url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
        url.addParameter( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );
        url.addParameter( Constants.PARAMETER_ACTION, ACTION_REMOVE_IMAGE );
        url.addParameter( Constants.PARAMETER_IMAGE_ID, nId );

        SiteMessageService.setMessage( request, MESSAGE_CONFIRM_REMOVE_IMAGE, SiteMessage.TYPE_CONFIRMATION, url.getUrl( ) );

        return null;
    }

    /**
     * Handles the removal form of an image
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage images
     * @throws UserNotSignedException
     *             if the user is not signed
     */
    @Action( ACTION_REMOVE_IMAGE )
    public XPage doRemoveImage( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_IMAGE_ID ) );
        ImageHome.remove( nId );
        addInfo( MESSAGE_IMAGE_REMOVED, getLocale( request ) );

        Map<String, String> mapParameters = new ConcurrentHashMap<>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) + ANCHOR_IMAGES );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
     * Manages the removal form of a topic version whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     * @throws SiteMessageException
     *             A site message
     */
    @Action( ACTION_CONFIRM_REMOVE_VERSION )
    public XPage getConfirmRemoveVersion( HttpServletRequest request ) throws SiteMessageException
    {
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_TOPIC_VERSION_ID ) );
        UrlItem url = new UrlItem( AppPathService.getPortalUrl( ) );
        url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
        url.addParameter( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );
        url.addParameter( Constants.PARAMETER_ACTION, ACTION_REMOVE_VERSION );
        url.addParameter( Constants.PARAMETER_TOPIC_VERSION_ID, nId );

        SiteMessageService.setMessage( request, MESSAGE_CONFIRM_REMOVE_VERSION, SiteMessage.TYPE_CONFIRMATION, url.getUrl( ) );

        return null;
    }

    /**
     * Handles the removal form of a topic version
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage images
     * @throws UserNotSignedException
     *             if the user is not signed
     */
    @Action( ACTION_REMOVE_VERSION )
    public XPage doRemoveVersion( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );

        // requires admin role
        if ( RoleService.hasAdminRole( request ) )
        {
            int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_TOPIC_VERSION_ID ) );
            TopicVersionHome.remove( nId );
            addInfo( MESSAGE_VERSION_REMOVED, getLocale( request ) );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );

        return redirect( request, VIEW_HISTORY, mapParameters );
    }

    /**
     * Returns the image list as JSON
     * 
     * @param request
     *            The HTTP request
     * @return A JSON flow
     */
    @View( VIEW_LIST_IMAGES )
    public XPage getListImages( HttpServletRequest request )
    {
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        JSONArray array = new JSONArray( );

        if ( strTopicId != null )
        {
            int nTopicId = Integer.parseInt( strTopicId );
            List<Image> list = ImageHome.findByTopic( nTopicId );
            for ( Image image : list )
            {
                JSONObject jsonImage = new JSONObject( );
                jsonImage.accumulate( "id", image.getId( ) );
                jsonImage.accumulate( "name", image.getName( ) );
                array.add( jsonImage );
            }
        }

        return responseJSON( array.toString( ) );
    }

    // /////////////////// Utils ////////////////////////////
    /**
     * Checks the connected user
     *
     * @param request
     *            The HTTP request
     * @return The user
     * @throws UserNotSignedException
     *             if user not connected
     */
    private LuteceUser checkUser( HttpServletRequest request ) throws UserNotSignedException
    {
        LuteceUser user;

        if ( SecurityService.isAuthenticationEnable( ) )
        {
            user = SecurityService.getInstance( ).getRemoteUser( request );

            if ( user == null )
            {
                throw new UserNotSignedException( );
            }
        }
        else
        {
            user = new WikiAnonymousUser( );
        }

        return user;
    }

    /**
     * Gets the topic corresponding to the given page name
     *
     * @param request
     *            The HTTP request
     * @param strPageName
     *            The page name
     * @param nMode
     *            The mode VIEW vs EDIT
     * @return The topic
     * @throws SiteMessageException
     *             If an error occurs
     */
    private Topic getTopic( HttpServletRequest request, String strPageName, int nMode ) throws SiteMessageException, UserNotSignedException
    {
        Topic topic = TopicHome.findByPrimaryKey( strPageName );

        if ( topic == null )
        {
            SiteMessageService.setMessage( request, Constants.MESSAGE_PAGE_NOT_EXISTS, SiteMessage.TYPE_STOP );
        }
        else
        {
            String strRole;
            String strMessage;
            switch( nMode )
            {
                case MODE_EDIT:
                    strRole = topic.getEditRole( );
                    strMessage = Constants.MESSAGE_USER_NOT_IN_ROLE;
                    break;
                case MODE_VIEW:
                default:
                    strRole = topic.getViewRole( );
                    strMessage = Constants.MESSAGE_USER_NOT_IN_ROLE;
                    break;
            }

            if ( SecurityService.isAuthenticationEnable( ) && ( !Page.ROLE_NONE.equals( strRole ) ) )
            {
                LuteceUser user = SecurityService.getInstance( ).getRemoteUser( request );

                if ( user == null )
                {
                    throw new UserNotSignedException( );
                }

                if ( !SecurityService.getInstance( ).isUserInRole( request, strRole ) )
                {
                    SiteMessageService.setMessage( request, strMessage, SiteMessage.TYPE_STOP );
                }
            }
        }

        return topic;
    }

    /**
     * Gets topics visible for a given user
     *
     * @param request
     *            The HTTP request
     * @return The list of topics
     */
    private List<Topic> getTopicsForUser( HttpServletRequest request )
    {
        Collection<Topic> listTopicAll = TopicHome.getTopicsList( );
        List<Topic> listTopic;

        if ( SecurityService.isAuthenticationEnable( ) )
        {
            listTopic = new ArrayList<>( );

            for ( Topic topic : listTopicAll )
            {
                String strRole = topic.getViewRole( );

                if ( !Page.ROLE_NONE.equals( strRole ) )
                {
                    if ( SecurityService.getInstance( ).isUserInRole( request, strRole ) )
                    {
                        listTopic.add( topic );
                    }
                }
                else
                {
                    listTopic.add( topic );
                }
            }
        }
        else
        {
            listTopic = new ArrayList<>( listTopicAll );
        }

        return listTopic;
    }

    /**
     * Returns a reference list of pages for user
     * 
     * @param request
     *            The HTTP request
     * @param bFirstItem
     *            True if the reference list must have a first empty default item
     * @return The reference list
     */
    private ReferenceList getTopicsReferenceListForUser( HttpServletRequest request, boolean bFirstItem )
    {
        ReferenceList list = new ReferenceList( );

        if ( bFirstItem )
        {
            list.addItem( "", "" );
        }
        for ( Topic topic : getTopicsForUser( request ) )
        {
            list.addItem( topic.getPageName( ), getTopicTitle( request, topic ) );
        }

        return list;
    }

    /**
     * Builds the page title
     *
     * @param strPageName
     *            The page name
     * @return The page title
     */
    private String getPageTitle( String strPageName )
    {
        StringBuilder sbPath = new StringBuilder( );
        sbPath.append( AppPropertiesService.getProperty( PROPERTY_PAGE_TITLE ) ).append( ' ' ).append( strPageName );

        return sbPath.toString( );
    }

    /**
     * Builds the extended path for views other than page
     *
     * @param strViewTitle
     *            The view title
     * @return The list that content path info
     */
    private ReferenceList getViewExtendedPath( String strViewTitle )
    {
        String strWikiRootLabel = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_LABEL, AppPropertiesService.getProperty( PROPERTY_PAGE_PATH ) );
        String strWikiRootPageName = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_PAGENAME, PAGE_DEFAULT );

        ReferenceList list = new ReferenceList( );

        list.addItem( strWikiRootLabel, URL_VIEW_PAGE + strWikiRootPageName );
        list.addItem( strViewTitle, "" );

        return list;
    }

    /**
     * Builds the extended path for wiki page
     *
     * @param topic
     *            The topic page
     * @param request
     *            The request
     * @return The list that content path info
     */
    private ReferenceList getPageExtendedPath( Topic topic, HttpServletRequest request )
    {
        String strWikiRootLabel = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_LABEL, AppPropertiesService.getProperty( PROPERTY_PAGE_PATH ) );
        String strWikiRootPageName = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_PAGENAME, AppPropertiesService.getProperty( PAGE_DEFAULT ) );

        ReferenceList list = new ReferenceList( );
        ReferenceItem item;

        String strTopicTitle;
        String strTopicUrl;

        if ( !topic.getPageName( ).equals( strWikiRootPageName ) )
        {
            list.addItem( getTopicTitle( request, topic ), "" );
        }

        while ( topic != null && !topic.getParentPageName( ).isEmpty( ) && topic.getParentPageName( ) != null
                && !topic.getParentPageName( ).equals( strWikiRootPageName ) && !isNameInReferenceList( list, URL_VIEW_PAGE + topic.getParentPageName( ) ) )
        {
            topic = TopicHome.findByPrimaryKey( topic.getParentPageName( ) );

            if ( topic != null )
            {
                strTopicTitle = getTopicTitle( request, topic );
                strTopicUrl = URL_VIEW_PAGE + topic.getPageName( );

                if ( SecurityService.isAuthenticationEnable( ) && ( !Page.ROLE_NONE.equals( topic.getViewRole( ) ) ) )
                {
                    if ( !SecurityService.getInstance( ).isUserInRole( request, topic.getViewRole( ) ) )
                    {
                        strTopicTitle = I18nService.getLocalizedString( MESSAGE_PATH_HIDDEN, getLocale( request ) );
                        strTopicUrl = "";
                    }
                }

                item = new ReferenceItem( );
                item.setCode( strTopicTitle );
                item.setName( strTopicUrl );

                list.add( 0, item );
            }
        }

        item = new ReferenceItem( );
        item.setCode( strWikiRootLabel );
        item.setName( URL_VIEW_PAGE + strWikiRootPageName );

        list.add( 0, item );

        return list;
    }

    /**
     * Checks if name is already in an item of the reference list
     * 
     * @param list
     *            The reference list
     * @param name
     *            The name
     * @return true if the name is in list and false otherwise
     */
    private boolean isNameInReferenceList( ReferenceList list, String name )
    {
        for ( ReferenceItem item : list )
        {
            if ( item.getName( ).equals( name ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Fills all versions with users infos
     * 
     * @param listVersions
     *            The version
     */
    private void fillUsersData( Collection<TopicVersion> listVersions )
    {
        for ( TopicVersion version : listVersions )
        {
            fillUserData( version );
        }
    }

    /**
     * Fills the version with users infos
     * 
     * @param version
     *            The version
     */
    private void fillUserData( TopicVersion version )
    {
        String strUserId = version.getLuteceUserId( );

        LuteceUser user = SecurityService.getInstance( ).getUser( strUserId );

        if ( user != null )
        {
            version.setUserName( user.getUserInfo( LuteceUser.NAME_GIVEN ) + " " + user.getUserInfo( LuteceUser.NAME_FAMILY ) );
            version.setUserAvatarUrl( AvatarService.getAvatarUrl( user.getEmail( ) ) );
        }
        else
        {
            version.setUserAvatarUrl( AvatarService.getAvatarUrl( strUserId ) );
        }

        version.setUserPseudo( UserPreferencesService.instance( ).getNickname( strUserId ) );
    }

    /**
     * Gets the current page URL from the request
     * 
     * @param request
     *            The request
     * @return The URL
     */
    private String getPageUrl( HttpServletRequest request )
    {
        return request.getRequestURI( ).substring( request.getContextPath( ).length( ) + 1 ) + "?" + request.getQueryString( );
    }

    /**
     * Returns if the plugin Extend is available
     * 
     * @return true if extend is installed and activated otherwise false
     */
    private boolean isExtend( )
    {
        Plugin plugin = PluginService.getPlugin( PLUGIN_EXTEND );
        return ( ( plugin != null ) && plugin.isInstalled( ) );
    }

    /**
     * Stores the current selected language in the user's session
     * 
     * @param request
     *            The request
     * @param strLanguage
     *            The language
     */
    private void setLanguage( HttpServletRequest request, String strLanguage )
    {
        Locale userSelectedLocale = new Locale( strLanguage );
        if ( LocaleService.isSupported( userSelectedLocale ) )
        {
            LocaleService.setUserSelectedLocale( request, userSelectedLocale );
        }
    }

    /**
     * Retrieves the current selected language from the user's session
     * 
     * @param request
     *            The request
     * @return The Language
     */
    private String getLanguage( HttpServletRequest request )
    {
        String strLanguage = null;

        if ( request.getParameter( PARAMETER_LANGUAGE ) != null )
        {
            // consider the language parameter in the URL if exists
            strLanguage = request.getParameter( PARAMETER_LANGUAGE );
            setLanguage( request, strLanguage );
        }

        return LocaleService.getContextUserLocale( request ).getLanguage( );
    }

    /**
     * Returns a topic title
     * 
     * @param topic
     *            The topic
     * @param strLanguage
     *            The language
     * @return The title
     */
    private String getTopicTitle( Topic topic, String strLanguage )
    {
        TopicVersion version = TopicVersionHome.findLastVersion( topic.getIdTopic( ) );
        if ( version != null && StringUtils.isNotEmpty( version.getWikiContent( strLanguage ).getPageTitle( ) ) )
        {
            return version.getWikiContent( strLanguage ).getPageTitle( );
        }
        else
        {
            return topic.getPageName( );
        }
    }

    /**
     * Returns a topic title
     * 
     * @param request
     *            The HTTP request
     * @param topic
     *            The topic
     * @return The title
     */
    private String getTopicTitle( HttpServletRequest request, Topic topic )
    {
        return getTopicTitle( topic, getLanguage( request ) );
    }
}
