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
package fr.paris.lutece.plugins.wiki.web;

import fr.paris.lutece.plugins.avatar.service.AvatarService;
import fr.paris.lutece.plugins.wiki.business.Image;
import fr.paris.lutece.plugins.wiki.business.ImageHome;
import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.plugins.wiki.business.TopicVersion;
import fr.paris.lutece.plugins.wiki.business.TopicVersionHome;
import fr.paris.lutece.plugins.wiki.service.DiffService;
import fr.paris.lutece.plugins.wiki.service.WikiUtils;
import fr.paris.lutece.plugins.wiki.service.parser.LuteceWikiParser;
import fr.paris.lutece.plugins.wiki.utils.auth.WikiAnonymousUser;
import fr.paris.lutece.portal.business.page.Page;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.service.content.XPageAppService;
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
import fr.paris.lutece.portal.web.resource.ExtendableResourcePluginActionManager;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.util.xml.XmlUtil;

import org.apache.commons.fileupload.FileItem;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * This class provides a simple implementation of an XPage
 */
@Controller( xpageName = "wiki", pageTitleProperty = "wiki.pageTitle", pagePathProperty = "wiki.pagePathLabel" )
public class WikiApp extends MVCApplication
{
    public static final int ACTION_VIEW = 1;
    private static final String TEMPLATE_MODIFY_WIKI = "skin/plugins/wiki/modify_page.html";
    private static final String TEMPLATE_VIEW_WIKI = "skin/plugins/wiki/view_page.html";
    private static final String TEMPLATE_VIEW_HISTORY_WIKI = "skin/plugins/wiki/history_page.html";
    private static final String TEMPLATE_VIEW_DIFF_TOPIC_WIKI = "skin/plugins/wiki/diff_topic.html";
    private static final String TEMPLATE_LIST_WIKI = "skin/plugins/wiki/list_wiki.html";
    private static final String TEMPLATE_SEARCH_WIKI = "skin/plugins/wiki/search_wiki.html";
    private static final String BEAN_SEARCH_ENGINE = "wiki.wikiSearchEngine";
    private static final String PROPERTY_PAGE_PATH = "wiki.pagePathLabel";
    private static final String PROPERTY_PAGE_TITLE = "wiki.pageTitle";
    private static final String PROPERTY_DEFAULT_RESULT_PER_PAGE = "wiki.search_wiki.itemsPerPage";
    private static final String PROPERTY_PATH_LIST = "wiki.path.list";
    private static final String PROPERTY_TITLE_LIST = "wiki.title.list";
    private static final String PROPERTY_PATH_SEARCH = "wiki.path.search";
    private static final String PROPERTY_TITLE_SEARCH = "wiki.title.search";
    private static final String PROPERTY_PATH_MODIFY = "wiki.path.labelModify";
    private static final String MARK_TOPIC = "topic";
    private static final String MARK_LIST_TOPIC = "list_topic";
    private static final String MARK_LATEST_VERSION = "lastVersion";
    private static final String MARK_DIFF = "diff";
    private static final String MARK_RESULT = "result";
    private static final String MARK_LIST_TOPIC_VERSION = "listTopicVersion";
    private static final String MARK_PAGE_ROLES_LIST = "page_roles_list";
    private static final String MARK_QUERY = "query";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_IMAGES_LIST = "images_list";
    private static final String VIEW_HOME = "home";
    private static final String VIEW_PAGE = "page";
    private static final String VIEW_MODIFY_PAGE = "modifyPage";
    private static final String VIEW_HISTORY = "history";
    private static final String VIEW_SEARCH = "search";
    private static final String VIEW_DIFF = "diff";
    private static final String ACTION_NEW_PAGE = "newPage";
    private static final String ACTION_EDIT_PAGE = "editPage";
    private static final String ACTION_MODIFY_PAGE = "modifyPage";
    private static final String ACTION_DELETE_PAGE = "deletePage";
    private static final String ACTION_REMOVE_IMAGE = "removeImage";
    private static final String ACTION_CONFIRM_REMOVE_IMAGE = "confirmRemoveImage";
    private static final String ACTION_REMOVE_VERSION = "removeVersion";
    private static final String ACTION_CONFIRM_REMOVE_VERSION = "confirmRemoveVersion";
    private static final String ACTION_UPLOAD_IMAGE = "uploadImage";
    private static final String TAG_PAGE_LINK = "page_link";
    private static final String TAG_PAGE_NAME = "page-name";
    private static final String TAG_PAGE_URL = "page-url";
    private static final String JSP_PAGE_PORTAL = "jsp/site/Portal.jsp";
    private static final String MESSAGE_IMAGE_REMOVED = "wiki.message.image.removed";
    private static final String MESSAGE_CONFIRM_REMOVE_IMAGE = "wiki.message.confirmRemoveImage";
    private static final String MESSAGE_NAME_MANDATORY = "wiki.message.error.name.notNull";
    private static final String MESSAGE_FILE_MANDATORY = "wiki.message.error.file.notNull";
    private static final String MESSAGE_CONFIRM_REMOVE_VERSION = "wiki.message.confirmRemoveVersion";
    private static final String MESSAGE_VERSION_REMOVED = "wiki.message.version.removed";
    private static final String ANCHOR_IMAGES = "#images";

