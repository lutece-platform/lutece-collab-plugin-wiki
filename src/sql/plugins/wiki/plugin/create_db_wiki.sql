
--
-- Structure for table wiki_topic
--

DROP TABLE IF EXISTS wiki_topic;
CREATE TABLE wiki_topic (		
id_topic INT DEFAULT '0' NOT NULL,
namespace INT DEFAULT '0' NOT NULL,
page_name VARCHAR(100) DEFAULT '' NOT NULL,
page_title VARCHAR(100) DEFAULT '' NOT NULL,
page_view_role VARCHAR(50) DEFAULT '' NOT NULL,
page_edit_role VARCHAR(50) DEFAULT '' NOT NULL,
  PRIMARY KEY (id_topic)
);

--
-- Structure for table wiki_topic_version
--

DROP TABLE IF EXISTS wiki_topic_version;
CREATE TABLE wiki_topic_version (
  id_topic_version INT DEFAULT '0' NOT NULL,
  edit_comment VARCHAR(50) DEFAULT '' NOT NULL,
  id_topic INT DEFAULT '0' NOT NULL,
  lutece_user_id VARCHAR(50) DEFAULT '' NOT NULL ,
  date_edition TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  id_topic_version_previous INT DEFAULT '0' NOT NULL,
  wiki_content LONG VARCHAR,
    PRIMARY KEY (id_topic_version)
);