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

import fr.paris.lutece.plugins.wiki.business.Image;
import fr.paris.lutece.plugins.wiki.business.ImageHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * AjaxImageServlet
 */
public class AjaxImageServlet extends HttpServlet
{
    private static Plugin _plugin = PluginService.getPlugin( Constants.PLUGIN_NAME );

    /**
     * {@inheritDoc }
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        JSONArray array = new JSONArray();

        if( strTopicId != null )
        {
            int nTopicId = Integer.parseInt(strTopicId);
            List<Image> list = ImageHome.findByTopic( nTopicId , _plugin );
            for( Image image : list )
            {
                JSONObject jsonImage = new JSONObject();
                jsonImage.accumulate( "id", image.getId() );
                jsonImage.accumulate( "name", image.getName() );
                array.add( jsonImage );
            }
        }
        response.setContentType( "application/json");
        ServletOutputStream out = response.getOutputStream();
        out.print( array.toString());
        out.flush();
        out.close();
    }

}
