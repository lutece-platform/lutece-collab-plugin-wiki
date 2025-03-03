ALTER TABLE wiki_topic_version
ADD COLUMN is_published BOOLEAN DEFAULT FALSE;
CREATE TABLE wiki_last_edits
(
    id_topic INT NOT NULL,
    name_user_editing VARCHAR(100) DEFAULT '' NOT NULL,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id_topic)
);
