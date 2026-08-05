CREATE TABLE wiki_item (
    id_item INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    icon VARCHAR(100) DEFAULT NULL,
    is_published BOOLEAN DEFAULT FALSE,
    view_role VARCHAR(100) DEFAULT 'none',
    edit_role VARCHAR(100) DEFAULT 'none',
    item_type ENUM('space', 'category', 'book', 'chapter', 'page') NOT NULL,
    id_parent INT,
    item_order INT DEFAULT 0,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code),
    INDEX idx_item_type (item_type),
    INDEX idx_parent_type (id_parent, item_type),
    INDEX idx_parent_order (id_parent, item_order),
    CONSTRAINT fk_item_parent FOREIGN KEY (id_parent) REFERENCES wiki_item(id_item) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Revision table
CREATE TABLE wiki_revision (
    id_revision INT AUTO_INCREMENT PRIMARY KEY,
    entity_id INT NOT NULL,
    revision_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content MEDIUMTEXT,
    comment VARCHAR(500),
    author VARCHAR(100),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_current BOOLEAN DEFAULT FALSE,
    UNIQUE KEY uk_entity_revision (entity_id, revision_number),
    INDEX idx_entity_current (entity_id, is_current),
    INDEX idx_date_creation (date_creation DESC),
    CONSTRAINT fk_revision_entity FOREIGN KEY (entity_id) REFERENCES wiki_item(id_item) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- User permissions table
CREATE TABLE wiki_item_user_permission (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_item INT NOT NULL,
    user_guid VARCHAR(255) NOT NULL,
    user_display_name VARCHAR(255) NOT NULL,
    permission_type VARCHAR(10) NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_item_user_permission (id_item, user_guid, permission_type),
    INDEX idx_item_permission (id_item, permission_type),
    INDEX idx_user_permission (user_guid, permission_type),
    CONSTRAINT fk_permission_item FOREIGN KEY (id_item) REFERENCES wiki_item(id_item) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Permissions granted to everyone sharing a directory attribute value, rather than to named users
CREATE TABLE wiki_item_attribute_permission (
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

-- Suggested revisions (proposed by users without edit rights, awaiting approval)
CREATE TABLE wiki_suggested_revision (
    id_suggestion INT AUTO_INCREMENT PRIMARY KEY,
    entity_id INT NOT NULL,
    id_parent_revision INT NULL,
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

