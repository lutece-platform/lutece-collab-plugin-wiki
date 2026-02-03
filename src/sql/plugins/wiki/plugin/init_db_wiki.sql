-- liquibase formatted sql
-- changeset wiki:init_db_wiki.sql
-- preconditions onFail:MARK_RAN onError:WARN

INSERT INTO wiki_item (code, is_published, view_role, edit_role, item_type) VALUES
('getting-started', TRUE, 'none', 'wiki_editor', 'book');


INSERT INTO core_role VALUES
( 'wiki_admin' , 'Wiki Administrator (can remove content)', '' ),
( 'wiki_create_space' , 'Wiki Space Creator (can create new spaces)', '' ),
( 'wiki_create_book' , 'Wiki Book Creator (can create new books)', '' );