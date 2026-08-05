-- Grant permissions to a population rather than to named users.
--
-- Until now an item could only be opened to people one name at a time. Opening it to a whole
-- direction meant ticking every one of its agents, which froze the browser on large directions and,
-- worse, went stale the day someone joined or left: the list was a photograph of a moving population.
--
-- A rule is stored instead — an attribute, a value, a permission — and evaluated on each request
-- against the attributes the signed-in user carries (their direction, their sub-direction, their
-- office). Nobody maintains the membership: the directory does.
--
-- Named grants keep working, in wiki_item_user_permission, for the cases where a person really is
-- the right unit.
--
-- This script is idempotent.

CREATE TABLE IF NOT EXISTS wiki_item_attribute_permission (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_item INT NOT NULL,
    attribute_name VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(255) NOT NULL,
    attribute_label VARCHAR(255) NOT NULL,
    permission_type VARCHAR(10) NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_item_attribute_permission (id_item, attribute_name, attribute_value, permission_type),
    INDEX idx_item_attribute_permission (id_item, permission_type),
    INDEX idx_attribute_permission (attribute_name, attribute_value, permission_type),
    CONSTRAINT fk_attribute_permission_item FOREIGN KEY (id_item) REFERENCES wiki_item(id_item) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- The values suggested while typing a search criterion are read from the directory by a daemon and
-- kept here, so a restart does not leave the suggestions empty until its next run. The row is written
-- by the plugin itself; this only makes sure a stale one from an earlier format is dropped.
DELETE FROM core_datastore WHERE entity_key = 'wiki.userAttributeValues.snapshot';
