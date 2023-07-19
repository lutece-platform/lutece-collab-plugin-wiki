ALTER TABLE wiki_topic_version ADD COLUMN is_published INT DEFAULT '2' NOT NULL;
ALTER TABLE wiki_topic_version MODIFY is_published INT DEFAULT '0' NOT NULL;

ALTER TABLE wiki_topic_version_content ADD html_wiki_content LONG VARCHAR NULL;



