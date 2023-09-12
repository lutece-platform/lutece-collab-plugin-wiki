ALTER TABLE wiki_topic_version_content ADD html_wiki_content LONG VARCHAR NULL;

ALTER TABLE wiki_topic ADD last_user_editing VARCHAR(100) DEFAULT '' NOT NULL;
ALTER TABLE wiki_topic ADD last_edit_attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
