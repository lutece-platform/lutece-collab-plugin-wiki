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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class provides Data Access methods for TopicVersion objects
 */
public final class TopicVersionDAO implements ITopicVersionDAO {
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_topic_version ) FROM wiki_topic_version";
    private static final String SQL_QUERY_SELECT = "SELECT id_topic_version, edit_comment, id_topic, lutece_user_id, date_edition, id_topic_version_previous, is_published FROM wiki_topic_version WHERE id_topic_version = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO wiki_topic_version ( id_topic_version, edit_comment, id_topic, lutece_user_id, date_edition, id_topic_version_previous, is_published ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM wiki_topic_version WHERE id_topic_version = ? ";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_topic_version, edit_comment, id_topic, lutece_user_id, date_edition, id_topic_version_previous, is_published FROM wiki_topic_version";
    private static final String SQL_QUERY_INSERT_MODIFICATION = "INSERT INTO wiki_topic_version ( id_topic_version, edit_comment, id_topic, lutece_user_id, date_edition, id_topic_version_previous, is_published ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_SELECT_LAST_BY_TOPIC_ID = "SELECT id_topic_version, edit_comment, id_topic, lutece_user_id, date_edition, id_topic_version_previous, is_published FROM wiki_topic_version WHERE id_topic = ?  ORDER BY  date_edition DESC LIMIT 1";
    private static final String SQL_QUERY_SELECT_BY_TOPIC_ID = "SELECT id_topic_version, edit_comment, id_topic, lutece_user_id, date_edition, id_topic_version_previous, is_published FROM wiki_topic_version WHERE id_topic = ?  ORDER BY  date_edition DESC ";
    private static final String SQL_QUERY_DELETE_BY_TOPIC_ID = "DELETE FROM wiki_topic_version WHERE id_topic = ? ";
    private static final String SQL_QUERY_SELECT_CONTENT = "SELECT locale, page_title, wiki_content FROM wiki_topic_version_content WHERE id_topic_version = ?";
    private static final String SQL_QUERY_INSERT_CONTENT = "INSERT INTO wiki_topic_version_content ( id_topic_version, locale, page_title, wiki_content ) VALUES ( ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE_CONTENT = "DELETE FROM wiki_topic_version_content WHERE id_topic_version = ? ";
    private static final String SQL_QUERY_DELETE_CONTENT_BY_TOPIC_ID = "DELETE a.* FROM wiki_topic_version_content a, wiki_topic_version b WHERE a.id_topic_version = b.id_topic_version AND b.id_topic = ? ";
    private static final String SQL_QUERY_SELECT_PUBLISHED_BY_TOPIC_ID= "SELECT id_topic_version, edit_comment, id_topic, lutece_user_id, date_edition, id_topic_version_previous, is_published FROM wiki_topic_version WHERE id_topic = ? AND is_published = 1 ORDER BY  date_edition DESC ";
    private static final String SQL_QUERY_UPDATE_IS_PUBLISHED = "UPDATE wiki_topic_version SET is_published=?,edit_comment=?  WHERE id_topic_version = ? ";
    private static final  String SQL_QUERY_DELETE_CONTENT_BY_TOPIC_VERSION_ID = "DELETE FROM wiki_topic_version_content WHERE id_topic_version = ? ";
    private static final String SQL_QUERY_DELETE_BY_TOPIC_VERSION_ID = "DELETE FROM wiki_topic_version WHERE id_topic_version = ? ";

    /* Generates a new primary key
     *
     * @param plugin The Plugin
     * @return The new primary key
     */
    public int newPrimaryKey(Plugin plugin) {
        int nKey;

        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_NEW_PK, plugin)) {
            daoUtil.executeQuery();

            daoUtil.next();
            nKey = daoUtil.getInt(1) + 1;
        }

        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert(TopicVersion topicVersion, Plugin plugin) {
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_INSERT, plugin)) {

            topicVersion.setIdTopicVersion(newPrimaryKey(plugin));

            daoUtil.setInt(1, topicVersion.getIdTopicVersion());
            daoUtil.setString(2, topicVersion.getEditComment());
            daoUtil.setInt(3, topicVersion.getIdTopic());
            daoUtil.setString(4, topicVersion.getLuteceUserId());
            daoUtil.setTimestamp(5, topicVersion.getDateEdition());
            daoUtil.setInt(6, topicVersion.getIdTopicVersionPrevious());
            daoUtil.setBoolean(7, topicVersion.getIsPublished());

            daoUtil.executeUpdate();
        }

        for (String strLocale : topicVersion.getWikiContents().keySet()) {
            WikiContent content = topicVersion.getWikiContent(strLocale);
            try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_INSERT_CONTENT, plugin)) {
                daoUtil.setInt(1, topicVersion.getIdTopicVersion());
                daoUtil.setString(2, strLocale);
                daoUtil.setString(3, content.getPageTitle());
                daoUtil.setString(4, content.getWikiContent());
                daoUtil.executeUpdate();
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TopicVersion load(int nId, Plugin plugin) {
        TopicVersion topicVersion = null;

        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT, plugin)) {
            daoUtil.setInt(1, nId);
            daoUtil.executeQuery();

            if (daoUtil.next()) {
                topicVersion =  setTopicVersionWithDaoUtil(daoUtil);
            }
        }

        if (topicVersion != null) {
            fillContent(topicVersion);
        }


        return topicVersion;
    }

    /**
     * Fill content
     *
     * @param topicVersion the version
     */
    private void fillContent(TopicVersion topicVersion) {
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT_CONTENT)) {
            daoUtil.setInt(1, topicVersion.getIdTopicVersion());
            daoUtil.executeQuery();
            while (daoUtil.next()) {
                WikiContent content = new WikiContent();

                String strLanguage = daoUtil.getString(1);
                content.setPageTitle(daoUtil.getString(2));
                content.setWikiContent(daoUtil.getString(3));
                topicVersion.addLocalizedWikiContent(strLanguage, content);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete(int nTopicVersionId, Plugin plugin) {
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_DELETE, plugin)) {
            daoUtil.setInt(1, nTopicVersionId);
            daoUtil.executeUpdate();
        }

        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_DELETE_CONTENT, plugin)) {
            daoUtil.setInt(1, nTopicVersionId);
            daoUtil.executeUpdate();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteByTopic(int nTopicId, Plugin plugin) {
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_DELETE_CONTENT_BY_TOPIC_ID, plugin)) {
            daoUtil.setInt(1, nTopicId);
            daoUtil.executeUpdate();
        }

        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_DELETE_BY_TOPIC_ID, plugin)) {
            daoUtil.setInt(1, nTopicId);
            daoUtil.executeUpdate();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteByTopicVersion(int nTopicVersionId, Plugin plugin) {
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_DELETE_CONTENT_BY_TOPIC_VERSION_ID, plugin)) {
            daoUtil.setInt(1, nTopicVersionId);
            daoUtil.executeUpdate();
        }

        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_DELETE_BY_TOPIC_VERSION_ID, plugin)) {
            daoUtil.setInt(1, nTopicVersionId);
            daoUtil.executeUpdate();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateIsPublished(int nIdTopicVersion, String comment, boolean bIsPublished, Plugin plugin) {
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_UPDATE_IS_PUBLISHED, plugin)) {
            daoUtil.setBoolean(1, bIsPublished);
            daoUtil.setString(2, comment);
            daoUtil.setInt(3, nIdTopicVersion);
            daoUtil.executeUpdate();
        }

    }


    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<TopicVersion> selectTopicVersionsList(Plugin plugin) {
        Collection<TopicVersion> topicVersionList = new ArrayList<>();
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECTALL, plugin)) {
            daoUtil.executeQuery();

            while (daoUtil.next()) {
                TopicVersion topicVersion =  setTopicVersionWithDaoUtil(daoUtil);
                fillContent(topicVersion);
                topicVersionList.add(topicVersion);
            }
        }

        return topicVersionList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void addTopicVersion(TopicVersion topicVersion, Plugin plugin) {
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_INSERT_MODIFICATION, plugin)) {
            topicVersion.setIdTopicVersion(newPrimaryKey(plugin));
            daoUtil.setInt(1, topicVersion.getIdTopicVersion());
            daoUtil.setString(2, topicVersion.getEditComment());
            daoUtil.setInt(3, topicVersion.getIdTopic());
            daoUtil.setString(4, topicVersion.getUserName());
            daoUtil.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));
            daoUtil.setInt(6, topicVersion.getIdTopicVersionPrevious());
            daoUtil.setBoolean(7, topicVersion.getIsPublished());

            daoUtil.executeUpdate();
        }

        storeContent(topicVersion);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void updateTopicVersion(TopicVersion topicVersion, Plugin plugin) {
        deleteByTopicVersion(topicVersion.getIdTopicVersion(), plugin);
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_INSERT_MODIFICATION, plugin)) {
            topicVersion.setIdTopicVersion(newPrimaryKey(plugin));
            daoUtil.setInt(1, topicVersion.getIdTopicVersion());
            daoUtil.setString(2, topicVersion.getEditComment());
            daoUtil.setInt(3, topicVersion.getIdTopic());
            daoUtil.setString(4, topicVersion.getUserName());
            daoUtil.setTimestamp(5, new java.sql.Timestamp(new java.util.Date().getTime()));
            daoUtil.setInt(6, topicVersion.getIdTopicVersionPrevious());
            daoUtil.setBoolean(7, topicVersion.getIsPublished());

            daoUtil.executeUpdate();
        }

        storeContent(topicVersion);
    }

    /**
     * Store the content of a Topic Version
     *
     * @param topicVersion The topic Version
     */
    private void storeContent(TopicVersion topicVersion) {
        for (String strLanguage : topicVersion.getWikiContents().keySet()) {
            WikiContent content = topicVersion.getWikiContents().get(strLanguage);
            try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_INSERT_CONTENT)) {
                daoUtil.setInt(1, topicVersion.getIdTopicVersion());
                daoUtil.setString(2, strLanguage);
                daoUtil.setString(3, content.getPageTitle());
                daoUtil.setString(4, content.getWikiContent());
                daoUtil.executeUpdate();
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TopicVersion loadLastVersion(int nIdTopic, Plugin plugin) {
        TopicVersion topicVersion = null;

        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT_LAST_BY_TOPIC_ID, plugin)) {
            daoUtil.setInt(1, nIdTopic);
            daoUtil.executeQuery();

            if (daoUtil.next()) {
                topicVersion =  setTopicVersionWithDaoUtil(daoUtil);
            }
        }

        if (topicVersion != null) {
            fillContent(topicVersion);
        }

        return topicVersion;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TopicVersion getPublishedVersion(int nTopicId, Plugin plugin) {
        TopicVersion topicVersion = null;
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT_PUBLISHED_BY_TOPIC_ID, plugin)) {
            daoUtil.setInt(1, nTopicId);
            daoUtil.executeQuery();

            if (daoUtil.next()) {
                topicVersion =  setTopicVersionWithDaoUtil(daoUtil);
            }
        }
        if (topicVersion != null) {
            fillContent(topicVersion);
        }
        return topicVersion;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<TopicVersion> loadAllVersions(int nIdTopic, Plugin plugin) {
        Collection<TopicVersion> topicVersionList = new ArrayList<>();
        try (DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT_BY_TOPIC_ID, plugin)) {
            daoUtil.setInt(1, nIdTopic);
            daoUtil.executeQuery();

            while (daoUtil.next()) {
             TopicVersion topicVersion =  setTopicVersionWithDaoUtil(daoUtil);
                topicVersionList.add(topicVersion);
            }
        }

        return topicVersionList;
    }

    /**
     * set the content of a topic version with doaUtil
     */
    public TopicVersion setTopicVersionWithDaoUtil(DAOUtil daoUtil) {
        TopicVersion topicVersion = new TopicVersion();
        topicVersion.setIdTopicVersion(daoUtil.getInt(1));
        topicVersion.setEditComment(daoUtil.getString(2));
        topicVersion.setIdTopic(daoUtil.getInt(3));
        topicVersion.setLuteceUserId(daoUtil.getString(4));
        topicVersion.setDateEdition(daoUtil.getTimestamp(5));
        topicVersion.setIdTopicVersionPrevious(daoUtil.getInt(6));
        topicVersion.setIsPublished(daoUtil.getBoolean(7));

        return topicVersion;
    }


}