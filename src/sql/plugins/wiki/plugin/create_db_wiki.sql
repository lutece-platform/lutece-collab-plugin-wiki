
--
-- Structure for table wiki_topic
--

DROP TABLE IF EXISTS wiki_topic;
CREATE TABLE wiki_topic (		
id_topic INT DEFAULT '0' NOT NULL,
namespace INT DEFAULT '0' NOT NULL,
page_name VARCHAR(100) DEFAULT '' NOT NULL,
page_view_role VARCHAR(50) DEFAULT '' NOT NULL,
page_edit_role VARCHAR(50) DEFAULT '' NOT NULL,
parent_page_name VARCHAR(100) DEFAULT '' NOT NULL,
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


--
-- Structure for table wiki_topic_version
--

DROP TABLE IF EXISTS wiki_topic_version_content;
CREATE TABLE wiki_topic_version_content (
  id_topic_version INT DEFAULT '0' NOT NULL,
  locale VARCHAR(50) DEFAULT '' NOT NULL,
  page_title VARCHAR(100) DEFAULT '' NOT NULL,
  wiki_content LONG VARCHAR,
  html_wiki_content LONG VARCHAR NULL,
    PRIMARY KEY (id_topic_version , locale )
) CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


--
-- Structure for table wiki_image
--
DROP TABLE IF EXISTS wiki_image;
CREATE TABLE wiki_image
(
 	id_image INT DEFAULT 0 NOT NULL,
	name VARCHAR(255) DEFAULT NULL,
	mime_type VARCHAR(50) DEFAULT NULL,
	file_value LONG VARBINARY,
        id_topic INT DEFAULT NULL,
	width INT DEFAULT NULL,
	height INT DEFAULT NULL,
	PRIMARY KEY (id_image)
);