    // private fields
    private final Plugin _plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );
    private String _strCurrentPageIndex;
    private int _nDefaultItemsPerPage;
    private int _nItemsPerPage;

    /**
     * Gets the Home page
     *
     * @param request The HTTP request
     * @return The XPage
     */
    @View( value = VIEW_HOME, defaultView = true )
    public XPage home( HttpServletRequest request )
    {
        XPage page = getTopicsListPage( request );
        page.setTitle( getPageTitle( I18nService.getLocalizedString( PROPERTY_TITLE_LIST, request.getLocale(  ) ) ) );
        page.setXmlExtendedPathLabel( getXmlExtendedPath( I18nService.getLocalizedString( PROPERTY_PATH_LIST,
                    request.getLocale(  ) ) ) );

        return page;
    }

    /**
     * Search page
     *
     * @param request The request
     * @return Thye XPage
     */
    @View( VIEW_SEARCH )
    public XPage search( HttpServletRequest request )
    {
        String strQuery = request.getParameter( Constants.PARAMETER_QUERY );
        String strPortalUrl = AppPathService.getPortalUrl(  );

        UrlItem urlWikiXpage = new UrlItem( strPortalUrl );
        urlWikiXpage.addParameter( XPageAppService.PARAM_XPAGE_APP, Constants.PLUGIN_NAME );
        urlWikiXpage.addParameter( Constants.PARAMETER_ACTION, Constants.PARAMETER_ACTION_SEARCH );

        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_RESULT_PER_PAGE, 10 );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );

        SearchEngine engine = (SearchEngine) SpringContextService.getBean( BEAN_SEARCH_ENGINE );
        List<SearchResult> listResults = engine.getSearchResults( strQuery, request );

        Paginator paginator = new Paginator( listResults, _nItemsPerPage, urlWikiXpage.getUrl(  ),
                Constants.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

        Map<String, Object> model = getModel(  );
        model.put( MARK_RESULT, paginator.getPageItems(  ) );
        model.put( MARK_QUERY, strQuery );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );

        XPage page = getXPage( TEMPLATE_SEARCH_WIKI, request.getLocale(  ), model );
        page.setTitle( getPageTitle( I18nService.getLocalizedString( PROPERTY_TITLE_SEARCH, request.getLocale(  ) ) ) );
        page.setXmlExtendedPathLabel( getXmlExtendedPath( I18nService.getLocalizedString( PROPERTY_PATH_SEARCH,
                    request.getLocale(  ) ) ) );

        return page;
    }

    /**
     * Display all pages
     *
     * @param request The HTTP request
     * @return The page
     */
    private XPage getTopicsListPage( HttpServletRequest request )
    {
        List<Topic> listTopic = getTopicsForUser( request );

        Map<String, Object> model = getModel(  );
        model.put( MARK_LIST_TOPIC, listTopic );

        return getXPage( TEMPLATE_LIST_WIKI, request.getLocale(  ), model );
    }

    /**
     * Display the view page
     *
     * @param request The HTTP request
     * @return The XPage
     * @throws SiteMessageException if an error occurs
     */
    @View( VIEW_PAGE )
    public XPage view( HttpServletRequest request ) throws SiteMessageException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName );
        TopicVersion version = TopicVersionHome.findLastVersion( topic.getIdTopic(  ), _plugin );
        fillUserData( version );
        String strWikiResult = new LuteceWikiParser( version.getWikiContent(  ) , getPageUrl (request ) ).toString(  );
        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_RESULT, strWikiResult );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_LATEST_VERSION, version );

        XPage page = getXPage( TEMPLATE_VIEW_WIKI, request.getLocale(  ), model );
        page.setTitle( getPageTitle( topic.getPageTitle(  ) ) );
        page.setXmlExtendedPathLabel( getXmlExtendedPath( topic.getPageTitle(  ) ) );

        return page;
    }

    /**
     * Create a new Page
     *
     * @param request The request
     * @return The XPage
     * @throws UserNotSignedException if the user is not signed
     * @throws java.io.UnsupportedEncodingException if an encoding exception occurs
     */
    @Action( ACTION_NEW_PAGE )
    public XPage newPage( HttpServletRequest request )
        throws UserNotSignedException, UnsupportedEncodingException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strPageTitle = strPageName;
        strPageName = WikiUtils.normalize( strPageName );
        
        Topic topic =TopicHome.findByPrimaryKey( strPageName, _plugin);
        if( topic == null )
        {
            topic = new Topic();
            topic.setPageName( strPageName );
            topic.setPageTitle( strPageTitle );
            topic.setViewRole( Page.ROLE_NONE );
            topic.setViewRole( Page.ROLE_NONE );

            TopicHome.create( topic, _plugin );
            
        }

        Map<String, String> mapParameters = new HashMap<String, String>(  );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName );
        mapParameters.put( Constants.PARAMETER_PAGE_TITLE, URLEncoder.encode( strPageTitle, "UTF-8" ) );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }


    /**
     * Display the Edit page
     * @param request The HTTP request
     * @return The page
     * @throws UserNotSignedException if the user is not signed
     * @throws java.io.UnsupportedEncodingException if an encoding exception occurs
     */
    @Action( ACTION_EDIT_PAGE )
    public XPage edit( HttpServletRequest request ) throws UserNotSignedException, UnsupportedEncodingException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Map<String, String> mapParameters = new HashMap<String, String>(  );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, URLEncoder.encode( strPageName, "UTF-8" ) );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
     * Display the edit mode
     *
     * @param request The HTTP request
     * @return The XPage
     * @throws SiteMessageException if an exception occurs
     */
    @View( VIEW_MODIFY_PAGE )
    public XPage modify( HttpServletRequest request ) throws SiteMessageException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName );
        TopicVersion topicVersion = TopicVersionHome.findLastVersion( topic.getIdTopic(  ), _plugin );
        List<Image> listImages = ImageHome.findByTopic( topic.getIdTopic(  ), _plugin );
        topicVersion.setWikiContent( LuteceWikiParser.renderWiki( topicVersion.getWikiContent() ));
        Map<String, Object> model = getModel(  );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_LATEST_VERSION, topicVersion );
        model.put( MARK_PAGE_ROLES_LIST, RoleHome.getRolesList(  ) );
        model.put( MARK_IMAGES_LIST, listImages );
        ExtendableResourcePluginActionManager.fillModel( request, null, model,
            Integer.toString( topic.getIdTopic(  ) ), Topic.RESOURCE_TYPE );

        XPage page = getXPage( TEMPLATE_MODIFY_WIKI, request.getLocale(  ), model );
        page.setTitle( getPageTitle( strPageName ) );

        String strPath = strPageName + I18nService.getLocalizedString( PROPERTY_PATH_MODIFY, request.getLocale(  ) );
        page.setXmlExtendedPathLabel( getXmlExtendedPath( strPath ) );

        return page;
    }

    /**
     * Modify a wikipage
     *
     * @param request The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException If user not connected
     */
    @Action( ACTION_MODIFY_PAGE )
    public XPage doModifyPage( HttpServletRequest request )
        throws UserNotSignedException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strPageTitle = request.getParameter( Constants.PARAMETER_PAGE_TITLE );
        String strContent = request.getParameter( Constants.PARAMETER_CONTENT );
        String strPreviousVersionId = request.getParameter( Constants.PARAMETER_PREVIOUS_VERSION_ID );
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        String strComment = request.getParameter( Constants.PARAMETER_MODIFICATION_COMMENT );
        String strViewRole = request.getParameter( Constants.PARAMETER_VIEW_ROLE );
        String strEditRole = request.getParameter( Constants.PARAMETER_EDIT_ROLE );

        LuteceUser user = checkUser( request );
        int nPreviousVersionId = Integer.parseInt( strPreviousVersionId );
        int nTopicId = Integer.parseInt( strTopicId );
        TopicVersionHome.modifyContentOnly( nTopicId, user.getName(  ), strComment, strContent, nPreviousVersionId,
            _plugin );

        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );

        topic.setPageTitle( strPageTitle );
        topic.setViewRole( strViewRole );
        topic.setEditRole( strEditRole );
        TopicHome.update( topic, _plugin );

        Map<String, String> mapParameters = new HashMap<String, String>(  );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName );

        return redirect( request, VIEW_PAGE, mapParameters );
    }

    /**
     * Display the history page
     *
     * @param request The HTTP request
     * @return The XPage
     * @throws SiteMessageException if an error occurs
     */
    @View( VIEW_HISTORY )
    public XPage viewHistory( HttpServletRequest request )
        throws SiteMessageException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName );
        Map<String, Object> model = getModel(  );
        Collection<TopicVersion> listTopicVersions = TopicVersionHome.findAllVersions( topic.getIdTopic(  ), _plugin );

        fillUsersData( listTopicVersions );
        model.put( MARK_LIST_TOPIC_VERSION, listTopicVersions );
        model.put( MARK_TOPIC, topic );

        XPage page = getXPage( TEMPLATE_VIEW_HISTORY_WIKI, request.getLocale(  ), model );
        page.setTitle( getPageTitle( strPageName ) );
        page.setXmlExtendedPathLabel( getXmlExtendedPath( strPageName ) );

        return page;
    }

    /**
     * Display the diff page
     *
     * @param request The HTTP request
     * @return The XPage
     * @throws SiteMessageException if an error occurs
     */
    @View( VIEW_DIFF )
    public XPage viewDiff( HttpServletRequest request )
        throws SiteMessageException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName );
        String strNewVersion = request.getParameter( Constants.PARAMETER_NEW_VERSION );
        String strOldVersion = request.getParameter( Constants.PARAMETER_OLD_VERSION );
        int nNewTopicVersion = Integer.parseInt( strNewVersion );
        int nOldTopicVersion = Integer.parseInt( strOldVersion );

        XPage page = new XPage(  );
        page.setContent( viewTopicDiff( request, topic, nNewTopicVersion, nOldTopicVersion ) );
        page.setTitle( getPageTitle( strPageName ) );
        page.setXmlExtendedPathLabel( getXmlExtendedPath( strPageName ) );

        return page;
    }

    /**
     * View Diff
     *
     * @param request The HTTP request
     * @param topic The topic
     * @param nNewTopicVersion The new version
     * @param nOldTopicVersion The old version
     * @return The page
     */
    private String viewTopicDiff( HttpServletRequest request, Topic topic, int nNewTopicVersion, int nOldTopicVersion )
    {
        int nPrevTopicVersion = ( nOldTopicVersion == 0 ) ? nNewTopicVersion : nOldTopicVersion;
        TopicVersion newTopicVersion = TopicVersionHome.findByPrimaryKey( nNewTopicVersion, _plugin );
        TopicVersion oldTopicVersion = TopicVersionHome.findByPrimaryKey( nPrevTopicVersion, _plugin );

        String strNewHtml = LuteceWikiParser.renderXHTML( newTopicVersion.getWikiContent(  ) );
        String strOldHtml = LuteceWikiParser.renderXHTML( oldTopicVersion.getWikiContent(  ) );
        String strDiff = DiffService.getDiff( strOldHtml, strNewHtml );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_DIFF, strDiff );
        model.put( MARK_TOPIC, topic );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_DIFF_TOPIC_WIKI, request.getLocale(  ),
                model );

        return template.getHtml(  );
    }

    /**
     * Delete a wikipage
     *
     * @param request The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException if user not connected
     */
    @Action( ACTION_DELETE_PAGE )
    public XPage doDeletePage( HttpServletRequest request )
        throws UserNotSignedException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );
        TopicHome.remove( topic.getIdTopic(  ), _plugin );

        return redirectView( request, VIEW_HOME );
    }

    /**
     * Upload an image
     * @param request The HTTP request
     * @return The XPage
     * @throws UserNotSignedException if the user is not signed 
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
        Image image = new Image(  );
        boolean bError = false;

        if ( ( fileItem == null ) || ( fileItem.getName(  ) == null ) || "".equals( fileItem.getName(  ) ) )
        {
            bError = true;
            addError( MESSAGE_FILE_MANDATORY, request.getLocale(  ) );
        }

        if ( ( strName == null ) || strName.trim(  ).equals( "" ) )
        {
            bError = true;
            addError( MESSAGE_NAME_MANDATORY, request.getLocale(  ) );
        }

        if ( !bError )
        {
            image.setName( strName );
            image.setTopicId( Integer.parseInt( strTopicId ) );

            if ( ( fileItem != null ) && ( fileItem.getName(  ) != null ) && !"".equals( fileItem.getName(  ) ) )
            {
                image.setValue( fileItem.get(  ) );
                image.setMimeType( fileItem.getContentType(  ) );
            }
            else
            {
                image.setValue( null );
            }

            image.setWidth( 500 );
            image.setHeight( 500 );

            ImageHome.create( image, _plugin );
        }

        Map<String, String> mapParameters = new HashMap<String, String>(  );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName + ANCHOR_IMAGES );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
      * Manages the removal form of a image whose identifier is in the http
      * request
      *
      * @param request The Http request
      * @return the html code to confirm
      * @throws fr.paris.lutece.portal.service.message.SiteMessageException
      */
    @Action( ACTION_CONFIRM_REMOVE_IMAGE )
    public XPage getConfirmRemoveImage( HttpServletRequest request )
        throws SiteMessageException
    {
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_IMAGE_ID ) );
        UrlItem url = new UrlItem( JSP_PAGE_PORTAL );
        url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
        url.addParameter( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );
        url.addParameter( Constants.PARAMETER_ACTION, ACTION_REMOVE_IMAGE );
        url.addParameter( Constants.PARAMETER_IMAGE_ID, nId );

        SiteMessageService.setMessage( request, MESSAGE_CONFIRM_REMOVE_IMAGE, SiteMessage.TYPE_CONFIRMATION,
            url.getUrl(  ) );

        return null;
    }

    /**
     * Handles the removal form of a image
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage images
     * @throws UserNotSignedException if the user is not signed
     */
    @Action( ACTION_REMOVE_IMAGE )
    public XPage doRemoveImage( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_IMAGE_ID ) );
        ImageHome.remove( nId, _plugin );
        addInfo( MESSAGE_IMAGE_REMOVED, getLocale( request ) );

        Map<String, String> mapParameters = new HashMap<String, String>(  );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) + ANCHOR_IMAGES );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
      * Manages the removal form of a image whose identifier is in the http
      * request
      *
      * @param request The Http request
      * @return the html code to confirm
      * @throws fr.paris.lutece.portal.service.message.SiteMessageException
      */
    @Action( ACTION_CONFIRM_REMOVE_VERSION )
    public XPage getConfirmRemoveVersion( HttpServletRequest request )
        throws SiteMessageException
    {
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_TOPIC_VERSION_ID ) );
        UrlItem url = new UrlItem( JSP_PAGE_PORTAL );
        url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
        url.addParameter( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );
        url.addParameter( Constants.PARAMETER_ACTION, ACTION_REMOVE_VERSION );
        url.addParameter( Constants.PARAMETER_TOPIC_VERSION_ID , nId );

        SiteMessageService.setMessage( request, MESSAGE_CONFIRM_REMOVE_VERSION, SiteMessage.TYPE_CONFIRMATION,
            url.getUrl(  ) );

        return null;
    }

    /**
     * Handles the removal form of a image
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage images
     * @throws UserNotSignedException if the user is not signed
     */
    @Action( ACTION_REMOVE_VERSION )
    public XPage doRemoveVersion( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_TOPIC_VERSION_ID ) );
        TopicVersionHome.remove( nId, _plugin );
        addInfo( MESSAGE_VERSION_REMOVED, getLocale( request ) );

        Map<String, String> mapParameters = new HashMap<String, String>(  );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME )  );

        return redirect( request, VIEW_HISTORY, mapParameters );
    }


    /////////////////////  Utils ////////////////////////////
    /**
     * Check the connected user
     *
     * @param request The HTTP request
     * @return The user
     * @throws UserNotSignedException if user not connected
     */
    private LuteceUser checkUser( HttpServletRequest request )
        throws UserNotSignedException
    {
        LuteceUser user;

        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            user = SecurityService.getInstance(  ).getRemoteUser( request );

            if ( user == null )
            {
                throw new UserNotSignedException(  );
            }
        }
        else
        {
            user = new WikiAnonymousUser(  );
        }

        return user;
    }

    /**
     * Gets the topic corresponding to the given page name
     *
     * @param request The HTTP request
     * @param strPageName The page name
     * @return The topic
     * @throws SiteMessageException If an error occurs
     */
    private Topic getTopic( HttpServletRequest request, String strPageName )
        throws SiteMessageException
    {
        Topic topic = TopicHome.findByPrimaryKey( strPageName, _plugin );

        if ( topic == null )
        {
            SiteMessageService.setMessage( request, Constants.MESSAGE_PAGE_NOT_EXISTS, SiteMessage.TYPE_STOP );
        }
        else
        {
            String strRole = topic.getViewRole(  );

            if ( SecurityService.isAuthenticationEnable(  ) && ( !Page.ROLE_NONE.equals( strRole ) ) )
            {
                if ( !SecurityService.getInstance(  ).isUserInRole( request, strRole ) )
                {
                    SiteMessageService.setMessage( request, Constants.MESSAGE_USER_NOT_IN_ROLE, SiteMessage.TYPE_STOP );
                }
            }
        }

        return topic;
    }

    /**
     * Gets topics visible for a given user
     *
     * @param request The HTTP request
     * @return The list of topics
     */
    private List<Topic> getTopicsForUser( HttpServletRequest request )
    {
        Collection<Topic> listTopicAll = TopicHome.getTopicsList( _plugin );
        List<Topic> listTopic;

        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            listTopic = new ArrayList<Topic>(  );

            for ( Topic topic : listTopicAll )
            {
                String strRole = topic.getViewRole(  );

                if ( !Page.ROLE_NONE.equals( strRole ) )
                {
                    if ( SecurityService.getInstance(  ).isUserInRole( request, strRole ) )
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
            listTopic = new ArrayList<Topic>( listTopicAll );
        }

        return listTopic;
    }

    /**
     * Build the page title
     *
     * @param strPageName The page name
     * @return The page title
     */
    private String getPageTitle( String strPageName )
    {
        StringBuilder sbPath = new StringBuilder(  );
        sbPath.append( AppPropertiesService.getProperty( PROPERTY_PAGE_TITLE ) );
        sbPath.append( " " );
        sbPath.append( strPageName );

        return sbPath.toString(  );
    }

    /**
     * Build XML path infos
     *
     * @param strPageName The page name
     * @return The XML that content path info
     */
    private String getXmlExtendedPath( String strPageName )
    {
        StringBuffer sbXml = new StringBuffer(  );
        XmlUtil.beginElement( sbXml, TAG_PAGE_LINK );
        XmlUtil.addElement( sbXml, TAG_PAGE_NAME, AppPropertiesService.getProperty( PROPERTY_PAGE_PATH ) );
        XmlUtil.addElement( sbXml, TAG_PAGE_URL, "page=wiki" );
        XmlUtil.endElement( sbXml, TAG_PAGE_LINK );
        XmlUtil.beginElement( sbXml, TAG_PAGE_LINK );
        XmlUtil.addElement( sbXml, TAG_PAGE_NAME, strPageName );
        XmlUtil.addElement( sbXml, TAG_PAGE_URL, "" );
        XmlUtil.endElement( sbXml, TAG_PAGE_LINK );

        return sbXml.toString(  );
    }

    private void fillUsersData( Collection<TopicVersion> listTopicVersions )
    {
        for ( TopicVersion version : listTopicVersions )
        {
            fillUserData( version );
        }
    }

    private void fillUserData( TopicVersion version )
    {
        String strUserId = version.getLuteceUserId(  );
        LuteceUser user = SecurityService.getInstance(  ).getUser( strUserId );

        if ( user != null )
        {
            version.setUserName( user.getUserInfo( LuteceUser.NAME_GIVEN ) + " " +
                user.getUserInfo( LuteceUser.NAME_FAMILY ) );
            version.setUserAvatarUrl( AvatarService.getAvatarUrl( user.getEmail(  ) ) );
        }
        else
        {
            version.setUserAvatarUrl( AvatarService.getAvatarUrl( strUserId ) );
        }

        version.setUserPseudo( UserPreferencesService.instance(  ).getNickname( strUserId ) );
    }
    
    private String  getPageUrl ( HttpServletRequest request )
    {
        return request.getRequestURI().substring(request.getContextPath().length() + 1) + "?" + request.getQueryString();
    }
}
