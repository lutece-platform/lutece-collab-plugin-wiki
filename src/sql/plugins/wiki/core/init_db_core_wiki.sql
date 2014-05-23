--
-- Dumping data for table core_datastore
--
REPLACE INTO core_datastore VALUES 
('wiki.site_property.path.rootLabel','Accueil du Wiki'),
('wiki.site_property.path.rootPageName','accueil'),
('wiki.site_property.role.admin','wiki_admin');

INSERT INTO core_role VALUES 
( 'wiki_admin' , 'Wiki Administrator (can remove content)', '' ),
( 'wiki_contributor' , 'Wiki Contributor (can edit content)', '' ),
( 'wiki_private' , 'Wiki User (can see private content)', '' );