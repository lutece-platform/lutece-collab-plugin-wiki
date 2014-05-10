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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;


public class TopicBusinessTest extends LuteceTestCase
{
    private final static int IDTOPIC1 = 1;
    private final static int IDTOPIC2 = 2;
    private final static int NAMESPACE1 = 1;
    private final static int NAMESPACE2 = 2;
    private final static String PAGENAME1 = "PageName1";
    private final static String PAGENAME2 = "PageName2";
    private final static String PAGETITLE1 = "PageTitle1";
    private final static String PAGETITLE2 = "PageTitle2";
    private final static String VIEWROLE1 = "VIEWROLE1";
    private final static String VIEWROLE2 = "VIEWROLE2";
    private final static String EDITROLE1 = "EDITROLE1";
    private final static String EDITROLE2 = "EDITROLE2";

    public void testBusiness(  )
    {
        Plugin plugin = PluginService.getPlugin( "wiki" );

        // Initialize an object
        Topic topic = new Topic(  );
        topic.setIdTopic( IDTOPIC1 );
        topic.setNamespace( NAMESPACE1 );
        topic.setPageName( PAGENAME1 );
        topic.setPageTitle( PAGETITLE1 );
        topic.setViewRole( VIEWROLE1 );
        topic.setEditRole( EDITROLE1 );

        // Create test
        TopicHome.create( topic, plugin );

        Topic topicStored = TopicHome.findByPrimaryKey( topic.getIdTopic(  ), plugin );
        assertEquals( topicStored.getIdTopic(  ), topic.getIdTopic(  ) );
        assertEquals( topicStored.getNamespace(  ), topic.getNamespace(  ) );
        assertEquals( topicStored.getPageName(  ), topic.getPageName(  ) );
        assertEquals( topicStored.getPageTitle(  ), topic.getPageTitle(  ) );
        assertEquals( topicStored.getViewRole(  ), topic.getViewRole(  ) );
        assertEquals( topicStored.getEditRole(  ), topic.getEditRole(  ) );

        // Update test
        topic.setIdTopic( IDTOPIC2 );
        topic.setNamespace( NAMESPACE2 );
        topic.setPageName( PAGENAME2 );
        topic.setPageTitle( PAGETITLE2 );
        topic.setViewRole( VIEWROLE2 );
        topic.setEditRole( EDITROLE2 );

        TopicHome.update( topic, plugin );
        topicStored = TopicHome.findByPrimaryKey( topic.getIdTopic(  ), plugin );
        assertEquals( topicStored.getIdTopic(  ), topic.getIdTopic(  ) );
        assertEquals( topicStored.getNamespace(  ), topic.getNamespace(  ) );
        assertEquals( topicStored.getPageName(  ), topic.getPageName(  ) );
        assertEquals( topicStored.getPageTitle(  ), topic.getPageTitle(  ) );
        assertEquals( topicStored.getViewRole(  ), topic.getViewRole(  ) );
        assertEquals( topicStored.getEditRole(  ), topic.getEditRole(  ) );

        // List test
        TopicHome.getTopicsList( plugin );

        // Delete test
        TopicHome.remove( topic.getIdTopic(  ), plugin );
        topicStored = TopicHome.findByPrimaryKey( topic.getIdTopic(  ), plugin );
        assertNull( topicStored );
    }
}
