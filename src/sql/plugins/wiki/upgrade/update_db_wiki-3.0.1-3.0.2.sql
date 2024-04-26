ALTER TABLE wiki_topic_version ADD COLUMN is_published INT DEFAULT '0' NOT NULL;
ALTER TABLE wiki_topic_version_content ADD html_wiki_content LONG VARCHAR NULL;

--
-- for each id_topic, set is_published to 1 for the latest version
--
UPDATE wiki_topic_version
    JOIN (
        SELECT id_topic, MAX(id_topic_version) AS max_id_topic_version
        FROM wiki_topic_version
        GROUP BY id_topic
    ) AS max_versions ON wiki_topic_version.id_topic = max_versions.id_topic
        AND wiki_topic_version.id_topic_version = max_versions.max_id_topic_version
SET wiki_topic_version.is_published = 1;

--
-- for each id_topic, set is_published to 1 for the latest version
--
UPDATE wiki_topic_version
    JOIN (
        SELECT id_topic, MAX(id_topic_version) AS max_id_topic_version
        FROM wiki_topic_version
        GROUP BY id_topic
    ) AS max_versions ON wiki_topic_version.id_topic = max_versions.id_topic
        AND wiki_topic_version.id_topic_version = max_versions.max_id_topic_version
SET wiki_topic_version.is_published = 1;

