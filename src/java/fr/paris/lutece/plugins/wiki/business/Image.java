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
package fr.paris.lutece.plugins.wiki.business;

/**
 *
 * Image
 *
 */
public class Image
{
    private int _nId;
    private String _strName;
    private byte [ ] _byValue;
    private String _strMimeType;
    private int _nTopicId;
    private int _nWidth;
    private int _nHeight;

    /**
     *
     * @return the id of the image
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * set the id of the image
     * 
     * @param idImage
     *            the id of the image
     */
    public void setId( int idImage )
    {
        _nId = idImage;
    }

    /**
     * Returns the image name
     *
     * @return the image name
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Set the image name
     *
     * @param strName
     *            the image name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * get the image file value
     * 
     * @return the image file value
     */
    public byte [ ] getValue( )
    {
        return _byValue;
    }

    /**
     * set the image file value
     * 
     * @param value
     *            the file value
     */
    public void setValue( byte [ ] value )
    {
        _byValue = value;
    }

    /**
     * the image mime type
     * 
     * @return the image mime type
     */
    public String getMimeType( )
    {
        return _strMimeType;
    }

    /**
     * set the image mime type
     * 
     * @param mimeType
     *            the image mime type
     */
    public void setMimeType( String mimeType )
    {
        _strMimeType = mimeType;
    }

    /**
     * @return the image height
     */
    public int getTopicId( )
    {
        return _nTopicId;
    }

    /**
     * @param height
     *            the image height
     */
    public void setTopicId( int height )
    {
        _nTopicId = height;
    }

    /**
     * @return the image height
     */
    public int getHeight( )
    {
        return _nHeight;
    }

    /**
     * @param height
     *            the image height
     */
    public void setHeight( int height )
    {
        _nHeight = height;
    }

    /**
     * @return the image width
     */
    public int getWidth( )
    {
        return _nWidth;
    }

    /**
     * @param width
     *            the image width
     */
    public void setWidth( int width )
    {
        _nWidth = width;
    }
}
