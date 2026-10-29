-- liquibase formatted sql
-- changeset wiki:update_db_wiki-6.0.0-6.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- Move per-user permissions out of Lutece roles and into wiki data.
--
-- Until now, granting a user access to an item created one role per item and per permission
-- (WIKI_ITEM_VIEW_<id>, WIKI_ITEM_EDIT_<id>) and assigned it to that user, while a "private" item
-- carried a per-user role (WIKI_USER_<guid>). Roles are resolved once, at sign-in, so a grant only
-- took effect after the target user signed in again, and the per-user role was never assigned to
-- anyone, which locked creators out of their own private items.
--
-- Permissions now live in wiki_item_user_permission, read on every request. Roles keep their proper
-- job: functional roles (wiki_admin, wiki_create_space, wiki_create_book, wiki_ai_features) and any
-- RBAC permission an administrator assigns to a role from the back office.
--
-- This script is idempotent. It requires module-mylutece-users, whose tables hold the assignments
-- being migrated.

CREATE TABLE IF NOT EXISTS wiki_item_user_permission (
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

-- The name to display for a migrated user, resolved once for the whole script: a readable first
-- and last name, else the login, else the technical guid.

CREATE OR REPLACE VIEW wiki_migration_display_name AS
SELECT connect_id, connect_id_provider,
       COALESCE( NULLIF( TRIM( CONCAT( given_name, ' ', last_name ) ), '' ), NULLIF( login, '' ), connect_id_provider ) AS display_name
FROM mylutece_users_searchuser;

-- 1. Per-item roles assigned to users become permissions on that item.

INSERT IGNORE INTO wiki_item_user_permission ( id_item, user_guid, user_display_name, permission_type )
SELECT i.id_item, su.connect_id_provider, su.display_name, 'VIEW'
FROM mylutece_users_userrole ur
JOIN wiki_migration_display_name su ON su.connect_id = ur.id_searchuser
JOIN wiki_item i ON i.id_item = CAST( SUBSTRING( ur.role_key, CHAR_LENGTH( 'WIKI_ITEM_VIEW_' ) + 1 ) AS UNSIGNED )
WHERE ur.role_key LIKE 'WIKI\_ITEM\_VIEW\_%'
  AND su.connect_id_provider IS NOT NULL AND su.connect_id_provider <> '';

INSERT IGNORE INTO wiki_item_user_permission ( id_item, user_guid, user_display_name, permission_type )
SELECT i.id_item, su.connect_id_provider, su.display_name, 'EDIT'
FROM mylutece_users_userrole ur
JOIN wiki_migration_display_name su ON su.connect_id = ur.id_searchuser
JOIN wiki_item i ON i.id_item = CAST( SUBSTRING( ur.role_key, CHAR_LENGTH( 'WIKI_ITEM_EDIT_' ) + 1 ) AS UNSIGNED )
WHERE ur.role_key LIKE 'WIKI\_ITEM\_EDIT\_%'
  AND su.connect_id_provider IS NOT NULL AND su.connect_id_provider <> '';

-- 2. Items carrying a per-user role: their creator gets the permission that role was standing for.
--    The user may be unknown to mylutece, hence the fallback on the guid carried by the role.

INSERT IGNORE INTO wiki_item_user_permission ( id_item, user_guid, user_display_name, permission_type )
SELECT i.id_item, SUBSTRING( i.view_role, CHAR_LENGTH( 'WIKI_USER_' ) + 1 ),
       COALESCE( su.display_name, SUBSTRING( i.view_role, CHAR_LENGTH( 'WIKI_USER_' ) + 1 ) ), 'VIEW'
FROM wiki_item i
LEFT JOIN wiki_migration_display_name su ON su.connect_id_provider = SUBSTRING( i.view_role, CHAR_LENGTH( 'WIKI_USER_' ) + 1 )
WHERE i.view_role LIKE 'WIKI\_USER\_%';

INSERT IGNORE INTO wiki_item_user_permission ( id_item, user_guid, user_display_name, permission_type )
SELECT i.id_item, SUBSTRING( i.edit_role, CHAR_LENGTH( 'WIKI_USER_' ) + 1 ),
       COALESCE( su.display_name, SUBSTRING( i.edit_role, CHAR_LENGTH( 'WIKI_USER_' ) + 1 ) ), 'EDIT'
FROM wiki_item i
LEFT JOIN wiki_migration_display_name su ON su.connect_id_provider = SUBSTRING( i.edit_role, CHAR_LENGTH( 'WIKI_USER_' ) + 1 )
WHERE i.edit_role LIKE 'WIKI\_USER\_%';

-- 3. Items whose view_role or edit_role points at the technical role of another item: the item
--    form used to offer those roles, so "visible to whoever edits item X" was expressed that way.
--    Every holder of that role gets the permission directly.

INSERT IGNORE INTO wiki_item_user_permission ( id_item, user_guid, user_display_name, permission_type )
SELECT i.id_item, su.connect_id_provider, su.display_name, 'VIEW'
FROM wiki_item i
JOIN mylutece_users_userrole ur ON ur.role_key = i.view_role
JOIN wiki_migration_display_name su ON su.connect_id = ur.id_searchuser
WHERE i.view_role LIKE 'WIKI\_ITEM\_%'
  AND su.connect_id_provider IS NOT NULL AND su.connect_id_provider <> '';

INSERT IGNORE INTO wiki_item_user_permission ( id_item, user_guid, user_display_name, permission_type )
SELECT i.id_item, su.connect_id_provider, su.display_name, 'EDIT'
FROM wiki_item i
JOIN mylutece_users_userrole ur ON ur.role_key = i.edit_role
JOIN wiki_migration_display_name su ON su.connect_id = ur.id_searchuser
WHERE i.edit_role LIKE 'WIKI\_ITEM\_%'
  AND su.connect_id_provider IS NOT NULL AND su.connect_id_provider <> '';

-- 4. Every item that carried a technical role becomes private. Runs after the inserts above, which
--    read the original role values.

UPDATE wiki_item SET view_role = 'private' WHERE view_role LIKE 'WIKI\_USER\_%' OR view_role LIKE 'WIKI\_ITEM\_%';
UPDATE wiki_item SET edit_role = 'private' WHERE edit_role LIKE 'WIKI\_USER\_%' OR edit_role LIKE 'WIKI\_ITEM\_%';

-- 5. Drop the technical roles, their assignments and their RBAC rows. RBAC permissions assigned
--    to functional or business roles are left untouched.

DELETE FROM mylutece_users_userrole WHERE role_key LIKE 'WIKI\_ITEM\_%' OR role_key LIKE 'WIKI\_USER\_%';
DELETE FROM core_admin_role_resource WHERE role_key LIKE 'WIKI\_ITEM\_%' OR role_key LIKE 'WIKI\_USER\_%';
DELETE FROM core_user_role WHERE role_key LIKE 'WIKI\_ITEM\_%' OR role_key LIKE 'WIKI\_USER\_%';
DELETE FROM core_admin_role WHERE role_key LIKE 'WIKI\_ITEM\_%' OR role_key LIKE 'WIKI\_USER\_%';
DELETE FROM core_role WHERE role LIKE 'WIKI\_ITEM\_%' OR role LIKE 'WIKI\_USER\_%';

DROP VIEW IF EXISTS wiki_migration_display_name;

-- changeset wiki:update_db_wiki-6.0.0-6.0.1-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = database() AND table_name = 'wiki_item_attribute_permission'
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

-- changeset wiki:update_db_wiki-6.0.0-6.0.1-rev2.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- Drop the wiki_create_book role.
--
-- Creating a book now follows the same rule as a category, a chapter or a page: the edit
-- permission on the parent item is what grants it. The functional role no longer takes part in
-- any check, so it is removed along with the assignments that carried it.
--
-- This script is idempotent. The mylutece_users_userrole statement requires module-mylutece-users.

DELETE FROM mylutece_users_userrole WHERE role_key = 'wiki_create_book';

DELETE FROM core_role WHERE role = 'wiki_create_book';
