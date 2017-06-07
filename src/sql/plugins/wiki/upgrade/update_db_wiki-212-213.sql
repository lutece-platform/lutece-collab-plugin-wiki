CREATE TABLE wiki_topic_version_content (
  id_topic_version INT DEFAULT '0' NOT NULL,
  locale VARCHAR(50) DEFAULT '' NOT NULL,
  page_title VARCHAR(100) DEFAULT '' NOT NULL,
  wiki_content LONG VARCHAR,
    PRIMARY KEY (id_topic_version , locale )
)COLLATE='utf8_general_ci'
ENGINE=InnoDB;

/* Init new table width FR locale from wiki inner content */
INSERT INTO wiki_topic_version_content SELECT wtv.id_topic_version,'fr',wt.page_title,wtv.wiki_content FROM wiki_topic_version wtv INNER JOIN wiki_topic wt ON wtv.id_topic = wt.id_topic;
/* Init new table width EN locale from wiki inner content */
INSERT INTO wiki_topic_version_content SELECT wtv.id_topic_version,'en',wt.page_title,wtv.wiki_content FROM wiki_topic_version wtv INNER JOIN wiki_topic wt ON wtv.id_topic = wt.id_topic;

