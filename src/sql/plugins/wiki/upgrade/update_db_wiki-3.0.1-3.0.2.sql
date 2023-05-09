ALTER TABLE wiki_topic ADD COLUMN parent_page_name VARCHAR(100) DEFAULT '' NOT NULL;
ALTER TABLE wiki_topic_version ADD COLUMN is_published INT DEFAULT '0' NOT NULL;
UPDATE wiki_topic_version wtv1 SET is_published = 1 WHERE id_topic_version = (SELECT MAX(id_topic_version) FROM wiki_topic_version wtv2 WHERE wtv1.id_topic = wtv2.id_topic);