-- Drop the wiki_create_book role.
--
-- Creating a book now follows the same rule as a category, a chapter or a page: the edit
-- permission on the parent item is what grants it. The functional role no longer takes part in
-- any check, so it is removed along with the assignments that carried it.
--
-- This script is idempotent. The mylutece_users_userrole statement requires module-mylutece-users.

DELETE FROM mylutece_users_userrole WHERE role_key = 'wiki_create_book';

DELETE FROM core_role WHERE role = 'wiki_create_book';
