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
 */package fr.paris.lutece.plugins.wiki.service;

import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.plugins.wiki.web.Constants;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.resource.IExtendableResource;
import fr.paris.lutece.portal.service.resource.IExtendableResourceService;
import java.util.Locale;

/**
 * Wiki Extendable Resource Service
 */
public class WikiExtendableResourceService implements IExtendableResourceService
{

    private static final String RESOURCE_TYPE_DESCRIPTION = "wiki.extend.resourceType";
    private static Plugin _plugin = PluginService.getPlugin(Constants.PLUGIN_NAME);

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isInvoked(String strResourceType)
    {
        return Topic.RESOURCE_TYPE.equalsIgnoreCase(strResourceType);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public IExtendableResource getResource(String strIdResource, String strResourceType)
    {
        try
        {
            int nIdTopic = Integer.parseInt(strIdResource);
            return TopicHome.findByPrimaryKey(nIdTopic, _plugin);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResourceType()
    {
        return Topic.RESOURCE_TYPE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResourceTypeDescription(Locale locale)
    {
        return I18nService.getLocalizedString(RESOURCE_TYPE_DESCRIPTION, locale);
    }
}
