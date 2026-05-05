-- Add suggested revision feature: lets users without edit rights propose
-- modifications to a wiki item. Proposals stay in PENDING until a user with
-- edit rights approves them (creating a real revision) or rejects them.

CREATE TABLE IF NOT EXISTS wiki_suggested_revision (
    id_suggestion INT AUTO_INCREMENT PRIMARY KEY,
    entity_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content MEDIUMTEXT,
    comment VARCHAR(500),
    author VARCHAR(100),
    author_guid VARCHAR(255),
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    review_comment VARCHAR(500),
    reviewer VARCHAR(100),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_review TIMESTAMP NULL,
    INDEX idx_suggestion_entity_status (entity_id, status),
    INDEX idx_suggestion_status (status),
    INDEX idx_suggestion_author (author_guid),
    CONSTRAINT fk_suggestion_entity FOREIGN KEY (entity_id) REFERENCES wiki_item(id_item) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
